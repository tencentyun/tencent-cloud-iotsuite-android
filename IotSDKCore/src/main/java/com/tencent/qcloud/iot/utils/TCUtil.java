package com.tencent.qcloud.iot.utils;

/**
 * Created by rongerwu on 2018/4/20.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class TCUtil {
    private static final String BASE_CLOUD_API_HOST = ".auth-device-iot.tencentcloudapi.com";

    public static String getCloudApiUrl(String scheme, String region) {
        return String.format("%s://%s%s", scheme, region, BASE_CLOUD_API_HOST);
    }

    public static String getTimeUrl(String scheme, String region) {
        return getCloudApiUrl(scheme, region) + "/time";
    }

    public static String getTokenUrl(String scheme, String region) {
        return getCloudApiUrl(scheme, region) + "/device";
    }
}
