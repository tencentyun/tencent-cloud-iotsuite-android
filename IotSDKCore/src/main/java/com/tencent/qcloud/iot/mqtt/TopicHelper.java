package com.tencent.qcloud.iot.mqtt;

import android.text.TextUtils;

/**
 * Created by rongerwu on 2018/1/30.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class TopicHelper {
    private String mProductId;
    private String mDeviceName;

    public TopicHelper(String productId, String deviceName) {
        if (TextUtils.isEmpty(productId) || TextUtils.isEmpty(deviceName)) {
            throw new IllegalArgumentException("productId and deviceName cannot be empty");
        }
        mProductId = productId;
        mDeviceName = deviceName;
    }

    public String getShadowGetTopic() {
        return String.format("shadow/get/%s/%s", mProductId, mDeviceName);
    }

    public String getShadowUpdateTopic() {
        return String.format("shadow/update/%s/%s", mProductId, mDeviceName);
    }

    public String getOtaGetTopic() {
        return String.format("ota/get/%s/%s", mProductId, mDeviceName);
    }

    public String getOtaUpdateTopic() {
        return String.format("ota/update/%s/%s", mProductId, mDeviceName);
    }
}
