package com.tencent.qcloud.iot.device.mqtt.certificate;

/**
 * Created by rongerwu on 2018/1/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class TCSSLSocketException extends Exception {
    public TCSSLSocketException(String message) {
        super(message);
    }

    public TCSSLSocketException(String message, Throwable cause) {
        super(message, cause);
    }
}
