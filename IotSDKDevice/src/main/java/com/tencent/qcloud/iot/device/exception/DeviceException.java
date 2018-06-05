package com.tencent.qcloud.iot.device.exception;

/**
 * Created by rongerwu on 2018/6/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class DeviceException extends Exception {
    public DeviceException(String message) {
        super(message);
    }

    public DeviceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceException(Throwable cause) {
        super(cause);
    }
}
