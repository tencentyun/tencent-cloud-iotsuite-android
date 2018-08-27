package com.tencent.qcloud.iot.sample.model;

import com.tencent.qcloud.iot.device.mqtt.constant.TCIotMqttQos;

/**
 * Created by rongerwu on 2018/1/15.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class Subscribe {
    private String mTopic;
    private TCIotMqttQos mQos;

    public String getTopic() {
        return mTopic;
    }

    public Subscribe setTopic(String topic) {
        mTopic = topic;
        return this;
    }

    public TCIotMqttQos getQos() {
        return mQos;
    }

    public Subscribe setQos(TCIotMqttQos qos) {
        mQos = qos;
        return this;
    }

    public boolean isSuccessed() {
        return mSuccessed;
    }

    public Subscribe setSuccessed(boolean successed) {
        mSuccessed = successed;
        return this;
    }

    private boolean mSuccessed;
}
