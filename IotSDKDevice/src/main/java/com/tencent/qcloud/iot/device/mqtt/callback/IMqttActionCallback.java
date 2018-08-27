package com.tencent.qcloud.iot.device.mqtt.callback;

/**
 * Created by rongerwu on 2018/1/14.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

/**
 * 发布和订阅等请求的回调接口
 */
public interface IMqttActionCallback {
    void onSuccess();

    void onFailure(Throwable exception);
}
