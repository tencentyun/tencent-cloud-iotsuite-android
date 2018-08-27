package com.tencent.qcloud.iot.app.tv.model.request;

import android.text.TextUtils;

import com.tencent.qcloud.iot.apiclient.model.TCIotCommonApiRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by rongerwu on 2018/7/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class WaitBindTVRequest extends TCIotCommonApiRequest {
    private String mTVId;

    public WaitBindTVRequest(String tvId) {
        if (TextUtils.isEmpty(tvId)) {
            throw new IllegalArgumentException("params is empty");
        }
        mTVId = tvId;
    }

    public String getTVId() {
        return mTVId;
    }

    @Override
    public Map<String, String> getParams() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("TvId", mTVId);
        return params;
    }

    @Override
    public String getAction() {
        return "TvWaitBind";
    }
}
