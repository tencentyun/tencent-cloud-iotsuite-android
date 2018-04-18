package com.tencent.qcloud.iot.mqtt.shadow;

import com.tencent.qcloud.iot.common.QLog;
import com.tencent.qcloud.iot.mqtt.QCloudIotMqttService;
import com.tencent.qcloud.iot.mqtt.callback.IMqttActionCallback;
import com.tencent.qcloud.iot.mqtt.constant.QCloudIotMqttQos;
import com.tencent.qcloud.iot.mqtt.request.MqttPublishRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rongerwu on 2018/1/30.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

/**
 * 影子管理类
 * 为了避免用户外部调用connect和disconnect导致ShadowManager数据被清空，设计为单例。
 */
public class ShadowManager {
    private static final String SHADOW_JSON_KEY_METHOD = "method";
    private static final String SHADOW_JSON_KEY_STATE = "state";
    private static final String SHADOW_JSON_KEY_METADATE = "metadata";
    private static final String SHADOW_JSON_KEY_PAYLOAD = "payload";
    private static final String SHADOW_JSON_KEY_STATUS = "status";
    private static final String SHADOW_JSON_KEY_CODE = "code";
    private static final String SHADOW_JSON_KEY_VERSION = "version";
    private static final String SHADOW_JSON_KEY_TIMESTAMP = "timestamp";
    private static final String SHADOW_JSON_KEY_PASSTHROUGH = "passthrough";

    private static final String SHADOW_JSON_KEY_REPORTED = "reported";
    private static final String SHADOW_JSON_KEY_DESIRED = "desired";

    private static final String SHADOW_METHOD_UPDATE = "update";
    private static final String SHADOW_METHOD_GET = "get";
    private static final String SHADOW_METHOD_DELETE = "delete";
    private static final String SHADOW_METHOD_REPLY = "reply";
    private static final String SHADOW_METHOD_CONTROL = "control";

    /**
     * 上报固件信息，设备主动上报
     */
    private static final String SHADOW_METHOD_UPDATE_FIRM_INFO = "update_firm_info";

    /**
     * 上报固件信息，服务端下发指令要求客户端上报
     */
    private static final String SHADOW_METHOD_REPORT_FIRM_INFO = "report_firm_info";

    public static final int RTCODE_OK = 0;
    public static final int RTCODE_BAD_SHADOW_MSG = 400;
    public static final int RTCODE_SHADOW_MSG_MISSING_METHOD = 401;
    public static final int RTCODE_SHADOW_MISSING_STAT = 402;
    public static final int RTCODE_UNSUPPORTED_SHADOW_METHOD = 403;
    public static final int RTCODE_SHADOW_BAD_STAT = 404;
    public static final int RTCODE_TOO_MANY_SHADOW_ATTR = 405;
    public static final int RTCODE_SHADOW_INTERNAL_ERROR = 500;

    private static final String TAG = ShadowManager.class.getSimpleName();
    private static ShadowManager mInstance;
    private QCloudIotMqttService mQCloudIotMqttService;
    private ShadowTopicHelper mShadowTopicHelper;
    private DeviceDataProcessor mDeviceDataProcessor;

    public static ShadowManager getInstance(QCloudIotMqttService mqttService, ShadowTopicHelper shadowTopicHelper) {
        if (mqttService == null || shadowTopicHelper == null) {
            throw new IllegalArgumentException("mqttService and shadowTopicHelper cannot be null");
        }
        if (mInstance == null) {
            mInstance = new ShadowManager(shadowTopicHelper);
        }
        mInstance.setQCloudIotMqttService(mqttService);
        return mInstance;
    }

    private ShadowManager(ShadowTopicHelper shadowTopicHelper) {
        mShadowTopicHelper = shadowTopicHelper;
        mDeviceDataProcessor = new DeviceDataProcessor(this);
    }

    private ShadowManager setQCloudIotMqttService(QCloudIotMqttService QCloudIotMqttService) {
        mQCloudIotMqttService = QCloudIotMqttService;
        return this;
    }

    public void getShadow() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(SHADOW_JSON_KEY_METHOD, SHADOW_METHOD_GET);

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

        publishShadowRequest(jsonObject.toString());
    }

    public void deleteShadow(JSONObject delete) throws JSONException {
        JSONObject jsonState = new JSONObject();
        jsonState.put(SHADOW_JSON_KEY_REPORTED, (delete != null) ? delete : JSONObject.NULL);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(SHADOW_JSON_KEY_METHOD, SHADOW_METHOD_DELETE);
        jsonObject.put(SHADOW_JSON_KEY_STATE, jsonState);

        publishShadowRequest(jsonObject.toString());
    }

    private void publishShadowRequest(String message) {
        QLog.d(TAG, "publishShadowRequest " + message);
        MqttPublishRequest request = new MqttPublishRequest()
                .setTopic(mShadowTopicHelper.getUpdateTopic())
                .setQos(QCloudIotMqttQos.QOS1)
                .setMsg(message)
                .setCallback(new IMqttActionCallback() {
                    @Override
                    public void onSuccess() {
                        //QLog.d(TAG, "publishShadowRequest successed");
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        QLog.e(TAG, "publishShadowRequest failed", exception);
                    }
                });
        mQCloudIotMqttService.publish(request);
    }

    public void parseShadowMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            String method = jsonObject.getString(SHADOW_JSON_KEY_METHOD);
            switch (method) {
                case SHADOW_METHOD_REPLY:
                    onReply(jsonObject);
                    break;
                case SHADOW_METHOD_CONTROL:
                    onControl(jsonObject);
                    break;
                default:
                    QLog.e(TAG, "parse shadow message, illegal method in message " + message);
                    break;
            }
        } catch (JSONException e) {
            QLog.e(TAG, "parseShadowMessage", e);
        }
    }

    /**
     * 当发出消息后收到答复时触发
     *
     * @param jsonObject
     * @throws JSONException
     */
    private synchronized void onReply(JSONObject jsonObject) throws JSONException {
        long timeStamp = jsonObject.getLong(SHADOW_JSON_KEY_TIMESTAMP);
        JSONObject payload = jsonObject.getJSONObject(SHADOW_JSON_KEY_PAYLOAD);
        int code = payload.getInt(SHADOW_JSON_KEY_CODE);
        String status = payload.getString(SHADOW_JSON_KEY_STATUS);
        JSONObject state = payload.optJSONObject(SHADOW_JSON_KEY_STATE);
        JSONObject metadata = payload.optJSONObject(SHADOW_JSON_KEY_METADATE);
        if (code == RTCODE_OK) {
            QLog.i(TAG, "on shadow reply, success");
            if (state != null) {
                // get shadow success
                JSONObject desired = state.getJSONObject(SHADOW_JSON_KEY_DESIRED);
                JSONObject reported = state.getJSONObject(SHADOW_JSON_KEY_REPORTED);
                mDeviceDataProcessor.processDesiredForInit(desired);
            } else {
                // report or delete success
            }
        } else {
            QLog.e(TAG, "on shadow reply, error, code = " + code + ", status = " + status);
        }
    }

    /**
     * 当收到控制消息用以改变设备状态时触发
     *
     * @param jsonObject
     * @throws JSONException
     */
    private synchronized void onControl(JSONObject jsonObject) throws JSONException {
        //TODO:根据时间戳判断control消息是否过期
        long timeStamp = jsonObject.getLong(SHADOW_JSON_KEY_TIMESTAMP);
        JSONObject payload = jsonObject.getJSONObject(SHADOW_JSON_KEY_PAYLOAD);
        int code = payload.getInt(SHADOW_JSON_KEY_CODE);
        String status = payload.getString(SHADOW_JSON_KEY_STATUS);
        if (code == RTCODE_OK) {
            JSONObject state = payload.getJSONObject(SHADOW_JSON_KEY_STATE);
            JSONObject metadata = payload.getJSONObject(SHADOW_JSON_KEY_METADATE);
            JSONObject desired = state.getJSONObject(SHADOW_JSON_KEY_DESIRED);
            mDeviceDataProcessor.processDeisredForControl(desired);
        } else {
            QLog.e(TAG, "onControl error, message = " + jsonObject.toString());
        }
    }

    public void onLocalDataChange(JSONObject localDeviceData) {
        mDeviceDataProcessor.updateLocalDeviceData(localDeviceData);
    }

    public void onUserChangeData(JSONObject userDesired, boolean commit) {
        try {
            mDeviceDataProcessor.onUserChangeData(userDesired, commit);
        } catch (JSONException e) {
            QLog.e(TAG, "onUserChangeData", e);
        }
    }

    public void setDataEventListener(IDataEventListener dataEventListener) {
        mDeviceDataProcessor.setDataEventListener(dataEventListener);
    }
}
