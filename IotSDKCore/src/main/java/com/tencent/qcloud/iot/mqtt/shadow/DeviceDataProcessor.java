package com.tencent.qcloud.iot.mqtt.shadow;

import android.support.annotation.NonNull;

import com.tencent.qcloud.iot.common.QLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by rongerwu on 2018/4/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

/**
 * desire处理逻辑：
 * 1、不把服务端desired置为null。
 * 2、客户端缓存收到的desired（包括上线时get到的desired和包含在control命令里的desired），假设是mCachedDesired（假如收到2次，第一次是{A:1}，第二次是{B:2}，则保存的是{A:1, B:2}）
 * 3、上线之后get到的desired，与mCachedDesired进行比较，只对不同的字段作处理，这样就不会重复执行之前已经执行过的desired了。（从服务端收到的control命令里面带的desired另外考虑，不与mCachedDesired作比较）
 * 4、用户直接操作本地数据，效果类似于从服务端收到desired，因此相应更新mCachedDesired
 */
public class DeviceDataProcessor {
    private static final String TAG = DeviceDataProcessor.class.getSimpleName();
    private ShadowManager mShadowManager;
    private JSONObject mCachedDesired = new JSONObject();
    private JSONObject mUserDesiredToReport = new JSONObject();
    /**
     * 设备端数据
     */
    private JSONObject mLocalDeviceData = new JSONObject();
    private IDataEventListener mDataEventListener;

    public DeviceDataProcessor(ShadowManager shadowManager) {
        mShadowManager = shadowManager;
    }

    /**
     * 处理get到的shadow里的desired
     * @param desired
     * @throws JSONException
     */
    public void processDesiredForInit(JSONObject desired) throws JSONException {
        if (desired == null) {
            throw new IllegalArgumentException("processDesiredForInit error");
        }
        JSONObject diffDesired = getDiffDesired(mCachedDesired, desired);
        if (desired.length() > 0) {
            processDeisredForControl(diffDesired);
        }
    }

    /**
     * 处理control消息里的desired
     * @param desired
     * @throws JSONException
     */
    public void processDeisredForControl(JSONObject desired) throws JSONException {
        if (desired == null) {
            throw new IllegalArgumentException("processDeisredForControl error");
        }
        mergeDesired(mCachedDesired, desired);

        ArrayList<String> readyForReportKeys = new ArrayList<>();
        JSONObject localDeviceData = cloneJSONObject(mLocalDeviceData);
        Iterator<String> desiredKeys = desired.keys();
        while (desiredKeys.hasNext()) {
            String key = desiredKeys.next();
            if (!localDeviceData.has(key)) {
                QLog.w(TAG, "processDeisredForControl, not found key = " + key + " in local device data");
                continue;
            }
            boolean diff = !desired.get(key).equals(localDeviceData.get(key));
            if (mDataEventListener != null) {
                mDataEventListener.onControl(key, desired.get(key), diff);
            }
            readyForReportKeys.add(key);
        }

        //TODO: 是否需要本地缓存已上报mReportedDeviceData，然后mLocalDeviceData和mReportedDeviceData对比，只上传差异部分？
        //onControl执行后，触发设备data改变，从而触发updateLocalDeviceData，改变了mLocalDeviceData
        if (readyForReportKeys.size() > 0) {
            JSONObject reportObject = new JSONObject(mLocalDeviceData, readyForReportKeys.toArray(new String[0]));
            mShadowManager.reportShadow(reportObject);
        }
    }

    public void updateLocalDeviceData(JSONObject localDeviceData) {
        mLocalDeviceData = localDeviceData;
    }

    /**
     * 用户主动改变设备数据，效果类似于从服务端收到desired，因此相应更新CachedDesired
     * @param userDesired 用户改变的数据的json结构
     * @param commit 是否需要立即上报到服务器
     */
    public void onUserChangeData(JSONObject userDesired, boolean commit) throws JSONException {
        if (userDesired == null) {
            QLog.e(TAG, "onUserChangeData, userDesired is null");
            return;
        }
        mergeDesired(mCachedDesired, userDesired);
        mergeDesired(mUserDesiredToReport, userDesired);
        if (commit) {
            JSONObject localDeviceData = cloneJSONObject(mLocalDeviceData);
            ArrayList<String> readyForReportKeys = new ArrayList<>();
            Iterator<String> desiredKeys = mCachedDesired.keys();
            while (desiredKeys.hasNext()) {
                String key = desiredKeys.next();
                if (localDeviceData.has(key)) {
                    readyForReportKeys.add(key);
                }
            }
            if (readyForReportKeys.size() > 0) {
                JSONObject reportObject = new JSONObject(mLocalDeviceData, readyForReportKeys.toArray(new String[0]));
                mShadowManager.reportShadow(reportObject);
            }
            //report后，清空
            mCachedDesired = new JSONObject();
        }
    }

    public DeviceDataProcessor setDataEventListener(IDataEventListener dataEventListener) {
        mDataEventListener = dataEventListener;
        return this;
    }

    @NonNull
    private JSONObject cloneJSONObject(JSONObject obj) {
        try {
            return new JSONObject(obj.toString());
        } catch (JSONException e) {
            throw new IllegalArgumentException("cloneJSONObject error", e);
        }
    }

    private synchronized JSONObject mergeDesired(JSONObject mainDesired, JSONObject mergeDesired) throws JSONException {
        Iterator<String> desiredKeys = mergeDesired.keys();
        while (desiredKeys.hasNext()) {
            String key = desiredKeys.next();
            mainDesired.put(key, mergeDesired.get(key));
        }
        //QLog.d(TAG, "mergeDesired: " + mainDesired.toString());
        return mainDesired;
    }

    /**
     * 返回desired中与mCachedDesired不同的字段
     * @param compareDesired
     * @return
     */
    private synchronized JSONObject getDiffDesired(JSONObject mainDesired, JSONObject compareDesired) throws JSONException {
        JSONObject diffDesired = new JSONObject();
        Iterator<String> desiredKeys = compareDesired.keys();
        while (desiredKeys.hasNext()) {
            String key = desiredKeys.next();
            if (mainDesired.has(key)) {
                if (mainDesired.get(key).equals(compareDesired.get(key))) {
                    continue;
                }
            }
            diffDesired.put(key, compareDesired.get(key));
        }
        QLog.d(TAG, "getDiffDesired: " + diffDesired.toString());
        return diffDesired;
    }
}
