package com.tencent.qcloud.iot.device.data;

import com.tencent.qcloud.iot.log.QLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rongerwu on 2018/7/12.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class OtaHandler {
    public static final String OTA_JSON_KEY_METHOD = "method";
    public static final String OTA_JSON_KEY_TIMESTAMP = "timestamp";
    public static final String OTA_JSON_KEY_PAYLOAD = "payload";
    public static final String OTA_JSON_KEY_CODE = "code";
    public static final String OTA_JSON_KEY_STATUS = "status";
    public static final String OTA_METHOD_REPLY = "reply";
    public static final int RTCODE_OK = 0;

    private static final String TAG = OtaHandler.class.getSimpleName();

    public void parseMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            String method = jsonObject.getString(OTA_JSON_KEY_METHOD);
            switch (method) {
                case OTA_METHOD_REPLY:
                    onReply(jsonObject);
                    break;
                default:
                    QLog.e(TAG, "parse ota message, illegal method in message " + message);
                    break;
            }
        } catch (JSONException e) {
            QLog.e(TAG, "parseMessage", e);
        }
    }

    private synchronized void onReply(JSONObject jsonObject) throws JSONException {
        JSONObject payload = jsonObject.getJSONObject(OTA_JSON_KEY_PAYLOAD);
        int code = payload.getInt(OTA_JSON_KEY_CODE);
        String status = payload.getString(OTA_JSON_KEY_STATUS);
        if (code == RTCODE_OK) {
            QLog.i(TAG, "on ota reply, success");
        } else {
            QLog.w(TAG, "on ota reply, error, code = " + code + ", status = " + status);
        }
    }
}
