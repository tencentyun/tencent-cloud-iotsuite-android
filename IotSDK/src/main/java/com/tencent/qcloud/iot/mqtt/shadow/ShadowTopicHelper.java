package com.tencent.qcloud.iot.mqtt.shadow;

import android.text.TextUtils;

/**
 * Created by rongerwu on 2018/1/30.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class ShadowTopicHelper {
    private String mProductKey;
    private String mDeviceName;

    public ShadowTopicHelper(String productKey, String deviceName) {
        if (TextUtils.isEmpty(productKey) || TextUtils.isEmpty(deviceName)) {
            throw new IllegalArgumentException("productKey and deviceName cannot be empty");
        }
        mProductKey = productKey;
        mDeviceName = deviceName;
    }

    public String getGetTopic() {
        return String.format("/shadow/get/%s/%s", mProductKey, mDeviceName);
    }

    public String getUpdateTopic() {
        return String.format("/shadow/update/%s/%s", mProductKey, mDeviceName);
    }
}
