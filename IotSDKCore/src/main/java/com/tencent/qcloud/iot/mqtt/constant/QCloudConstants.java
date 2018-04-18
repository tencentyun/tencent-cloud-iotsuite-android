package com.tencent.qcloud.iot.mqtt.constant;

/**
 * Created by rongerwu on 2018/1/23.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class QCloudConstants {

    /**
     * mqtt连接的端口
     */
    public static final int MQTT_PORT = 1883;

    private static final String BASE_URL = "http://gz.auth-device-iot.tencentcloudapi.com";

    /**
     * 请求token的url
     */
    public static final String TOKEN_URL = BASE_URL + "/device";

    public static final String TIMESTAMP_URL = BASE_URL + "/time";

    /**
     * 请求token成功的返回值标识
     */
    public static final int TOKEN_RETURN_CODE_SUCCESS = 0;
}
