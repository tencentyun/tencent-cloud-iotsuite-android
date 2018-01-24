package com.tencent.qcloud.iot.mqtt;

/**
 * Created by rongerwu on 2018/1/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class QCloudMqttClientException extends RuntimeException {
    public QCloudMqttClientException(String message) {
        super(message);
    }

    public QCloudMqttClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
