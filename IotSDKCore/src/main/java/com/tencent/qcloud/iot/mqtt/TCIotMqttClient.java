package com.tencent.qcloud.iot.mqtt;

import com.tencent.qcloud.iot.common.QLog;
import com.tencent.qcloud.iot.common.ReconnectHelper;
import com.tencent.qcloud.iot.mqtt.callback.IMqttActionCallback;
import com.tencent.qcloud.iot.mqtt.callback.IMqttConnectStateCallback;
import com.tencent.qcloud.iot.mqtt.callback.IMqttMessageListener;
import com.tencent.qcloud.iot.mqtt.certificate.TCCertificateException;
import com.tencent.qcloud.iot.mqtt.certificate.TCSSLSocketException;
import com.tencent.qcloud.iot.mqtt.certificate.TCTLSSocketFactory;
import com.tencent.qcloud.iot.mqtt.certificate.TokenHelper;
import com.tencent.qcloud.iot.mqtt.constant.MqttConnectState;
import com.tencent.qcloud.iot.mqtt.constant.TCConstants;
import com.tencent.qcloud.iot.mqtt.constant.TCIotMqttQos;
import com.tencent.qcloud.iot.mqtt.request.BaseMqttRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttPublishRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttSubscribeRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttUnSubscribeRequest;
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

    private TCMqttConfig mTCMqttConfig;
    private MqttAsyncClient mMqttClient;
    private MqttConnectOptions mMqttConnectOptions;
    private MqttConnectState mConnectState = MqttConnectState.CLOSED;
    private IMqttConnectStateCallback mMqttConnectStateCallback;
    private String mMqttClientId;
    private boolean mUserDisconnect = true;
    private ReconnectHelper mReconnectHelper;
    private ConcurrentLinkedQueue<BaseMqttRequest> mMqttRequestQueue;
    private ConcurrentLinkedQueue<MqttSubscribeRequest> mReSubscribeQueue;
    private IMqttMessageListener mMqttMessageListener;

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
        if (mTCMqttConfig.getConnectionMode() == TCMqttConfig.TCMqttConnectionMode.MODE_DIRECT) {//直连模式，直接mqtt连接.
            mqttConnect();
        } else if (mTCMqttConfig.getConnectionMode() == TCMqttConfig.TCMqttConnectionMode.MODE_TOKEN) {//token模式，先请求token，再mqtt连接.
            TokenHelper tokenHelper = new TokenHelper(mTCMqttConfig.getRegion(), mTCMqttConfig.getProductId(), mTCMqttConfig.getDeviceName(), mTCMqttConfig.getDeviceSecret());
            tokenHelper.getToken(mMqttClientId, new TokenHelper.ITokenListener() {
                @Override
                public void onSuccessed(String userName, String password) {
                    mTCMqttConfig.setMqttUserName(userName)
                            .setMqttPassword(password);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mqttConnect();
                        }
                    });
                }

                @Override
                public void onFailed(String message) {
                    QLog.e(TAG, "get token failed, " + message);
                    onConnectFailed(new Throwable("get token failed, " + message));
                }
            });
        }
    }

    private void mqttConnect() {
        String mqttBrokerURL = "tcp://" + mTCMqttConfig.getMqttHost() + ":" + TCConstants.MQTT_PORT;
        try {
            if (mMqttClient == null) {
                mMqttClient = new MqttAsyncClient(mqttBrokerURL, mMqttClientId, new MemoryPersistence());
            }
            QLog.d(TAG, "url = " + mqttBrokerURL + ", clientid = " + mMqttClientId);
            SSLSocketFactory socketFactory = null;
            if (mTCMqttConfig.getKeyStore() != null) {
                socketFactory = TCTLSSocketFactory.getSocketFactory(mTCMqttConfig.getKeyStore());
            }
            mMqttConnectOptions = buildMqttConnectOptions(socketFactory);
            mMqttClient.setCallback(mMqttCallback);
            mMqttClient.connect(mMqttConnectOptions, null, mMqttConnectActionListener);

        } catch (MqttException e) {
            QLog.d(TAG, "connect exception", e);
            switch (e.getReasonCode()) {
                case MqttException.REASON_CODE_CLIENT_CONNECTED:
                    setConnectStateAndNotify(MqttConnectState.CONNECTED);
                    break;
                case MqttException.REASON_CODE_CONNECT_IN_PROGRESS:
                    setConnectStateAndNotify(MqttConnectState.CONNECTING);
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
        mMqttClient = null;
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
        //TODO: need setMqttVersion?
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
            QLog.d(TAG, "messageArrived: " + message + ", clientId = " + mMqttClientId + ", topic = " + topic);
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
    private IMqttActionListener mMqttConnectActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            QLog.d(TAG, "connect successed, clientId = " + mMqttClientId);
            mReconnectHelper.reset();
            setConnectStateAndNotify(MqttConnectState.CONNECTED);
            reSubscribeFromQueue();
            sendRequestFromQueueIfConnected();
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            QLog.d(TAG, "connect failed, exception: " + (exception != null ? exception : "") + ", clientId = " + mMqttClientId);
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
            if (request instanceof MqttPublishRequest) {
                MqttPublishRequest publishRequest = (MqttPublishRequest) request;
                if (publishRequest.getCallback() != null) {
                    publishRequest.getCallback().onFailure(new Throwable("disconnected"));
                }
            } else if (request instanceof MqttSubscribeRequest) {
                MqttSubscribeRequest subscribeRequest = (MqttSubscribeRequest) request;
                if (subscribeRequest.getCallback() != null) {
                    subscribeRequest.getCallback().onFailure(new Throwable("disconnected"));
                }
            } else if (request instanceof MqttUnSubscribeRequest) {
                MqttUnSubscribeRequest unSubscribeRequest = (MqttUnSubscribeRequest) request;
                if (unSubscribeRequest.getCallback() != null) {
                    unSubscribeRequest.getCallback().onFailure(new Throwable("disconnected"));
                }
            }
            return;
        }
        mMqttRequestQueue.add(request);
        sendRequestFromQueueIfConnected();
    }

    /**
     * 连接成功时，处理队列中的待发送请求
     */
    private synchronized void sendRequestFromQueueIfConnected() {
        if (mConnectState != MqttConnectState.CONNECTED) {
            QLog.d(TAG, "cannot send request now, will send after connect to server");
            return;
        }
        BaseMqttRequest request;
        while ((request = mMqttRequestQueue.poll()) != null) {
            if (request instanceof MqttPublishRequest) {
                MqttPublishRequest publishRequest = (MqttPublishRequest) request;
                mqttPublish(publishRequest.getMsg().getBytes(StringUtil.UTF8), publishRequest.getTopic(), publishRequest.getQos(), publishRequest.getCallback());
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
            mMqttClient.publish(topic, message, userContext, mPubSubListener);
        } catch (MqttException e) {
            if (callback != null) {
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
            mMqttClient.subscribe(topic, qos.asInt(), userContext, mPubSubListener);
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
            mMqttClient.unsubscribe(topic, userContext, mPubSubListener);
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
    private IMqttActionListener mPubSubListener = new IMqttActionListener() {
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
                QLog.d(TAG, "on action fail, type = " + userContext.getActionType() + ", topic = " + userContext.getTopic());
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
