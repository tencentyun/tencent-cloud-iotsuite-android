package com.tencent.qcloud.iot.sample.qcloud;

import android.util.Log;

import com.tencent.qcloud.iot.mqtt.QCloudIotMqttService;
import com.tencent.qcloud.iot.mqtt.QCloudMqttConfig;
import com.tencent.qcloud.iot.mqtt.QCloudMqttConfig.QCloudMqttConnectionMode;
import com.tencent.qcloud.iot.mqtt.shadow.IDataEventListener;

import org.json.JSONObject;

/**
 * Created by rongerwu on 2018/4/9.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class DeviceDataManager {
    private static final String TAG = DeviceDataManager.class.getSimpleName();
    private JsonFileData mJsonFileData;
    private JsonFileData.DataTemplate mDataTemplate;
    private QCloudIotMqttService mQCloudIotMqttService;

    private IDataEventListener mDataEventListener = new IDataEventListener() {
        @Override
        public void onControl(String key, Object value, boolean diff) {
            mDataTemplate.onControl(key, value, diff);
        }
    };

    private JsonFileData.ILocalDataListener mLocalDataListener = new JsonFileData.ILocalDataListener() {
        @Override
        public void onLocalDataChange(JSONObject localData) {
            if (mQCloudIotMqttService == null) {
                Log.e(TAG, "onDataChange， mQCloudIotMqttService is null");
                return;
            }
            mQCloudIotMqttService.onLocalDataChange(localData);
        }

        @Override
        public void onUserChangeData(JSONObject userDesired, boolean commit) {
            if (mQCloudIotMqttService == null) {
                Log.e(TAG, "onUserChangeData， mQCloudIotMqttService is null");
                return;
            }
            mQCloudIotMqttService.onUserChangeData(userDesired, commit);
        }
    };

    public DeviceDataManager() {
        mJsonFileData = JsonFileData.getInstance();
        mDataTemplate = mJsonFileData.getDataTemplate();
        //监听设备数据修改，用于传入SDK处理，触发上传到服务器
        mDataTemplate.setLocalDataListener(mLocalDataListener);
    }

    public QCloudMqttConfig genQCloudMqttConfig() {
        QCloudMqttConfig config = new QCloudMqttConfig(mJsonFileData.getHost(), mJsonFileData.getProductKey(), mJsonFileData.getProductId());
        QCloudMqttConnectionMode connectionMode = getConnectionMode();
        config.setConnectionMode(connectionMode);
        if (connectionMode == QCloudMqttConnectionMode.MODE_DIRECT) {
            config.setMqttUserName(mJsonFileData.getUserName())
                    .setMqttPassword(mJsonFileData.getPassword());
        }
        return config;
    }

    public QCloudMqttConnectionMode getConnectionMode() {
        int authType = mJsonFileData.getAuthType();
        if (authType == JsonFileData.AUTH_TYPE_DIRECT) {
            return QCloudMqttConnectionMode.MODE_DIRECT;
        } else if (authType == JsonFileData.AUTH_TYPE_TOKEN) {
            return QCloudMqttConnectionMode.MODE_TOKEN;
        } else {
            throw new IllegalStateException("illegal auth type = " + authType);
        }
    }

    public void setQCloudIotMqttService(QCloudIotMqttService QCloudIotMqttService) {
        mQCloudIotMqttService = QCloudIotMqttService;
        onSetQCloudIotMqttService();
    }

    public JsonFileData getJsonFileData() {
        return mJsonFileData;
    }

    private void onSetQCloudIotMqttService() {
        if (mQCloudIotMqttService == null) {
            return;
        }
        //监听来自服务端的控制消息
        mQCloudIotMqttService.setDataEventListener(mDataEventListener);
        mQCloudIotMqttService.onLocalDataChange(mDataTemplate.toJson());
    }

    public void setDataControlListener(JsonFileData.IDataControlListener dataControlListener) {
        //监听本地处理后的来自服务端的控制消息
        mDataTemplate.setDataControlListener(dataControlListener);
    }
}
