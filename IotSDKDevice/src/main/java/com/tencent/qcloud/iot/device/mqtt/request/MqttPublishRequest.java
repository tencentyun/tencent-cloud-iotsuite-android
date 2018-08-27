package com.tencent.qcloud.iot.device.mqtt.request;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.tencent.qcloud.iot.device.mqtt.constant.TCIotMqttQos;
import com.tencent.qcloud.iot.utils.StringUtil;

/**
 * Created by rongerwu on 2018/1/12.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class MqttPublishRequest extends BaseMqttRequest<MqttPublishRequest> {
    private byte[] mMsg = new byte[0];
    private String mTopic = "topic";
    private TCIotMqttQos mQos = TCIotMqttQos.QOS0;

    public byte[] getMsg() {
        return mMsg;
    }

    @NonNull
    public MqttPublishRequest setMsg(final String msg) {
        if (TextUtils.isEmpty(msg)) {
            throw new IllegalArgumentException("msg cannot be empty");
        }
        mMsg = msg.getBytes(StringUtil.UTF8);
        return this;
    }

    @NonNull
    public MqttPublishRequest setMsg(final byte[] msg) {
        if (msg == null) {
            throw new IllegalArgumentException("msg cannot be empty");
        }
        mMsg = msg;
        return this;
    }

    public String getTopic() {
        return mTopic;
    }

    @NonNull
    public MqttPublishRequest setTopic(final String topic) {
        if (TextUtils.isEmpty(topic)) {
            throw new IllegalArgumentException("topic cannot be empty");
        }
        mTopic = topic;
        return this;
    }

    public TCIotMqttQos getQos() {
        return mQos;
    }

    public MqttPublishRequest setQos(final TCIotMqttQos qos) {
        mQos = qos;
        return this;
    }
}
