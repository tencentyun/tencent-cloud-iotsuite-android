package com.tencent.qcloud.iot.device.exception;

/**
 * Created by rongerwu on 2018/6/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class DeviceRuntimeException extends RuntimeException {
    public DeviceRuntimeException(String message) {
        super(message);
    }

    public DeviceRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceRuntimeException(Throwable cause) {
        super(cause);
    }
}
