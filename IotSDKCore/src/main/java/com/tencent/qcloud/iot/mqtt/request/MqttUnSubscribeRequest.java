package com.tencent.qcloud.iot.mqtt.request;

import android.text.TextUtils;

import com.tencent.qcloud.iot.mqtt.callback.IMqttActionCallback;

/**
 * Created by rongerwu on 2018/1/14.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class MqttUnSubscribeRequest extends BaseMqttRequest {
    private String mTopic = "topic";
    private IMqttActionCallback mCallback;

    public String getTopic() {
        return mTopic;
    }

    public MqttUnSubscribeRequest setTopic(String topic) {
        if (TextUtils.isEmpty(topic)) {
            throw new IllegalArgumentException("topic cannot be empty");
        }
        mTopic = topic;
        return this;
    }

    public IMqttActionCallback getCallback() {
        return mCallback;
    }

    public MqttUnSubscribeRequest setCallback(IMqttActionCallback callback) {
        mCallback = callback;
        return this;
    }
}
