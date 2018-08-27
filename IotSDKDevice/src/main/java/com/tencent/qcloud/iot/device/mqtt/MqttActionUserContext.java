package com.tencent.qcloud.iot.device.mqtt;

import android.text.TextUtils;

import com.tencent.qcloud.iot.device.mqtt.callback.IMqttActionCallback;

/**
 * Created by rongerwu on 2018/1/14.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

/**
 * mqtt发布或订阅请求时的用户context，用于跟踪请求结果.
 */
public class MqttActionUserContext {
    public static final String ACTION_TYPE_PUBLISH = "publish";
    public static final String ACTION_TYPE_SUBSCRIBE = "subscribe";
    public static final String ACTION_TYPE_UNSUBSCRIBE = "unsubscribe";

    private IMqttActionCallback mCallback;
    private String mActionType = "unknown";
    private String mTopic = "unknown";

    public IMqttActionCallback getCallback() {
        return mCallback;
    }

    public MqttActionUserContext setCallback(IMqttActionCallback callback) {
        this.mCallback = callback;
        return this;
    }

    public String getActionType() {
        return mActionType;
    }

    public MqttActionUserContext setActionType(String actionType) {
        if (TextUtils.isEmpty(actionType)) {
            throw new IllegalArgumentException("actionType is empty");
        }
        mActionType = actionType;
        return this;
    }

    public String getTopic() {
        return mTopic;
    }

    public MqttActionUserContext setTopic(String topic) {
        if (TextUtils.isEmpty(topic)) {
            throw new IllegalArgumentException("topic is empty");
        }
        mTopic = topic;
        return this;
    }
}
