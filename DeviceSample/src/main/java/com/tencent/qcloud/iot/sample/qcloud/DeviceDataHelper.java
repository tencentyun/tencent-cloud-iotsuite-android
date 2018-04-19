package com.tencent.qcloud.iot.sample.qcloud;

import android.util.Log;

import com.tencent.qcloud.iot.device.TCIotDeviceService;
import com.tencent.qcloud.iot.device.data.IDataEventListener;

import org.json.JSONObject;

/**
 * Created by rongerwu on 2018/4/9.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class DeviceDataHelper {
    private static final String TAG = DeviceDataHelper.class.getSimpleName();
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
                Log.e(TAG, "onLocalDataChange， mTCIotDeviceService is null");
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

    public DeviceDataHelper(TCIotDeviceService deviceService, JsonFileData.DataTemplate dataTemplate) {
        if (deviceService == null || dataTemplate == null) {
            throw new IllegalArgumentException("deviceService or dataTemplate is null");
        }
        mTCIotDeviceService = deviceService;
        mDataTemplate = dataTemplate;
        //监听来自服务端的控制消息
        mTCIotDeviceService.setDataEventListener(mDataEventListener);
        mTCIotDeviceService.onLocalDataChange(mDataTemplate.toJson());
        //监听设备数据修改，用于传入SDK处理，触发上传到服务器
        mDataTemplate.setLocalDataListener(mLocalDataListener);
    }

    /**
     * 设置监听服务端对数据点的控制消息
     *
     * @param dataControlListener
     */
    public void setDataControlListener(JsonFileData.IDataControlListener dataControlListener) {
        if (dataControlListener == null) {
            throw new IllegalArgumentException("dataControlListener is null");
        }
        //监听本地处理后的来自服务端的控制消息
        mDataTemplate.setDataControlListener(dataControlListener);
    }

    public JsonFileData.DataTemplate getDataTemplate() {
        return mDataTemplate;
    }
}
