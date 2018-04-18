package com.tencent.qcloud.iot.mqtt;

/**
 * Created by rongerwu on 2018/1/14.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class TCMqttStateException extends RuntimeException {
    public TCMqttStateException(String message) {
        super(message);
    }

    public TCMqttStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
