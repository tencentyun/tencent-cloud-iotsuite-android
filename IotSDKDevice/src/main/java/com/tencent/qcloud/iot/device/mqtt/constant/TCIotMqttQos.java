package com.tencent.qcloud.iot.device.mqtt.constant;

/**
 * Created by rongerwu on 2018/1/10.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public enum TCIotMqttQos {
    QOS0,
    QOS1;

    public int asInt() {
        return (this == QOS0 ? 0 : 1);
    }
}
