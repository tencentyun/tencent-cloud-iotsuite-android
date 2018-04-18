package com.tencent.qcloud.iot.sample.qcloud;

import android.util.Log;

import com.tencent.qcloud.iot.device.TCIotDeviceService;
import com.tencent.qcloud.iot.mqtt.TCMqttConfig;
import com.tencent.qcloud.iot.mqtt.TCMqttConfig.TCMqttConnectionMode;
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
    private TCIotDeviceService mTCIotDeviceService;

    private IDataEventListener mDataEventListener = new IDataEventListener() {
        @Override
        public void onControl(String key, Object value, boolean diff) {
            try {
                mDataTemplate.onControl(key, value, diff);
            } catch (ClassCastException e) {
                Log.w(TAG, "onControl", e);
            }
        }
    };

    private JsonFileData.ILocalDataListener mLocalDataListener = new JsonFileData.ILocalDataListener() {
        @Override
        public void onLocalDataChange(JSONObject localData) {
            if (mTCIotDeviceService == null) {
                Log.e(TAG, "onDataChange， mTCIotDeviceService is null");
                return;
            }
            mTCIotDeviceService.onLocalDataChange(localData);
        }

        @Override
        public void onUserChangeData(JSONObject userDesired, boolean commit) {
            if (mTCIotDeviceService == null) {
                Log.e(TAG, "onUserChangeData， mTCIotDeviceService is null");
                return;
            }
            mTCIotDeviceService.onUserChangeData(userDesired, commit);
        }
    };

    public DeviceDataManager() {
        mJsonFileData = JsonFileData.getInstance();
        mDataTemplate = mJsonFileData.getDataTemplate();
        //监听设备数据修改，用于传入SDK处理，触发上传到服务器
        mDataTemplate.setLocalDataListener(mLocalDataListener);
    }

    public TCMqttConfig genTCMqttConfig() {
        TCMqttConfig config = new TCMqttConfig(mJsonFileData.getHost(), mJsonFileData.getProductKey(), mJsonFileData.getProductId());
        TCMqttConnectionMode connectionMode = getConnectionMode();
        config.setConnectionMode(connectionMode);
        if (connectionMode == TCMqttConnectionMode.MODE_DIRECT) {
            config.setMqttUserName(mJsonFileData.getUserName())
                    .setMqttPassword(mJsonFileData.getPassword());
        }
        return config;
    }

    public TCMqttConnectionMode getConnectionMode() {
        int authType = mJsonFileData.getAuthType();
        if (authType == JsonFileData.AUTH_TYPE_DIRECT) {
            return TCMqttConnectionMode.MODE_DIRECT;
        } else if (authType == JsonFileData.AUTH_TYPE_TOKEN) {
            return TCMqttConnectionMode.MODE_TOKEN;
        } else {
            throw new IllegalStateException("illegal auth type = " + authType);
        }
    }

    public void setTCIotDeviceService(TCIotDeviceService TCIotDeviceService) {
        mTCIotDeviceService = TCIotDeviceService;
        onSetTCIotDeviceService();
    }

    public JsonFileData getJsonFileData() {
        return mJsonFileData;
    }

    private void onSetTCIotDeviceService() {
        if (mTCIotDeviceService == null) {
            return;
        }
        //监听来自服务端的控制消息
        mTCIotDeviceService.setDataEventListener(mDataEventListener);
        mTCIotDeviceService.onLocalDataChange(mDataTemplate.toJson());
    }

    public void setDataControlListener(JsonFileData.IDataControlListener dataControlListener) {
        //监听本地处理后的来自服务端的控制消息
        mDataTemplate.setDataControlListener(dataControlListener);
    }
}
