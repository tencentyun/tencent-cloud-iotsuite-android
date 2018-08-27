package com.tencent.qcloud.iot.app.tv.model.serverpush;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rongerwu on 2018/7/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class BindTVServerPush {
    private String mTVId;
    private String mAccessToken;

    public BindTVServerPush(String message) throws JSONException {
        JSONObject jsonObject = new JSONObject(message);
        mTVId = jsonObject.getString("TvId");
        mAccessToken = jsonObject.getString("AccessToken");
    }

    public String getTVId() {
        return mTVId;
    }

    public String getAccessToken() {
        return mAccessToken;
    }
}
