package com.tencent.qcloud.iot.device.mqtt.callback;

import com.tencent.qcloud.iot.device.mqtt.constant.MqttConnectState;

/**
 * Created by rongerwu on 2018/1/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

/**
 * mqtt连接请求的回调接口
 */
public interface IMqttConnectStateCallback {
    void onStateChanged(MqttConnectState state);
}
