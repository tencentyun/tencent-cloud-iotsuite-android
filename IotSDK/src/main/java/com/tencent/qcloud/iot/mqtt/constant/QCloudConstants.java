package com.tencent.qcloud.iot.mqtt.constant;

/**
 * Created by rongerwu on 2018/1/23.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class QCloudConstants {
    /**
     * client id后缀，可以是其他值
     */
    public static final String CLIENT_SUFFIX = "android";

    /**
     * mqtt连接的端口
     */
    public static final int MQTT_PORT = 1883;

    /**
     * 请求token的url
     */
    public static final String TOKEN_URL = "http://auth-device-iot.tencentcloudapi.com/device";

    /**
     * 请求token成功的返回值标识
     */
    public static final int TOKEN_RETURN_CODE_SUCCESS = 0;
}
