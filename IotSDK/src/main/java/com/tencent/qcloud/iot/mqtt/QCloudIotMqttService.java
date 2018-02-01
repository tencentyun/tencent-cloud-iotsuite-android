package com.tencent.qcloud.iot.mqtt;

import com.tencent.qcloud.iot.common.QLog;
import com.tencent.qcloud.iot.mqtt.callback.IMqttConnectStateCallback;
import com.tencent.qcloud.iot.mqtt.callback.IMqttMessageListener;
import com.tencent.qcloud.iot.mqtt.request.MqttPublishRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttSubscribeRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttUnSubscribeRequest;
import com.tencent.qcloud.iot.mqtt.shadow.IShadowListener;
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
        mShadowManager = new ShadowManager(this, mShadowTopicHelper);
    }

    public void setMqttMessageListener(IMqttMessageListener mqttMessageListener) {
        mMqttMessageListener = mqttMessageListener;
    }

    /**
     * 建立mqtt连接
     *
     * @param connectStateCallback
     */
    public void connect(final IMqttConnectStateCallback connectStateCallback) {
        mQCloudIotMqttClient.connect(connectStateCallback);

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
     * @param request
     */
    public void publish(final MqttPublishRequest request) {
        mQCloudIotMqttClient.publish(request);
    }

    /**
     * 订阅topic，必须在connect调用之后，否则无效。
     *
     * @param request
     */
    public void subscribe(final MqttSubscribeRequest request) {
        mQCloudIotMqttClient.subscribe(request);
    }

    /**
     * 取消订阅topic，必须在connect调用之后，否则无效。
     *
     * @param request
     */
    public void unSubscribe(final MqttUnSubscribeRequest request) {
        mQCloudIotMqttClient.unSubscribe(request);
    }

    /**
     * 设置监听影子消息，必须在connect调用之后，否则无效。
     */
    public void setShadowMessageListener(IShadowListener shadowListener) {
        mShadowManager.setShadowMessageListener(shadowListener);
    }

    /**
     * 获取影子。
     * 异步操作，成功后触发 IShadowListener.onGetShadow
     */
    public void getShadow() {
        try {
            mShadowManager.getShadow();
        } catch (JSONException e) {
            QLog.e(TAG, "getShadow", e);
        }
    }

    /**
     * APP端操作影子，进而间接操作设备。
     * 备注：目前未规划通过mqtt发desired update设备，该接口暂不对外。
     *
     * @param desire 要操作的设备属性的json文档
     */
    private void desireShadow(JSONObject desire) {
        try {
            mShadowManager.desireShadow(desire);
        } catch (JSONException e) {
            QLog.e(TAG, "desireShadow", e);
        }
    }

    /**
     * 设备端发出请求，汇报自身状态属性用于更新影子
     *
     * @param report 要report的属性的json文档
     */
    public void reportShadow(JSONObject report) {
        try {
            mShadowManager.reportShadow(report);
        } catch (JSONException e) {
            QLog.e(TAG, "reportShadow", e);
        }
    }

    /**
     * 设备端发出请求，删除影子的某个属性或全部属性。
     *
     * @param delete
     * null表示删除影子的所有属性，否则只删除delete json对象中值为null的属性
     */
    public void deleteShadow(JSONObject delete) {
        try {
            mShadowManager.deleteShadow(delete);
        } catch (JSONException e) {
            QLog.e(TAG, "deleteShadow", e);
        }
    }

    private void onShadowMessageArrived(String message) {
        mShadowManager.parseShadowMessage(message);
    }
}
