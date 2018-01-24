package com.tencent.qcloud.iot.mqtt.request;

import android.text.TextUtils;

import com.tencent.qcloud.iot.mqtt.callback.IMqttActionCallback;
import com.tencent.qcloud.iot.mqtt.constant.QCloudIotMqttQos;

/**
 * Created by rongerwu on 2018/1/12.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class MqttSubscribeRequest extends BaseMqttRequest {
    private String mTopic = "topic";
    private QCloudIotMqttQos mQos = QCloudIotMqttQos.QOS0;
    private IMqttActionCallback mCallback;

    public String getTopic() {
        return mTopic;
    }

    public MqttSubscribeRequest setTopic(String topic) {
        if (TextUtils.isEmpty(topic)) {
            throw new IllegalArgumentException("topic cannot be empty");
        }
        mTopic = topic;
        return this;
    }

    public QCloudIotMqttQos getQos() {
        return mQos;
    }

    public MqttSubscribeRequest setQos(QCloudIotMqttQos qos) {
        mQos = qos;
        return this;
    }

    public IMqttActionCallback getCallback() {
        return mCallback;
    }

    public MqttSubscribeRequest setCallback(IMqttActionCallback callback) {
        mCallback = callback;
        return this;
    }
}
