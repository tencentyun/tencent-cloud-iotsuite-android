package com.tencent.qcloud.iot.sample;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.tencent.qcloud.iot.device.TCIotDeviceService;
import com.tencent.qcloud.iot.device.datatemplate.DataTemplate;
import com.tencent.qcloud.iot.device.datatemplate.JsonFileData;
import com.tencent.qcloud.iot.log.QLog;
import com.tencent.qcloud.iot.mqtt.TCMqttConfig;
import com.tencent.qcloud.iot.mqtt.callback.IMqttActionCallback;
import com.tencent.qcloud.iot.mqtt.callback.IMqttConnectStateCallback;
import com.tencent.qcloud.iot.mqtt.callback.IMqttMessageListener;
import com.tencent.qcloud.iot.mqtt.constant.MqttConnectState;
import com.tencent.qcloud.iot.mqtt.constant.TCConstants;
import com.tencent.qcloud.iot.mqtt.constant.TCIotMqttQos;
import com.tencent.qcloud.iot.mqtt.request.MqttPublishRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttSubscribeRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttUnSubscribeRequest;
import com.tencent.qcloud.iot.sample.model.Subscribe;
import com.tencent.qcloud.iot.sample.utils.ByteUtil;
import com.tencent.qcloud.iot.utils.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rongerwu on 2018/1/16.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class Connection implements Parcelable {

    private static final String TAG = Connection.class.getSimpleName();
    private TCIotDeviceService mTCIotDeviceService;
    private TCMqttConfig mTCMqttConfig;
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
     * 连接mqtt
     *
     * @param deviceName   不可以为null
     * @param deviceSecret 直连模式下可以为null, token模式下不可以为null.
     */
    public void connect(String deviceName, String deviceSecret, DataTemplate.IDataControlListener dataControlListener) {
        if (mTCIotDeviceService != null) {
            mTCIotDeviceService.disconnect();
        }
        mTCMqttConfig = TCIotDeviceService.genTCMqttConfig();
        mTCMqttConfig.setDeviceName(deviceName);
        if (deviceSecret != null) {
            mTCMqttConfig.setDeviceSecret(deviceSecret);
        }
        //设置自动重连参数
        mTCMqttConfig.setAutoReconnect(true)
                .setMinRetryTimeMs(1000)
                .setMaxRetryTimeMs(20000)
                .setMaxRetryTimes(5000);
        //请求token时默认是https，可以在此处设为Http
        //mTCMqttConfig.setTokenScheme(TCConstants.Scheme.HTTP);

        connect(mTCMqttConfig, dataControlListener);
    }

    private void connect(TCMqttConfig config, DataTemplate.IDataControlListener dataControlListener) {
        mTCIotDeviceService = new TCIotDeviceService(config);
        mTCIotDeviceService.setDataControlListener(dataControlListener);
        //建立mqtt连接并监听连接结果
        mTCIotDeviceService.connect(new IMqttConnectStateCallback() {
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
        mTCIotDeviceService.setMqttMessageListener(mMqttMessageListener);
    }

    /**
     * 断开mqtt连接
     */
    public void disconnect() {
        if (mTCIotDeviceService == null) {
            return;
        }
        mTCIotDeviceService.disconnect();
        mSubscribesMap.clear();
        onSubscribeStateChanged();
    }

    /**
     * 发布字符串消息到topic
     *
     * @param topic
     * @param msg
     */
    public void publish(String topic, String msg) {
        publish(topic, msg.getBytes(StringUtil.UTF8));
    }

    /**
     * 发布byte（可理解为二进制）消息流到topic
     *
     * @param topic
     * @param msg
     */
    public void publish(String topic, byte[] msg) {
        if (mTCIotDeviceService == null || mTCMqttConfig == null) {
            return;
        }
        //根据规则拼接得到topic
        final String fullTopic = mTCMqttConfig.getProductId() + "/" + mTCMqttConfig.getDeviceName() + "/" + topic;
        //封装publish请求
        MqttPublishRequest request = new MqttPublishRequest()
                .setTopic(fullTopic)
                .setQos(TCIotMqttQos.QOS0)
                .setMsg(msg)
                .setCallback(new IMqttActionCallback() {
                    @Override
                    public void onSuccess() {
                        notifyMessage("publish successed");
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        notifyMessage("publish failed: " + exception);
                    }
                });
        mTCIotDeviceService.publish(request);
    }

    /**
     * 订阅topic
     *
     * @param topic
     */
    public void subscribe(final String topic) {
        if (mTCIotDeviceService == null || mTCMqttConfig == null) {
            return;
        }
        //根据规则拼接得到topic
        final String fullTopic = mTCMqttConfig.getProductId() + "/" + mTCMqttConfig.getDeviceName() + "/" + topic;
        if (!mSubscribesMap.containsKey(fullTopic)) {
            Subscribe newSubscribe = new Subscribe()
                    .setTopic(fullTopic)
                    .setQos(TCIotMqttQos.QOS0)
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
        mTCIotDeviceService.subscribe(request);
    }

    /**
     * 取消订阅topic
     *
     * @param topic
     */
    public void unsubscribe(final String topic) {
        if (mTCIotDeviceService == null) {
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
        mTCIotDeviceService.unSubscribe(request);
    }

    private IMqttMessageListener mMqttMessageListener = new IMqttMessageListener() {
        @Override
        public void onMessageArrived(String topic, String message) {
            //如果是二进制数据，调用getBytes转化为byte[]
            byte[] byteMessage = message.getBytes(StringUtil.UTF8);
            notifyMessage("onMessageArrived, topic = " + topic + ", message = " + message + "\n byteMessage = " + ByteUtil.toBinaryString(byteMessage));
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

    public JsonFileData getJsonFileData() {
        if (mTCIotDeviceService == null) {
            return null;
        }
        return mTCIotDeviceService.getJsonFileData();
    }

    public DataTemplate getDataTemplate() {
        if (mTCIotDeviceService == null) {
            return null;
        }
        return mTCIotDeviceService.getDataTemplate();
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
