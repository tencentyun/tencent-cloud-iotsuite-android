package com.tencent.qcloud.iot.mqtt.request;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.tencent.qcloud.iot.mqtt.callback.IMqttActionCallback;
import com.tencent.qcloud.iot.mqtt.constant.TCIotMqttQos;

/**
 * Created by rongerwu on 2018/1/12.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class MqttPublishRequest extends BaseMqttRequest {
    private String mMsg = "msg";
    private String mTopic = "topic";
    private TCIotMqttQos mQos = TCIotMqttQos.QOS0;
    private IMqttActionCallback mCallback;

    public String getMsg() {
        return mMsg;
    }

    @NonNull
    public MqttPublishRequest setMsg(String msg) {
        if (TextUtils.isEmpty(msg)) {
            throw new IllegalArgumentException("msg cannot be empty");
        }
        mMsg = msg;
        return this;
    }

    public String getTopic() {
        return mTopic;
    }

    @NonNull
    public MqttPublishRequest setTopic(String topic) {
        if (TextUtils.isEmpty(topic)) {
            throw new IllegalArgumentException("topic cannot be empty");
        }
        mTopic = topic;
        return this;
    }

    public TCIotMqttQos getQos() {
        return mQos;
    }

    public MqttPublishRequest setQos(TCIotMqttQos qos) {
        mQos = qos;
        return this;
    }

    public IMqttActionCallback getCallback() {
        return mCallback;
    }

    public MqttPublishRequest setCallback(IMqttActionCallback callback) {
        mCallback = callback;
        return this;
    }
}
