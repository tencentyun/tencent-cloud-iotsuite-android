package com.tencent.qcloud.iot.sample.qcloud;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rongerwu on 2018/4/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class JsonFileData {
    public static int AUTH_TYPE_DIRECT = 0;
    public static int AUTH_TYPE_TOKEN = 1;
    private static final String TAG = JsonFileData.class.getSimpleName();
    private String mProductId = ${product_id};
    private String mProductKey = ${product_key};
    private String mProductSecret = ${product_secret};
    private int mAppId = ${app_id};
    private String mRegion = ${region};
    private String mProductName = ${name};
    private String mDescription = ${description};
    private String mHost = ${domain};
    private int mStandard = ${standard};
    private int mAuthType = ${auth_type};
    private int mDeleted = ${deleted};
    private String mMessage = ${message};
    private String mCreateTime = ${create_time};
    private String mUpdateTime = ${update_time};
    private String mUserName = ${username};
    private String mPassword = ${password};

    private static JsonFileData mInstance;
    private DataTemplate mDataTemplate;

    public static JsonFileData getInstance() {
        if (mInstance == null) {
            mInstance = new JsonFileData();
        }
        return mInstance;
    }

    private JsonFileData() {
        mDataTemplate = new DataTemplate();
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

    public static class DataTemplate {
        private IDataControlListener mDataControlListener;
        private ILocalDataListener mLocalDataListener;
        ${JAVA_CODE_CONST_FIELD_STRING}
        ${JAVA_CODE_FIELD}
        ${JAVA_CODE_ACCESS_METHOD}
        private void onLocalDataChange() {
            if (mLocalDataListener != null) {
                mLocalDataListener.onLocalDataChange(toJson());
            }
        }

        private void onUserChangeData(JSONObject userDesired, boolean commit) {
            if (mLocalDataListener != null) {
                mLocalDataListener.onUserChangeData(userDesired, commit);
            }
        }

        private JSONObject genJsonObject(String key, Object value) {
            JSONObject object = new JSONObject();
            try {
                object.put(key, value);
            } catch (JSONException e) {
                throw new RuntimeException("gen json object error", e);
            }
            return object;
        }

        public JSONObject toJson() {
            JSONObject object = new JSONObject();
            try {
                ${JAVA_CODE_TO_JSON_METHOD}
            } catch (JSONException e) {
                throw new RuntimeException("to json string error", e);
            }
            return object;
        }

        /**
         * 解析并处理来自服务端的控制消息
         * @param key
         * @param obj
         * @return
         */
        //TODO: 根据diff判断是否需要执行
        public boolean onControl(String key, Object obj, boolean diff) {
            if (mDataControlListener == null) {
                return false;
            }
            boolean result = false;
            switch (key) {
                ${JAVA_CODE_ON_CONTROL_METHOD}
                default:
                    Log.e(TAG, "onControl, illegal key = " + key);
            }
            return result;
        }

        public DataTemplate setDataControlListener(IDataControlListener dataControlListener) {
            mDataControlListener = dataControlListener;
            return this;
        }

        public DataTemplate setLocalDataListener(ILocalDataListener dataChangeListener) {
            mLocalDataListener = dataChangeListener;
            return this;
        }
        ${JAVA_CODE_ENUM_INDEX_METHOD}
    }
    ${JAVA_CODE_ENUM_CLASS}
    /**
     * 监听来自服务端的控制消息
     * 每个接口，处理完成后需要返回true，才能够正确修改设备数据。
     */
    public interface IDataControlListener {
        ${JAVA_CODE_ON_CONTROL_LISTENER}
    }

    public interface ILocalDataListener {
        void onLocalDataChange(JSONObject localData);
        void onUserChangeData(JSONObject userDesired, boolean commit);
    }
}
