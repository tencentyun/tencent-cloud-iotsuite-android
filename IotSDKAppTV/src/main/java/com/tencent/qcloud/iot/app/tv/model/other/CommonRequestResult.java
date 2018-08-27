package com.tencent.qcloud.iot.app.tv.model.other;

/**
 * Created by rongerwu on 2018/7/17.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class CommonRequestResult {
    private boolean mError;

    public boolean isError() {
        return mError;
    }

    public CommonRequestResult setError(boolean error) {
        mError = error;
        return this;
    }
}
