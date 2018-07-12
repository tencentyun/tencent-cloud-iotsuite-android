package com.tencent.qcloud.iot.mqtt.ota;

import com.tencent.qcloud.iot.log.QLog;
import com.tencent.qcloud.iot.mqtt.TCIotMqttClient;
import com.tencent.qcloud.iot.mqtt.TopicHelper;
import com.tencent.qcloud.iot.mqtt.callback.IMqttActionCallback;
import com.tencent.qcloud.iot.mqtt.constant.TCIotMqttQos;
import com.tencent.qcloud.iot.mqtt.request.MqttPublishRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rongerwu on 2018/7/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class OtaManager {
    private static final String TAG = OtaManager.class.getSimpleName();
    public static final String OTA_JSON_KEY_METHOD = "method";
    public static final String OTA_JSON_KEY_PAYLOAD = "payload";
    /**
     * 上报固件信息，设备主动上报
     */
    public static final String OTA_METHOD_REPORT_FIRM_INFO = "report_firm";

    private TCIotMqttClient mTCIotMqttClient;
    private TopicHelper mTopicHelper;

    public OtaManager(TCIotMqttClient mqttClient, TopicHelper topicHelper) {
        if (mqttClient == null || topicHelper == null) {
            throw new IllegalArgumentException("mqttClient and topicHelper cannot be null");
        }
        mTCIotMqttClient = mqttClient;
        mTopicHelper = topicHelper;
    }

    public void reportDeviceInfo(JSONObject deviceInfo) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(OTA_JSON_KEY_METHOD, OTA_METHOD_REPORT_FIRM_INFO);
        jsonObject.put(OTA_JSON_KEY_PAYLOAD, deviceInfo);
        publishOtaRequest(jsonObject.toString());
    }

    private void publishOtaRequest(final String message) {
        MqttPublishRequest request = new MqttPublishRequest()
                .setTopic(mTopicHelper.getOtaUpdateTopic())
                .setQos(TCIotMqttQos.QOS1)
                .setMsg(message)
                .setCallback(new IMqttActionCallback() {
                    @Override
                    public void onSuccess() {
                        QLog.d(TAG, "publishOtaRequest successed");
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        QLog.e(TAG, "publishOtaRequest failed", exception);
                    }
                });
        mTCIotMqttClient.publish(request);
    }
}
