package com.tencent.qcloud.iot.sample;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.tencent.qcloud.iot.common.QLog;
import com.tencent.qcloud.iot.mqtt.QCloudIotMqttService;
import com.tencent.qcloud.iot.mqtt.QCloudMqttConfig;
import com.tencent.qcloud.iot.mqtt.QCloudMqttConfig.QCloudMqttConnectionMode;
import com.tencent.qcloud.iot.mqtt.callback.IMqttActionCallback;
import com.tencent.qcloud.iot.mqtt.callback.IMqttConnectStateCallback;
import com.tencent.qcloud.iot.mqtt.callback.IMqttMessageListener;
import com.tencent.qcloud.iot.mqtt.constant.MqttConnectState;
import com.tencent.qcloud.iot.mqtt.constant.QCloudIotMqttQos;
import com.tencent.qcloud.iot.mqtt.request.MqttPublishRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttSubscribeRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttUnSubscribeRequest;
import com.tencent.qcloud.iot.mqtt.shadow.IShadowListener;
import com.tencent.qcloud.iot.sample.model.Subscribe;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rongerwu on 2018/1/16.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class Connection implements Parcelable {

    private static final String TAG = Connection.class.getSimpleName();
    private QCloudIotMqttService mQCloudIotMqttService;
    private QCloudMqttConfig mQCloudMqttConfig;
    private Map<String, Subscribe> mSubscribesMap;

    /**
     * mSubscribeStateListener: 当订阅状态改变时，通知外部修改UI
     */
    private ISubscribeStateListener mSubscribeStateListener;

    /**
     * mConnectionStateListener: 当connect状态改变时，通知外部修改UI
     */
    private IConnectionStateListener mConnectionStateListener;

    /**
     * mMessageNotifyListener: 用于将想要对外展示的消息发出给Activity
     */
    private IMessageNotifyListener mMessageNotifyListener;

    public Connection() {
        mSubscribesMap = new HashMap<>();
        QLog.setLogLevel(QLog.QLOG_LEVEL_DEBUG);
    }

    protected Connection(Parcel in) {
    }

    public static final Creator<Connection> CREATOR = new Creator<Connection>() {
        @Override
        public Connection createFromParcel(Parcel in) {
            return new Connection(in);
        }

        @Override
        public Connection[] newArray(int size) {
            return new Connection[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    /**
     * mqtt直连模式
     *
     * @param mqttHost
     * @param productKey
     * @param productId
     * @param deviceName
     * @param deviceSecret
     * @param userName
     * @param password
     */
    public void connectDirectMode(String mqttHost, String productKey, String productId, String deviceName, String deviceSecret, String userName, String password) {
        if (mQCloudIotMqttService != null) {
            mQCloudIotMqttService.disconnect();
        }
        //mqtt参数配置
        mQCloudMqttConfig = new QCloudMqttConfig(mqttHost, productKey, deviceName, deviceSecret)
                .setProductId(productId)
                .setMqttUserName(userName)
                .setMqttPassword(password)
                .setConnectionMode(QCloudMqttConnectionMode.MODE_DIRECT)
                .setAutoReconnect(true)
                .setMinRetryTimeMs(1000)
                .setMaxRetryTimeMs(20000)
                .setMaxRetryTimes(5000);
        connect(mQCloudMqttConfig);
    }

    /**
     * token连接模式
     *
     * @param mqttHost
     * @param productKey
     * @param productId
     * @param deviceName
     * @param deviceSecret
     */
    public void connectTokenMode(String mqttHost, String productKey, String productId, String deviceName, String deviceSecret) {
        if (mQCloudIotMqttService != null) {
            mQCloudIotMqttService.disconnect();
        }
        //mqtt参数配置
        mQCloudMqttConfig = new QCloudMqttConfig(mqttHost, productKey, deviceName, deviceSecret)
                .setProductId(productId)
                .setConnectionMode(QCloudMqttConnectionMode.MODE_TOKEN)
                .setAutoReconnect(true)
                .setMinRetryTimeMs(1000)
                .setMaxRetryTimeMs(20000)
                .setMaxRetryTimes(5000);
        connect(mQCloudMqttConfig);
    }

    private void connect(QCloudMqttConfig config) {
        mQCloudIotMqttService = new QCloudIotMqttService(config);
        //建立mqtt连接并监听连接结果
        mQCloudIotMqttService.connect(new IMqttConnectStateCallback() {
            @Override
            public void onStateChanged(MqttConnectState state) {
                notifyMessage("onStateChanged: " + state);
                if (mConnectionStateListener != null) {
                    if (state == MqttConnectState.CONNECTING
                            || state == MqttConnectState.PRE_RECONNECT
                            || state == MqttConnectState.RECONNECTING) {
                        mConnectionStateListener.onConnecting();
                    } else if (state == MqttConnectState.CONNECTED) {
                        mConnectionStateListener.onSuccess();
                    } else if (state == MqttConnectState.CLOSED) {
                        mConnectionStateListener.onFailure();
                    } else {
                        Log.e(TAG, "error connection state, state = " + state);
                    }
                }
            }
        });
        //设置监听来自已订阅topic的消息
        mQCloudIotMqttService.setMqttMessageListener(mMqttMessageListener);
        //TODO:创建影子成功后？
        //设置监听影子消息，connect后调用
        mQCloudIotMqttService.setShadowMessageListener(mShadowListener);
    }

    /**
     * 断开mqtt连接
     */
    public void disconnect() {
        if (mQCloudIotMqttService == null) {
            return;
        }
        mQCloudIotMqttService.disconnect();
        mSubscribesMap.clear();
        onSubscribeStateChanged();
    }

    /**
     * 发布消息到topic
     *
     * @param topic
     * @param msg
     */
    public void publish(String topic, String msg) {
        if (mQCloudIotMqttService == null || mQCloudMqttConfig == null) {
            return;
        }
        //根据规则拼接得到topic
        final String fullTopic = mQCloudMqttConfig.getProductId() + "/" + mQCloudMqttConfig.getDeviceName() + "/" + topic;
        //封装publish请求
        MqttPublishRequest request = new MqttPublishRequest()
                .setTopic(fullTopic)
                .setQos(QCloudIotMqttQos.QOS0)
                .setMsg(msg)
                .setCallback(new IMqttActionCallback() {
                    @Override
                    public void onSuccess() {
                        notifyMessage("publish successed");
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        notifyMessage("publish failed");
                    }
                });
        mQCloudIotMqttService.publish(request);
    }

    /**
     * 订阅topic
     *
     * @param topic
     */
    public void subscribe(final String topic) {
        if (mQCloudIotMqttService == null || mQCloudMqttConfig == null) {
            return;
        }
        //根据规则拼接得到topic
        final String fullTopic = mQCloudMqttConfig.getProductId() + "/" + mQCloudMqttConfig.getDeviceName() + "/" + topic;
        if (!mSubscribesMap.containsKey(fullTopic)) {
            Subscribe newSubscribe = new Subscribe()
                    .setTopic(fullTopic)
                    .setQos(QCloudIotMqttQos.QOS0)
                    .setSuccessed(false);
            mSubscribesMap.put(fullTopic, newSubscribe);
        }

        onSubscribeStateChanged();

        Subscribe subscribe = mSubscribesMap.get(fullTopic);
        if (subscribe.isSuccessed()) {
            Log.w(TAG, "topic has subscribed already! topic = " + fullTopic);
            return;
        }

        //封装subscribe请求
        MqttSubscribeRequest request = new MqttSubscribeRequest()
                .setTopic(fullTopic)
                .setQos(subscribe.getQos())
                .setCallback(new IMqttActionCallback() {
                    @Override
                    public void onSuccess() {
                        notifyMessage("subscribe success, topic = " + fullTopic);
                        if (mSubscribesMap.get(fullTopic) == null) {
                            Log.e(TAG, "subscribe success, but topic no found from map, topic = " + fullTopic);
                            return;
                        }
                        mSubscribesMap.get(fullTopic).setSuccessed(true);
                        onSubscribeStateChanged();
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        notifyMessage("subscribe failed, topic = " + fullTopic);
                        onSubscribeStateChanged();
                    }
                });
        mQCloudIotMqttService.subscribe(request);
    }

    /**
     * 取消订阅topic
     *
     * @param topic
     */
    public void unsubscribe(final String topic) {
        if (mQCloudIotMqttService == null) {
            return;
        }
        mSubscribesMap.remove(topic);
        MqttUnSubscribeRequest request = new MqttUnSubscribeRequest()
                .setTopic(topic)
                .setCallback(new IMqttActionCallback() {
                    @Override
                    public void onSuccess() {
                        notifyMessage("unsubscribe successed, topic = " + topic);
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        notifyMessage("unsubscribe failed, topic = " + topic);
                    }
                });
        mQCloudIotMqttService.unSubscribe(request);
    }

    private IMqttMessageListener mMqttMessageListener = new IMqttMessageListener() {
        @Override
        public void onMessageArrived(String topic, String message) {
            notifyMessage("onMessageArrived, topic = " + topic + ", message = " + message);
        }
    };

    public ArrayList<Subscribe> getSubscribes() {
        ArrayList<Subscribe> subscribes = new ArrayList<>();
        subscribes.addAll(mSubscribesMap.values());
        return subscribes;
    }

    private void onSubscribeStateChanged() {
        if (mSubscribeStateListener != null) {
            mSubscribeStateListener.onSubscribeStateChanged();
        }
    }

    private void notifyMessage(String msg) {
        Log.d(TAG, msg);
        if (mMessageNotifyListener != null) {
            mMessageNotifyListener.onMessage(msg);
        }
    }

    public Connection setSubscribeStateListener(ISubscribeStateListener subscribeStateListener) {
        mSubscribeStateListener = subscribeStateListener;
        return this;
    }

    public Connection setConnectionStateListener(IConnectionStateListener connectionStateListener) {
        mConnectionStateListener = connectionStateListener;
        return this;
    }

    public Connection setMessageNotifyListener(IMessageNotifyListener messageNotifyListener) {
        mMessageNotifyListener = messageNotifyListener;
        return this;
    }

    public interface ISubscribeStateListener {
        void onSubscribeStateChanged();
    }

    public interface IConnectionStateListener {
        void onConnecting();

        void onSuccess();

        void onFailure();
    }

    public interface IMessageNotifyListener {
        void onMessage(String msg);
    }

    /**
     * 获取影子。
     * 异步操作，成功后触发 IShadowListener.onGetShadow
     */
    public void getShadow() {
        if (mQCloudIotMqttService == null) {
            return;
        }
        mQCloudIotMqttService.getShadow();
    }

    /**
     * 设备端发出请求，汇报自身状态属性用于更新影子
     *
     * @param report
     */
    public void reportShadow(JSONObject report) {
        if (mQCloudIotMqttService == null) {
            return;
        }
        mQCloudIotMqttService.reportShadow(report);
    }

    /**
     * 设备端发出请求，删除影子的某个属性或全部属性。
     *
     * @param delete null表示删除影子的所有属性，否则只删除delete json对象中值为null的属性
     */
    public void deleteShadow(JSONObject delete) {
        if (mQCloudIotMqttService == null) {
            return;
        }
        mQCloudIotMqttService.deleteShadow(delete);
    }

    /**
     * 监听影子消息
     */
    private IShadowListener mShadowListener = new IShadowListener() {

        @Override
        public void onReplySuccess() {
            notifyMessage("on shadow reply, success");
        }

        @Override
        public void onReplyFail(int errorCode, String status) {
            notifyMessage("on shadow reply, error, code = " + errorCode + ", status = " + status);
        }

        @Override
        public void onGetShadow(JSONObject desired, JSONObject reported) {
            notifyMessage("on get shadow, desired = " + desired + ", reported = " + reported);
        }

        @Override
        public void onControl(JSONObject desired) {
            notifyMessage("on control, desired = " + desired);
        }
    };
}
