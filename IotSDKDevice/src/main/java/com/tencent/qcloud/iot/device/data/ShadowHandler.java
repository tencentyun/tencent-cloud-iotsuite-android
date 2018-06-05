package com.tencent.qcloud.iot.device.data;

import com.tencent.qcloud.iot.log.QLog;
import com.tencent.qcloud.iot.mqtt.shadow.ShadowManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rongerwu on 2018/4/18.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class ShadowHandler {
    public static final String SHADOW_JSON_KEY_METHOD = ShadowManager.SHADOW_JSON_KEY_METHOD;
    public static final String SHADOW_JSON_KEY_STATE = ShadowManager.SHADOW_JSON_KEY_STATE;
    public static final String SHADOW_JSON_KEY_METADATE = ShadowManager.SHADOW_JSON_KEY_METADATE;
    public static final String SHADOW_JSON_KEY_PAYLOAD = ShadowManager.SHADOW_JSON_KEY_PAYLOAD;
    public static final String SHADOW_JSON_KEY_STATUS = ShadowManager.SHADOW_JSON_KEY_STATUS;
    public static final String SHADOW_JSON_KEY_CODE = ShadowManager.SHADOW_JSON_KEY_CODE;
    public static final String SHADOW_JSON_KEY_VERSION = ShadowManager.SHADOW_JSON_KEY_VERSION;
    public static final String SHADOW_JSON_KEY_TIMESTAMP = ShadowManager.SHADOW_JSON_KEY_TIMESTAMP;
    public static final String SHADOW_JSON_KEY_PASSTHROUGH = ShadowManager.SHADOW_JSON_KEY_PASSTHROUGH;

    public static final String SHADOW_JSON_KEY_REPORTED = ShadowManager.SHADOW_JSON_KEY_REPORTED;
    public static final String SHADOW_JSON_KEY_DESIRED = ShadowManager.SHADOW_JSON_KEY_DESIRED;

    public static final String SHADOW_METHOD_REPLY = ShadowManager.SHADOW_METHOD_REPLY;
    public static final String SHADOW_METHOD_CONTROL = ShadowManager.SHADOW_METHOD_CONTROL;

    public static final int RTCODE_OK = ShadowManager.RTCODE_OK;

    private static final String TAG = ShadowHandler.class.getSimpleName();
    private DeviceDataHandler mDeviceDataHandler;

    public ShadowHandler(DeviceDataHandler deviceDataHandler) {
        mDeviceDataHandler = deviceDataHandler;
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
                mDeviceDataHandler.handleDesiredForInit(desired);
            } else {
                // report or delete success
            }
        } else {
            QLog.w(TAG, "on shadow reply, error, code = " + code + ", status = " + status);
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
            mDeviceDataHandler.handleDeisredForControl(desired, false);
        } else {
            QLog.e(TAG, "onControl error, message = " + jsonObject.toString());
        }
    }
}
