package com.tencent.qcloud.iot.mqtt.certificate;

/**
 * Created by rongerwu on 2018/1/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class QCloudCertificateException extends RuntimeException {
    public QCloudCertificateException(String message) {
        super(message);
    }

    public QCloudCertificateException(Throwable cause) {
        super(cause);
    }

    public QCloudCertificateException(String message, Throwable cause) {
        super(message, cause);
    }
}
