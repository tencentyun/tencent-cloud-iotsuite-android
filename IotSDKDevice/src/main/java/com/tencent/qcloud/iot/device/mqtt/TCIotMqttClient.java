package com.tencent.qcloud.iot.device.mqtt;

import android.os.SystemClock;

import com.tencent.qcloud.iot.common.ReconnectHelper;
import com.tencent.qcloud.iot.device.mqtt.callback.IMqttActionCallback;
import com.tencent.qcloud.iot.device.mqtt.callback.IMqttConnectStateCallback;
import com.tencent.qcloud.iot.device.mqtt.callback.IMqttMessageListener;
import com.tencent.qcloud.iot.device.mqtt.certificate.TCCertificateException;
import com.tencent.qcloud.iot.device.mqtt.certificate.TCSSLSocketException;
import com.tencent.qcloud.iot.device.mqtt.certificate.TCTLSSocketFactory;
import com.tencent.qcloud.iot.device.mqtt.certificate.TokenHelper;
import com.tencent.qcloud.iot.device.mqtt.constant.MqttConnectState;
import com.tencent.qcloud.iot.device.mqtt.constant.TCConstants;
import com.tencent.qcloud.iot.device.mqtt.constant.TCIotMqttQos;
import com.tencent.qcloud.iot.device.mqtt.request.BaseMqttRequest;
import com.tencent.qcloud.iot.device.mqtt.request.MqttPublishRequest;
import com.tencent.qcloud.iot.device.mqtt.request.MqttSubscribeRequest;
import com.tencent.qcloud.iot.device.mqtt.request.MqttUnSubscribeRequest;
import com.tencent.qcloud.iot.log.QLog;
import com.tencent.qcloud.iot.utils.StringUtil;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by rongerwu on 2018/1/10.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class TCIotMqttClient extends AbstractIotMqttClient {
    private static final String TAG = TCIotMqttClient.class.getSimpleName();
    private static final int CONNECT_TIMEOUT_MS = 40 * 1000;

    private TCMqttConfig mTCMqttConfig;
    private MqttAsyncClient mMqttClient;
    private MqttConnectState mConnectState = MqttConnectState.CLOSED;
    private IMqttConnectStateCallback mMqttConnectStateCallback;
    private String mMqttClientId;
    private boolean mUserDisconnect = true;
    private ReconnectHelper mReconnectHelper;
    private ConcurrentLinkedQueue<BaseMqttRequest> mMqttRequestQueue;
    private ConcurrentLinkedQueue<MqttSubscribeRequest> mReSubscribeQueue;
    private IMqttMessageListener mMqttMessageListener;
    private final Object mConnectSuccessLock = new Object();
    private final Object mConnectToken = new Object();

    public TCIotMqttClient(TCMqttConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null!");
        }
        mTCMqttConfig = config;
        mReconnectHelper = new ReconnectHelper(config.isAutoReconnect())
                .setMinRetryTimeMs(config.getMinRetryTimeMs())
                .setMaxRetryTimeMs(config.getMaxRetryTimeMs())
                .setMaxRetryTimes(config.getMaxRetryTimes());
        mMqttRequestQueue = new ConcurrentLinkedQueue<>();
        mReSubscribeQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * 建立mqtt连接.
     * 必须在指定线程上运行.
     *
     * @param connectStateCallback connect状态回调
     */
    @Override
    protected void connectInternal(final IMqttConnectStateCallback connectStateCallback) {
        if (Thread.currentThread() != getHandler().getLooper().getThread()) {
            throw new IllegalStateException("wrong thread");
        }

        if (mConnectState != MqttConnectState.CLOSED) {
            QLog.i(TAG, "can not do connect process now! current state = " + mConnectState);
            return;
        }
        mUserDisconnect = false;
        mMqttClientId = mTCMqttConfig.getProductKey() + "@" + mTCMqttConfig.getDeviceName();
        mMqttConnectStateCallback = connectStateCallback;
        mMqttRequestQueue.clear();
        mReSubscribeQueue.clear();
        setConnectStateAndNotify(MqttConnectState.CONNECTING);

        doConnect();
    }

    private void doConnect() {
        if (Thread.currentThread() != getHandler().getLooper().getThread()) {
            throw new IllegalStateException("wrong thread");
        }
        if (mTCMqttConfig.getConnectionMode() == TCMqttConfig.TCMqttConnectionMode.MODE_DIRECT) {
            //直连模式，直接mqtt连接.
            mqttConnect();
        } else if (mTCMqttConfig.getConnectionMode() == TCMqttConfig.TCMqttConnectionMode.MODE_TOKEN) {
            //token模式，先请求token，再mqtt连接.
            TokenHelper tokenHelper = new TokenHelper(mTCMqttConfig.getRegion(), mTCMqttConfig.getProductId(), mTCMqttConfig.getDeviceName(), mTCMqttConfig.getDeviceSecret(),
                    mTCMqttConfig.getTokenScheme());
            final String clientId = mMqttClientId;
            tokenHelper.getToken(clientId, new TokenHelper.ITokenCallback() {
                @Override
                public void onSuccess(String userName, String password) {
                    mTCMqttConfig.setMqttUserName(userName)
                            .setMqttPassword(password);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (mUserDisconnect || !clientId.equals(mMqttClientId)) {
                                return;
                            }
                            mqttConnect();
                        }
                    });
                }

                @Override
                public void onFailure(String message) {
                    if (mUserDisconnect || !clientId.equals(mMqttClientId)) {
                        return;
                    }
                    QLog.e(TAG, "get token failed, " + message);
                    onConnectFailed(new Throwable("get token failed, " + message));
                }
            });
        }
    }

    private void mqttConnect() {
        String mqttBrokerURL = "tcp://" + mTCMqttConfig.getMqttHost() + ":" + TCConstants.MQTT_PORT;
        try {
            mMqttClient = new MqttAsyncClient(mqttBrokerURL, mMqttClientId, new MemoryPersistence());
            //QLog.d(TAG, "url = " + mqttBrokerURL + ", clientid = " + mMqttClientId);
            SSLSocketFactory socketFactory = null;
            if (mTCMqttConfig.getKeyStore() != null) {
                socketFactory = TCTLSSocketFactory.getSocketFactory(mTCMqttConfig.getKeyStore());
            }
            mMqttClient.setCallback(mMqttCallback);
            mMqttClient.connect(buildMqttConnectOptions(socketFactory), null, mMqttConnectActionCallback);

            //有出现一次bug：重连时发送connect包到服务器后，客户端没有收到ack，也就没有启动keep-alive线程，服务器15s没收到心跳包后断开tcp，客户端却没有任何消息抛出。
            //这种情况下，一直处于connecting状态无法重连。
            getHandler().postAtTime(new Runnable() {
                @Override
                public void run() {
                    QLog.e(TAG, "connect timeout, timeout = " + CONNECT_TIMEOUT_MS + " ms");
                    try {
                        mMqttClient.disconnect(0);
                    } catch (MqttException e) {
                        QLog.i(TAG, "connect timeout, then disconnect exception", e);
                    }
                    reconnect(1000);
                }
            }, mConnectToken, SystemClock.uptimeMillis() + CONNECT_TIMEOUT_MS);
        } catch (MqttException e) {
            switch (e.getReasonCode()) {
                case MqttException.REASON_CODE_CLIENT_CONNECTED:
                    QLog.d(TAG, "connect exception", e);
                    setConnectStateAndNotify(MqttConnectState.CONNECTED);
                    break;
                case MqttException.REASON_CODE_CONNECT_IN_PROGRESS:
                    //setConnectStateAndNotify(MqttConnectState.CONNECTING);
                    QLog.i(TAG, "connect exception", e);
                    break;
                default:
                    QLog.e(TAG, "connect exception: ", e);
                    onConnectFailed(e);
                    break;
            }
        } catch (TCSSLSocketException e) {
            throw new TCCertificateException("get socket factory exception!", e);
        }
    }

    /**
     * 连接失败时调用，触发重连或关闭连接.
     *
     * @param exception
     */
    private void onConnectFailed(Throwable exception) {
        //TODO: pass exception to outside
        if (shouldReconnect()) {
            setConnectStateAndNotify(MqttConnectState.PRE_RECONNECT);
            reconnect(mReconnectHelper.getRetryDelay());
        } else {
            disconnect();
        }
    }

    /**
     * 重连.
     * 必须在指定线程上运行.
     */
    @Override
    protected void reconnectInternal() {
        if (Thread.currentThread() != getHandler().getLooper().getThread()) {
            throw new IllegalStateException("wrong thread");
        }
        if (!shouldReconnect()) {
            QLog.d(TAG, "should not reconnect now");
            disconnect();
            return;
        }
        setConnectStateAndNotify(MqttConnectState.RECONNECTING);
        doConnect();
        mReconnectHelper.addRetryTimes();
    }

    /**
     * 断开连接
     * 必须在指定线程上运行.
     */
    @Override
    protected void disconnectInternal() {
        QLog.i(TAG, "user disconnect");
        if (Thread.currentThread() != getHandler().getLooper().getThread()) {
            throw new IllegalStateException("wrong thread");
        }
        mUserDisconnect = true;
        mReconnectHelper.reset();
        mMqttRequestQueue.clear();
        mReSubscribeQueue.clear();
        if (mMqttClient != null && mConnectState != MqttConnectState.CLOSED) {
            try {
                mMqttClient.setCallback(null);
                mMqttClient.disconnect(0);
            } catch (MqttException e) {
                QLog.d(TAG, "disconnect exception", e);
            }
        }
        setConnectStateAndNotify(MqttConnectState.CLOSED);
    }

    /**
     * 构建mqtt连接所需的options
     *
     * @param socketFactory
     * @return
     */
    private MqttConnectOptions buildMqttConnectOptions(SocketFactory socketFactory) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        //options.setCleanSession(false);
        if (socketFactory != null) {
            options.setSocketFactory(socketFactory);
        }
        options.setAutomaticReconnect(false);
        options.setUserName(mTCMqttConfig.getMqttUserName());
        options.setPassword(mTCMqttConfig.getMqttPassword().toCharArray());
        options.setKeepAliveInterval(mTCMqttConfig.getKeepAliveSeconds());
        return options;
    }

    /**
     * 设置当前连接状态，并调用回调函数告知外部.
     *
     * @param state
     */
    private void setConnectStateAndNotify(MqttConnectState state) {
        boolean stateChanged = (mConnectState != state);
        mConnectState = state;
        QLog.d(TAG, "set connect state to " + state);
        if (stateChanged) {
            if (mMqttConnectStateCallback != null) {
                mMqttConnectStateCallback.onStateChanged(mConnectState);
            }
        }
    }

    /**
     * 监听连接丢失事件、消息到达事件
     */
    private MqttCallback mMqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            QLog.i(TAG, "connectionLost, clientId = " + mMqttClientId, cause);
            onConnectFailed(cause);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            if (message == null || message.getPayload() == null) {
                QLog.e(TAG, "messageArrived error, message or payload is null");
                return;
            }
            //QLog.d(TAG, "messageArrived: " + message + ", clientId = " + mMqttClientId + ", topic = " + topic);
            String msg = new String(message.getPayload(), StringUtil.UTF8);
            if (mMqttMessageListener != null) {
                mMqttMessageListener.onMessageArrived(topic, msg);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            //QLog.d(TAG, "clientId = " + mMqttClientId + ", deliveryComplete: " + token);
        }
    };

    /**
     * 监听连接是否成功
     */
    private IMqttActionListener mMqttConnectActionCallback = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            QLog.d(TAG, "connect successed, clientId = " + mMqttClientId);
            getHandler().removeCallbacksAndMessages(mConnectToken);
            mReconnectHelper.reset();
            synchronized (mConnectSuccessLock) {
                setConnectStateAndNotify(MqttConnectState.CONNECTED);
                reSubscribeFromQueue();
                sendRequestFromQueueIfConnected();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            QLog.w(TAG, "connect failed, exception: " + (exception != null ? exception : "") + ", clientId = " + mMqttClientId);
            getHandler().removeCallbacksAndMessages(mConnectToken);
            onConnectFailed(exception);
        }
    };

    /**
     * 判断是否满足重连的条件
     *
     * @return
     */
    private boolean shouldReconnect() {
        QLog.d(TAG, "shouldReconnect. mUserDisconnect = " + mUserDisconnect + ", shouldRetry = " + mReconnectHelper.shouldRetry()
                + ", mConnectState = " + mConnectState + ", retryDelay = " + mReconnectHelper.getRetryDelay());
        return (!mUserDisconnect && mReconnectHelper.shouldRetry());
    }

    /**
     * 发布消息.
     * 必须在指定线程上运行.
     *
     * @param request 请求
     */
    @Override
    protected void publishInternal(final MqttPublishRequest request) {
        if (Thread.currentThread() != getHandler().getLooper().getThread()) {
            throw new IllegalStateException("wrong thread");
        }
        preRequest(request);
    }

    /**
     * 订阅主题.
     * 必须在指定线程上运行.
     *
     * @param request 请求
     */
    @Override
    protected void subscribeInternal(final MqttSubscribeRequest request) {
        if (Thread.currentThread() != getHandler().getLooper().getThread()) {
            throw new IllegalStateException("wrong thread");
        }
        preRequest(request);
    }

    /**
     * 取消订阅主题.
     * 必须在指定线程上运行.
     *
     * @param request 请求
     */
    @Override
    protected void unSubscribeInternal(final MqttUnSubscribeRequest request) {
        if (Thread.currentThread() != getHandler().getLooper().getThread()) {
            throw new IllegalStateException("wrong thread");
        }
        preRequest(request);
    }

    /**
     * 订阅及发布相关请求的预处理.
     * 当未连接时不予请求. 当连接但未成功时将请求加入队列，待连接成功时再发出请求.
     *
     * @param request 请求
     */
    private void preRequest(BaseMqttRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        if (mUserDisconnect) {
            QLog.w(TAG, "cannot request, client has closed, ");
            if (request.getCallback() != null) {
                request.getCallback().onFailure(new Throwable("disconnected"));
            }
            return;
        }
        if (mMqttRequestQueue.size() > 10000) {
            QLog.w(TAG, "too much request: " + mMqttRequestQueue.size() + ", discard current");
            return;
        }
        mMqttRequestQueue.add(request);
        synchronized (mConnectSuccessLock) {
            sendRequestFromQueueIfConnected();
        }
    }

    /**
     * 连接成功时，处理队列中的待发送请求
     */
    private void sendRequestFromQueueIfConnected() {
        if (mConnectState != MqttConnectState.CONNECTED) {
            QLog.d(TAG, "cannot send request now, will send after connect to server");
            return;
        }
        BaseMqttRequest request;
        while ((request = mMqttRequestQueue.poll()) != null) {
            if (request instanceof MqttPublishRequest) {
                MqttPublishRequest publishRequest = (MqttPublishRequest) request;
                mqttPublish(publishRequest.getMsg(), publishRequest.getTopic(), publishRequest.getQos(), publishRequest.getCallback());
            } else if (request instanceof MqttSubscribeRequest) {
                MqttSubscribeRequest subscribeRequest = (MqttSubscribeRequest) request;
                mqttSubscribe(subscribeRequest.getTopic(), subscribeRequest.getQos(), subscribeRequest.getCallback());
                mReSubscribeQueue.add(subscribeRequest);
            } else if (request instanceof MqttUnSubscribeRequest) {
                MqttUnSubscribeRequest unSubscribeRequest = (MqttUnSubscribeRequest) request;
                mqttUnSubscribe(unSubscribeRequest.getTopic(), unSubscribeRequest.getCallback());
            } else {
                throw new TCMqttClientException("invalid request type");
            }

            //一定程度上缓解发送压力
            int sleepMillis = Math.min((mMqttRequestQueue.size() + 1) * 20, 200);
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                QLog.w(TAG, "sleep millis = " + sleepMillis, e);
            }
        }
    }

    private void mqttPublish(final byte[] data, final String topic, final TCIotMqttQos qos, final IMqttActionCallback callback) {
        MqttMessage message = new MqttMessage(data);
        message.setQos(qos.asInt());
        message.setRetained(false);
        MqttActionUserContext userContext = new MqttActionUserContext()
                .setActionType(MqttActionUserContext.ACTION_TYPE_PUBLISH)
                .setTopic(topic)
                .setCallback(callback);
        try {
            mMqttClient.publish(topic, message, userContext, mPubSubCallback);
        } catch (MqttException e) {
            if (callback != null) {
                QLog.e(TAG, "mqtt publish failure: " + e);
                callback.onFailure(e);
            }
        }
    }

    private void mqttSubscribe(final String topic, final TCIotMqttQos qos, final IMqttActionCallback callback) {
        MqttActionUserContext userContext = new MqttActionUserContext()
                .setActionType(MqttActionUserContext.ACTION_TYPE_SUBSCRIBE)
                .setTopic(topic)
                .setCallback(callback);
        try {
            mMqttClient.subscribe(topic, qos.asInt(), userContext, mPubSubCallback);
        } catch (MqttException e) {
            if (callback != null) {
                callback.onFailure(e);
            }
        }
    }

    private void mqttUnSubscribe(final String topic, final IMqttActionCallback callback) {
        MqttActionUserContext userContext = new MqttActionUserContext()
                .setActionType(MqttActionUserContext.ACTION_TYPE_UNSUBSCRIBE)
                .setCallback(callback);
        try {
            mMqttClient.unsubscribe(topic, userContext, mPubSubCallback);
        } catch (MqttException e) {
            if (callback != null) {
                callback.onFailure(e);
            }
        }
    }

    /**
     * 断网重连后，重新订阅topic
     */
    private void reSubscribeFromQueue() {
        ConcurrentLinkedQueue<MqttSubscribeRequest> cloneQueue = new ConcurrentLinkedQueue<MqttSubscribeRequest>(mReSubscribeQueue);
        mReSubscribeQueue.clear();
        MqttSubscribeRequest request;
        while ((request = cloneQueue.poll()) != null) {
            QLog.d(TAG, "try resubscribe to topic = " + request.getTopic());
            subscribe(request);
        }
    }

    /**
     * 监听发布和订阅等请求的结果
     */
    private IMqttActionListener mPubSubCallback = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            if (asyncActionToken.getUserContext() != null) {
                MqttActionUserContext userContext = ((MqttActionUserContext) asyncActionToken.getUserContext());
                IMqttActionCallback actionCallback = userContext.getCallback();
                QLog.d(TAG, "on action success, type = " + userContext.getActionType() + ", topic = " + userContext.getTopic());
                if (actionCallback != null) {
                    actionCallback.onSuccess();
                }
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (asyncActionToken.getUserContext() != null) {
                MqttActionUserContext userContext = ((MqttActionUserContext) asyncActionToken.getUserContext());
                IMqttActionCallback actionCallback = userContext.getCallback();
                QLog.w(TAG, "on action fail, type = " + userContext.getActionType() + ", topic = " + userContext.getTopic());
                if (actionCallback != null) {
                    actionCallback.onFailure(exception);
                }
            }
        }
    };

    /**
     * 设置消息到达的监听，当所订阅主题发来消息时，会触发此监听.
     *
     * @param listener 监听接口
     * @return 返回当前实例
     */
    public TCIotMqttClient setMqttMessageListener(IMqttMessageListener listener) {
        mMqttMessageListener = listener;
        return this;
    }
}
