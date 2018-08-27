package com.tencent.qcloud.iot.device.mqtt.certificate;

/**
 * Created by rongerwu on 2018/1/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class TCCertificateException extends RuntimeException {
    public TCCertificateException(String message) {
        super(message);
    }

    public TCCertificateException(Throwable cause) {
        super(cause);
    }

    public TCCertificateException(String message, Throwable cause) {
        super(message, cause);
    }
}
