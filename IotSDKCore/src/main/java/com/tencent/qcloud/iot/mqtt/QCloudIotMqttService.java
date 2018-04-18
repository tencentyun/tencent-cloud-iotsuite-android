package com.tencent.qcloud.iot.mqtt;

import com.tencent.qcloud.iot.common.QLog;
import com.tencent.qcloud.iot.mqtt.callback.IMqttActionCallback;
import com.tencent.qcloud.iot.mqtt.callback.IMqttConnectStateCallback;
import com.tencent.qcloud.iot.mqtt.callback.IMqttMessageListener;
import com.tencent.qcloud.iot.mqtt.constant.MqttConnectState;
import com.tencent.qcloud.iot.mqtt.constant.QCloudIotMqttQos;
import com.tencent.qcloud.iot.mqtt.request.MqttPublishRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttSubscribeRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttUnSubscribeRequest;
import com.tencent.qcloud.iot.mqtt.shadow.IDataEventListener;
import com.tencent.qcloud.iot.mqtt.shadow.ShadowManager;
import com.tencent.qcloud.iot.mqtt.shadow.ShadowTopicHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rongerwu on 2018/1/30.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class QCloudIotMqttService {
    private static final String TAG = QCloudIotMqttService.class.getSimpleName();
    private QCloudIotMqttClient mQCloudIotMqttClient;
    private ShadowManager mShadowManager;
    private ShadowTopicHelper mShadowTopicHelper;
    private IMqttMessageListener mMqttMessageListener;

    public QCloudIotMqttService(QCloudMqttConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null!");
        }
        mQCloudIotMqttClient = new QCloudIotMqttClient(config);
        mShadowTopicHelper = new ShadowTopicHelper(config.getProductId(), config.getDeviceName());
        mShadowManager = ShadowManager.getInstance(this, mShadowTopicHelper);
    }

    public void setMqttMessageListener(IMqttMessageListener mqttMessageListener) {
        mMqttMessageListener = mqttMessageListener;
    }

    /**
     * 建立mqtt连接
     *
     * @param connectStateCallback 连接状态回调
     */
    public void connect(final IMqttConnectStateCallback connectStateCallback) {
        mQCloudIotMqttClient.connect(new IMqttConnectStateCallback() {
            @Override
            public void onStateChanged(MqttConnectState state) {
                if (state == MqttConnectState.CONNECTED) {
                    //connect后getShadow以得到desired，用来初始化
                    try {
                        mShadowManager.getShadow();
                    } catch (JSONException e) {
                        QLog.e(TAG, "getShadow after connect", e);
                    }
                }
                if (connectStateCallback != null) {
                    connectStateCallback.onStateChanged(state);
                }
            }
        });

        mQCloudIotMqttClient.setMqttMessageListener(new IMqttMessageListener() {
            @Override
            public void onMessageArrived(String topic, String message) {
                //影子消息在内部处理
                if (topic.equals(mShadowTopicHelper.getGetTopic())) {
                    onShadowMessageArrived(message);
                } else {
                    if (mMqttMessageListener != null) {
                        mMqttMessageListener.onMessageArrived(topic, message);
                    }
                }
            }
        });
        subscribeShadowMessage();
    }

    /**
     * 断开mqtt连接
     */
    public void disconnect() {
        mQCloudIotMqttClient.disconnect();
    }

    /**
     * 向topic发布消息，必须在connect调用之后，否则无效。
     *
     * @param request 请求
     */
    public void publish(final MqttPublishRequest request) {
        mQCloudIotMqttClient.publish(request);
    }

    /**
     * 订阅topic，必须在connect调用之后，否则无效。
     *
     * @param request 请求
     */
    public void subscribe(final MqttSubscribeRequest request) {
        mQCloudIotMqttClient.subscribe(request);
    }

    /**
     * 取消订阅topic，必须在connect调用之后，否则无效。
     *
     * @param request 请求
     */
    public void unSubscribe(final MqttUnSubscribeRequest request) {
        mQCloudIotMqttClient.unSubscribe(request);
    }

    private void onShadowMessageArrived(String message) {
        mShadowManager.parseShadowMessage(message);
    }

    /**
     * 订阅shadow消息
     */
    private void subscribeShadowMessage() {
        MqttSubscribeRequest request = new MqttSubscribeRequest()
                .setTopic(mShadowTopicHelper.getGetTopic())
                .setQos(QCloudIotMqttQos.QOS1)
                .setCallback(new IMqttActionCallback() {
                    @Override
                    public void onSuccess() {
                        //QLog.d(TAG, "setShadowMessageListener successed");
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        QLog.e(TAG, "setShadowMessageListener failed", exception);
                    }
                });
        subscribe(request);
    }

    public void onLocalDataChange(JSONObject localDeviceData) {
        mShadowManager.onLocalDataChange(localDeviceData);
    }

    public void onUserChangeData(JSONObject userDesired, boolean commit) {
        mShadowManager.onUserChangeData(userDesired, commit);
    }

    /**
     * 监听来自服务端的控制消息
     * @param dataEventListener
     */
    public void setDataEventListener(IDataEventListener dataEventListener) {
        mShadowManager.setDataEventListener(dataEventListener);
    }
}
