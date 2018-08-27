package com.tencent.qcloud.iot.device.data;

import android.support.annotation.NonNull;

import com.tencent.qcloud.iot.log.QLog;
import com.tencent.qcloud.iot.device.mqtt.shadow.ShadowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by rongerwu on 2018/4/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

/**
 * 为了避免用户外部调用connect和disconnect导致缓存的数据被清空，设计为单例。
 * <p>
 * desire处理逻辑：
 * 1、不把服务端desired置为null。
 * 2、客户端缓存收到的desired（包括上线时get到的desired和包含在control命令里的desired），假设是mCachedDesired（假如收到2次，第一次是{A:1}，第二次是{B:2}，则保存的是{A:1, B:2}）
 * 3、上线之后get到的desired，与mCachedDesired进行比较，只对不同的字段作处理，这样就不会重复执行之前已经执行过的desired了。（从服务端收到的control命令里面带的desired另外考虑，不与mCachedDesired作比较）
 * 4、用户直接操作本地数据，效果类似于从服务端收到desired，因此相应更新mCachedDesired
 */
public class DeviceDataHandler {
    private static final String TAG = DeviceDataHandler.class.getSimpleName();
    private static DeviceDataHandler mInstance;
    private ShadowManager mShadowManager;
    //TODO: 持久化 mCachedDesired 和 mCachedMetadataDesired ？以解决重启设备后仍用服务端的desired（可能已过期）来初始化。
    /**
     * 缓存来自服务端或本地的desired
     */
    private JSONObject mCachedDesired = new JSONObject();

    /**
     * 缓存来自服务端的metadata里面的desired，用于时间戳判断
     */
    private JSONObject mCachedMetadataDesired = new JSONObject();
    /**
     * 缓存本地desired用于report，report后需要清空
     */
    private JSONObject mUserDesiredToReport = new JSONObject();
    /**
     * 设备端数据
     */
    private JSONObject mLocalDeviceData = new JSONObject();
    private IDataEventListener mDataEventListener;

    public static DeviceDataHandler getInstance(ShadowManager shadowManager) {
        if (shadowManager == null) {
            throw new IllegalArgumentException("shadowManager is null");
        }
        if (mInstance == null) {
            mInstance = new DeviceDataHandler(shadowManager);
        }
        mInstance.setShadowManager(shadowManager);
        return mInstance;
    }

    private DeviceDataHandler(ShadowManager shadowManager) {
        mShadowManager = shadowManager;
    }

    private void setShadowManager(ShadowManager shadowManager) {
        mShadowManager = shadowManager;
    }

    /**
     * 处理get到的shadow里的desired
     *
     * @param desired desired对象
     * @throws JSONException 异常
     */
    public void handleDesiredForInit(JSONObject desired, JSONObject metadataDesired, int outerSequence) throws JSONException {
        if (desired == null) {
            throw new IllegalArgumentException("handleDesiredForInit error");
        }
        //forInit标识是否用于SDK启动后第一次设备初始化
        boolean forInit = (mCachedDesired.length() == 0);
        handleDeisredForControl(desired, metadataDesired, outerSequence, forInit);
    }

    public void handleDeisredForControl(JSONObject desired, JSONObject metadataDesired, int outerSequence) throws JSONException {
        handleDeisredForControl(desired, metadataDesired, outerSequence, false);
    }

    /**
     * 处理control消息里的desired
     *
     * @param desired desired对象
     * @throws JSONException 异常
     */
    private void handleDeisredForControl(JSONObject desired, JSONObject metadataDesired, int outerSequence, boolean forInit) throws JSONException {
        if (desired == null) {
            throw new IllegalArgumentException("handleDeisredForControl error");
        }
        deleteRemoteDeisred(desired, outerSequence);
        if (metadataDesired != null) {
            desired = filterDesiredByMetadata(desired, metadataDesired);
        }
        mergeDesired(mCachedDesired, desired);

        ArrayList<String> readyForReportKeys = new ArrayList<>();
        JSONObject localDeviceData = cloneJSONObject(mLocalDeviceData);
        Iterator<String> desiredKeys = desired.keys();
        while (desiredKeys.hasNext()) {
            String key = desiredKeys.next();
            if (!localDeviceData.has(key)) {
                QLog.w(TAG, "handleDeisredForControl, not found key = " + key + " in local device data");
                continue;
            }
            boolean diff = !desired.get(key).equals(localDeviceData.get(key));
            if (mDataEventListener != null) {
                mDataEventListener.onControl(key, desired.get(key), diff, forInit);
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

    /**
     * delete服务端的desired
     *
     * @param desired
     * @param outerSequence
     * @throws JSONException
     */
    private void deleteRemoteDeisred(JSONObject desired, int outerSequence) throws JSONException {
        JSONObject delete = new JSONObject();
        Iterator<String> desiredKeys = desired.keys();
        while (desiredKeys.hasNext()) {
            String key = desiredKeys.next();
            delete.put(key, JSONObject.NULL);
        }
        if (delete.length() > 0) {
            mShadowManager.deleteShadow(delete, outerSequence);
        }
    }

    public void updateLocalDeviceData(JSONObject localDeviceData) {
        mLocalDeviceData = localDeviceData;
    }

    /**
     * 用户主动改变设备数据，效果类似于从服务端收到desired，因此相应更新CachedDesired
     *
     * @param userDesired 用户改变的数据的json结构
     * @param commit      是否需要立即上报到服务器
     * @throws JSONException 异常
     */
    public void onUserChangeData(JSONObject userDesired, boolean commit) throws JSONException {
        if (userDesired == null) {
            QLog.e(TAG, "onUserChangeData, userDesired is null");
            return;
        }
        mergeDesired(mCachedDesired, userDesired);
        mergeDesired(mUserDesiredToReport, userDesired);
        updateCacheMetadataOnUserChangeData(userDesired);
        if (commit) {
            JSONObject localDeviceData = cloneJSONObject(mLocalDeviceData);
            ArrayList<String> readyForReportKeys = new ArrayList<>();
            Iterator<String> desiredKeys = mUserDesiredToReport.keys();
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
            mUserDesiredToReport = new JSONObject();
        }
    }

    public DeviceDataHandler setDataEventListener(IDataEventListener dataEventListener) {
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
     *
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

    /**
     * 过滤掉sequence不符的字段
     *
     * @param desired
     * @param metadataDesired
     * @return
     */
    private synchronized JSONObject filterDesiredByMetadata(JSONObject desired, JSONObject metadataDesired) throws JSONException {
        if (metadataDesired == null) {
            return desired;
        }
        Iterator<String> metadataDesiredKeys = metadataDesired.keys();
        while (metadataDesiredKeys.hasNext()) {
            String key = metadataDesiredKeys.next();
            if (mCachedMetadataDesired.has(key)) {
                int newSequence = metadataDesired.getJSONObject(key).getInt(ShadowHandler.SHADOW_JSON_KEY_SEQUENCE);
                int cachedSequeuce = mCachedMetadataDesired.getJSONObject(key).getInt(ShadowHandler.SHADOW_JSON_KEY_SEQUENCE);
                if (newSequence <= cachedSequeuce) {
                    long newTimestamp = metadataDesired.getJSONObject(key).getLong(ShadowHandler.SHADOW_JSON_KEY_TIMESTAMP);
                    long cachedTimestamp = mCachedMetadataDesired.getJSONObject(key).getLong(ShadowHandler.SHADOW_JSON_KEY_TIMESTAMP);
                    //考虑cachedSequeuce到达Integer.MAX_VALUE，newSequence从0重新开始计数的情况
                    if (newTimestamp < cachedTimestamp || (newTimestamp == cachedTimestamp && cachedSequeuce - newSequence < 10000)) {
                        QLog.d(TAG, "expired desired, key = " + key + ", cachedSequeuce = " + cachedSequeuce + ", newSequence = " + newSequence);
                        desired.remove(key);
                        continue;
                    }
                }
            }
            mCachedMetadataDesired.put(key, metadataDesired.get(key));
        }
        return desired;
    }

    private void updateCacheMetadataOnUserChangeData(JSONObject userDesired) {
        //TODO:当前时间戳写入到 mCachedMetadataDesired 对应的字段中，前提是获取的时间戳和服务端是同步的。以解决本地控制和服务端控制的时序问题。
    }
}
