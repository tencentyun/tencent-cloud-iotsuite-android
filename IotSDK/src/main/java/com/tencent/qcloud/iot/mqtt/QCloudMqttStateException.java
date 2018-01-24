package com.tencent.qcloud.iot.mqtt;

/**
 * Created by rongerwu on 2018/1/14.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class QCloudMqttStateException extends RuntimeException {
    public QCloudMqttStateException(String message) {
        super(message);
    }

    public QCloudMqttStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
