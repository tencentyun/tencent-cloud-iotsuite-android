package com.tencent.qcloud.iot.app.tv.model.request;

import android.text.TextUtils;

/**
 * Created by rongerwu on 2018/7/20.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class TVControlDeviceRequest {
    private String mDeviceId;
    private String mAttributeName;
    private Object mAttributeValue;

    public TVControlDeviceRequest(String deviceId, String attributeName, Object attributeValue) {
        if (TextUtils.isEmpty(deviceId) || TextUtils.isEmpty(attributeName) || attributeValue == null) {
            throw new IllegalArgumentException("params error");
        }
        mDeviceId = deviceId;
        mAttributeName = attributeName;
        mAttributeValue = attributeValue;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public String getAttributeName() {
        return mAttributeName;
    }

    public Object getAttributeValue() {
        return mAttributeValue;
    }
}
