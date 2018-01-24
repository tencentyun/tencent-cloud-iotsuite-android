package com.tencent.qcloud.iot.mqtt.constant;

/**
 * Created by rongerwu on 2018/1/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public enum MqttConnectState {
    CONNECTING,
    CONNECTED,
    PRE_RECONNECT,
    RECONNECTING,
    CLOSED,
}
