package com.tencent.qcloud.iot.device.datatemplate;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rongerwu on 2018/4/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class JsonFileData {
    private static final String PRODUCT_ID = "product_id";
    private static final String PRODUCT_KEY = "product_key";
    private static final String PRODUCT_SECRET = "product_secret";
    private static final String APP_ID = "app_id";
    private static final String REGION = "region";
    private static final String PRODUCT_NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String DOMAIN = "domain";
    private static final String STANDARD = "standard";
    private static final String AUTH_TYPE = "auth_type";
    private static final String DATA_TEMPLATE = "data_template";
    private static final String DELETED = "deleted";
    private static final String MESSAGE = "message";
    private static final String CREATE_TIME = "create_time";
    private static final String UPDATE_TIME = "update_time";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    public static int AUTH_TYPE_DIRECT = 0;
    public static int AUTH_TYPE_TOKEN = 1;
    private static final String TAG = JsonFileData.class.getSimpleName();
    final private String mProductId;
    final private String mProductKey;
    final private String mProductSecret;
    final private int mAppId;
    final private String mRegion;
    final private String mProductName;
    final private String mDescription;
    final private String mHost;
    final private int mStandard;
    final private int mAuthType;
    final private int mDeleted;
    final private String mMessage;
    final private String mCreateTime;
    final private String mUpdateTime;
    final private String mUserName;
    final private String mPassword;

    private DataTemplate mDataTemplate;

    public JsonFileData(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        mProductId = jsonObject.getString(PRODUCT_ID);
        mProductKey = jsonObject.getString(PRODUCT_KEY);
        mProductSecret = jsonObject.optString(PRODUCT_SECRET, "");
        mAppId = jsonObject.getInt(APP_ID);
        mRegion = jsonObject.getString(REGION);
        mProductName = jsonObject.optString(PRODUCT_NAME, "");
        mDescription = jsonObject.optString(DESCRIPTION, "");
        mHost = jsonObject.getString(DOMAIN);
        mStandard = jsonObject.optInt(STANDARD, -1);
        mAuthType = jsonObject.getInt(AUTH_TYPE);
        mDeleted = jsonObject.optInt(DELETED, -1);
        mMessage = jsonObject.optString(MESSAGE, "");
        mCreateTime = jsonObject.optString(CREATE_TIME, "");
        mUpdateTime = jsonObject.optString(UPDATE_TIME, "");
        mUserName = jsonObject.optString(USERNAME, "");
        mPassword = jsonObject.optString(PASSWORD, "");

        if (!jsonObject.isNull(DATA_TEMPLATE)) {
            mDataTemplate = new DataTemplate(jsonObject.getJSONArray(DATA_TEMPLATE));
        }
    }

    public String getProductId() {
        return mProductId;
    }

    public String getProductKey() {
        return mProductKey;
    }

    public String getProductSecret() {
        return mProductSecret;
    }

    public int getAppId() {
        return mAppId;
    }

    public String getRegion() {
        return mRegion;
    }

    public String getProductName() {
        return mProductName;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getHost() {
        return mHost;
    }

    public int getStandard() {
        return mStandard;
    }

    public int getAuthType() {
        return mAuthType;
    }

    public int getDeleted() {
        return mDeleted;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getCreateTime() {
        return mCreateTime;
    }

    public String getUpdateTime() {
        return mUpdateTime;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getPassword() {
        return mPassword;
    }

    public DataTemplate getDataTemplate() {
        return mDataTemplate;
    }

}
