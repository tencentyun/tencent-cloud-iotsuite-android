package com.tencent.qcloud.iot.mqtt.request;

import com.tencent.qcloud.iot.mqtt.callback.IMqttActionCallback;

/**
 * Created by rongerwu on 2018/1/12.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class BaseMqttRequest<T> {
    protected IMqttActionCallback mCallback;

    public IMqttActionCallback getCallback() {
        return mCallback;
    }

    public T setCallback(IMqttActionCallback callback) {
        mCallback = callback;
        return (T) this;
    }
}
