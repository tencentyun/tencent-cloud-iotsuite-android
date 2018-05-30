package com.tencent.qcloud.iot.mqtt.request;

import android.text.TextUtils;

import com.tencent.qcloud.iot.mqtt.constant.TCIotMqttQos;

/**
 * Created by rongerwu on 2018/1/12.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class MqttSubscribeRequest extends BaseMqttRequest<MqttSubscribeRequest> {
    private String mTopic = "topic";
    private TCIotMqttQos mQos = TCIotMqttQos.QOS0;

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

    public TCIotMqttQos getQos() {
        return mQos;
    }

    public MqttSubscribeRequest setQos(TCIotMqttQos qos) {
        mQos = qos;
        return this;
    }
}
