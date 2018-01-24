package com.tencent.qcloud.iot.sample;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.tencent.qcloud.iot.common.QLog;
import com.tencent.qcloud.iot.mqtt.QCloudIotMqttClient;
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
import com.tencent.qcloud.iot.sample.model.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rongerwu on 2018/1/16.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class Connection implements Parcelable {

    private static final String TAG = Connection.class.getSimpleName();
    private QCloudIotMqttClient mQCloudIotMqttClient;
    private QCloudMqttConfig mQCloudMqttConfig;
    private Map<String, Subscribe> mSubscribesMap;
    private ISubscribeStateListener mSubscribeStateListener;
    private IConnectionStateListener mConnectionStateListener;
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
     * @param mqttHost
     * @param productKey
     * @param productId
     * @param deviceName
     * @param deviceSecret
     * @param userName
     * @param password
     */
    public void connectDirectMode(String mqttHost, String productKey, String productId, String deviceName, String deviceSecret, String userName, String password) {
        if (mQCloudIotMqttClient != null) {
            mQCloudIotMqttClient.disconnect();
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
                .setMaxRetryTimes(10);
        connect(mQCloudMqttConfig);
    }

    /**
     * token连接模式
     * @param mqttHost
     * @param productKey
     * @param productId
     * @param deviceName
     * @param deviceSecret
     */
    public void connectTokenMode(String mqttHost, String productKey, String productId, String deviceName, String deviceSecret) {
        if (mQCloudIotMqttClient != null) {
            mQCloudIotMqttClient.disconnect();
        }
        //mqtt参数配置
        mQCloudMqttConfig = new QCloudMqttConfig(mqttHost, productKey, deviceName, deviceSecret)
                .setProductId(productId)
                .setConnectionMode(QCloudMqttConnectionMode.MODE_TOKEN)
                .setAutoReconnect(true)
                .setMinRetryTimeMs(1000)
                .setMaxRetryTimeMs(20000)
                .setMaxRetryTimes(10);
        connect(mQCloudMqttConfig);
    }

    private void connect(QCloudMqttConfig config) {
        mQCloudIotMqttClient = new QCloudIotMqttClient(config);
        //设置监听来自已订阅topic的消息
        mQCloudIotMqttClient.setMqttMessageListener(mMqttMessageListener);
        //建立mqtt连接并监听连接结果
        mQCloudIotMqttClient.connect(new IMqttConnectStateCallback() {
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
    }

    public void disconnect() {
        if (mQCloudIotMqttClient == null) {
            return;
        }
        mQCloudIotMqttClient.disconnect();
        mSubscribesMap.clear();
        onSubscribeStateChanged();
    }

    public void publish(String topic, String msg) {
        if (mQCloudIotMqttClient == null || mQCloudMqttConfig == null) {
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
        mQCloudIotMqttClient.publish(request);
    }

    public void subscribe(final String topic) {
        if (mQCloudIotMqttClient == null || mQCloudMqttConfig == null) {
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
        mQCloudIotMqttClient.subscribe(request);
    }

    public void unsubscribe(final String topic) {
        if (mQCloudIotMqttClient == null) {
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
        mQCloudIotMqttClient.unSubscribe(request);
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
}
