package com.tencent.qcloud.iot.mqtt.certificate;

/**
 * Created by rongerwu on 2018/1/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class QCloudSSLSocketException extends Exception {
    public QCloudSSLSocketException(String message) {
        super(message);
    }

    public QCloudSSLSocketException(String message, Throwable cause) {
        super(message, cause);
    }
}
