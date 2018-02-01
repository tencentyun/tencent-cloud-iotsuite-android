package com.tencent.qcloud.iot.mqtt.shadow;

import android.text.TextUtils;

/**
 * Created by rongerwu on 2018/1/30.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class ShadowTopicHelper {
    private String mProductId;
    private String mDeviceName;

    public ShadowTopicHelper(String productId, String deviceName) {
        if (TextUtils.isEmpty(productId) || TextUtils.isEmpty(deviceName)) {
            throw new IllegalArgumentException("productId and deviceName cannot be empty");
        }
        mProductId = productId;
        mDeviceName = deviceName;
    }

    public String getGetTopic() {
        return String.format("shadow/get/%s/%s", mProductId, mDeviceName);
    }

    public String getUpdateTopic() {
        return String.format("shadow/update/%s/%s", mProductId, mDeviceName);
    }
}
