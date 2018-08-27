package com.tencent.qcloud.iot.device.mqtt.shadow;

import com.tencent.qcloud.iot.log.QLog;
import com.tencent.qcloud.iot.device.mqtt.TCIotMqttClient;
import com.tencent.qcloud.iot.device.mqtt.TCMqttConfig;
import com.tencent.qcloud.iot.device.mqtt.TopicHelper;
import com.tencent.qcloud.iot.device.mqtt.callback.IMqttActionCallback;
import com.tencent.qcloud.iot.device.mqtt.constant.TCIotMqttQos;
import com.tencent.qcloud.iot.device.mqtt.request.MqttPublishRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rongerwu on 2018/1/30.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

/**
 * 影子管理类
 */
public class ShadowManager {
    public static final String SHADOW_JSON_KEY_METHOD = "method";
    public static final String SHADOW_JSON_KEY_STATE = "state";
    public static final String SHADOW_JSON_KEY_METADATE = "metadata";
    public static final String SHADOW_JSON_KEY_PAYLOAD = "payload";
    public static final String SHADOW_JSON_KEY_STATUS = "status";
    public static final String SHADOW_JSON_KEY_CODE = "code";
    public static final String SHADOW_JSON_KEY_VERSION = "version";
    public static final String SHADOW_JSON_KEY_TIMESTAMP = "timestamp";
    public static final String SHADOW_JSON_KEY_PASSTHROUGH = "passthrough";
    public static final String SHADOW_JSON_KEY_SEQUENCE = "sequence";

    public static final String SHADOW_JSON_KEY_REPORTED = "reported";
    public static final String SHADOW_JSON_KEY_DESIRED = "desired";

    public static final String SHADOW_METHOD_UPDATE = "update";
    public static final String SHADOW_METHOD_GET = "get";
    public static final String SHADOW_METHOD_DELETE = "delete";
    public static final String SHADOW_METHOD_REPLY = "reply";
    public static final String SHADOW_METHOD_CONTROL = "control";

    public static final int RTCODE_OK = 0;
    public static final int RTCODE_BAD_SHADOW_MSG = 400;
    public static final int RTCODE_SHADOW_MSG_MISSING_METHOD = 401;
    public static final int RTCODE_SHADOW_MISSING_STAT = 402;
    public static final int RTCODE_UNSUPPORTED_SHADOW_METHOD = 403;
    public static final int RTCODE_SHADOW_BAD_STAT = 404;
    public static final int RTCODE_TOO_MANY_SHADOW_ATTR = 405;
    public static final int RTCODE_SHADOW_INTERNAL_ERROR = 500;

    private static final String TAG = ShadowManager.class.getSimpleName();
    private TCIotMqttClient mTCIotMqttClient;
    private TCMqttConfig mTCMqttConfig;
    private TopicHelper mTopicHelper;

    public ShadowManager(TCIotMqttClient mqttClient, TCMqttConfig mqttConfig, TopicHelper topicHelper) {
        if (mqttClient == null || mqttConfig == null || topicHelper == null) {
            throw new IllegalArgumentException("params empty");
        }
        mTCIotMqttClient = mqttClient;
        mTCMqttConfig = mqttConfig;
        mTopicHelper = topicHelper;
    }

    //TODO: 服务端 get 可以支持 select 字段，用于 get 指定的数据点
    public void getShadow() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(SHADOW_JSON_KEY_METHOD, SHADOW_METHOD_GET);
        if (mTCMqttConfig.isIgnoreGetReported()) {
            jsonObject.put(SHADOW_JSON_KEY_REPORTED, false);
        }
        if (mTCMqttConfig.isIgnoreGetMetadata()) {
            jsonObject.put(SHADOW_JSON_KEY_METADATE, false);
        }

        publishShadowRequest(jsonObject.toString());
    }

    public void desireShadow(JSONObject desire) throws JSONException {
        updateShadow(desire, null);
    }

    public void reportShadow(JSONObject report) throws JSONException {
        updateShadow(null, report);
    }

    public void updateShadow(JSONObject desire, JSONObject report) throws JSONException {
        if (desire == null && report == null) {
            throw new IllegalArgumentException("desire and report is null");
        }
        JSONObject jsonState = new JSONObject();
        if (desire != null) {
            jsonState.put(SHADOW_JSON_KEY_DESIRED, desire);
        }
        if (report != null) {
            jsonState.put(SHADOW_JSON_KEY_REPORTED, report);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(SHADOW_JSON_KEY_METHOD, SHADOW_METHOD_UPDATE);
        jsonObject.put(SHADOW_JSON_KEY_STATE, jsonState);
        if (mTCMqttConfig.isIgnoreUpdateMetadata()) {
            jsonObject.put(SHADOW_JSON_KEY_METADATE, false);
        }

        publishShadowRequest(jsonObject.toString());
    }

    public void deleteShadow(JSONObject delete, int sequence) throws JSONException {
        JSONObject jsonState = new JSONObject();
        jsonState.put(SHADOW_JSON_KEY_DESIRED, (delete != null) ? delete : JSONObject.NULL);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(SHADOW_JSON_KEY_METHOD, SHADOW_METHOD_DELETE);
        jsonObject.put(SHADOW_JSON_KEY_SEQUENCE, sequence);
        jsonObject.put(SHADOW_JSON_KEY_STATE, jsonState);

        publishShadowRequest(jsonObject.toString());
    }

    private void publishShadowRequest(final String message) {
        MqttPublishRequest request = new MqttPublishRequest()
                .setTopic(mTopicHelper.getShadowUpdateTopic())
                .setQos(TCIotMqttQos.QOS1)
                .setMsg(message)
                .setCallback(new IMqttActionCallback() {
                    @Override
                    public void onSuccess() {
                        QLog.d(TAG, "publishShadowRequest successed");
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        QLog.e(TAG, "publishShadowRequest failed", exception);
                    }
                });
        mTCIotMqttClient.publish(request);
    }
}
