package com.tencent.qcloud.iot.device.mqtt;

/**
 * Created by rongerwu on 2018/1/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class TCMqttClientException extends RuntimeException {
    public TCMqttClientException(String message) {
        super(message);
    }

    public TCMqttClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
