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
    private String mProductId = "iot-pbppj8jy";
    private String mProductKey = "mqtt-4tevxlx8e";
    private String mProductSecret = "";
    private int mAppId = 0;
    private String mRegion = "gz";
    private String mProductName = "智能灯";
    private String mDescription = "可远程开关、调节亮度和颜色的智能灯。";
    private String mHost = "mqtt-4tevxlx8e.ap-guangzhou.mqtt.tencentcloudmq.com";
    private int mStandard = 3;
    private int mAuthType = 1;
    private int mDeleted = 0;
    private String mMessage = "";
    private String mCreateTime = "2018-01-12 00:00:00";
    private String mUpdateTime = "2018-03-26 00:00:00";
    private String mUserName = "username";
    private String mPassword = "password";

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

        private static final String DEVICE_SWITCH = "device_switch";
        private static final String COLOR = "color";
        private static final String BRIGHTNESS = "brightness";

        private boolean mDeviceSwitch = false;
        private Color mColor = Color.values()[0];
        private int mBrightness = 0;

        public boolean isDeviceSwitch() {
            return mDeviceSwitch;
        }

        private void setDeviceSwitch(boolean deviceSwitch) {
            mDeviceSwitch = deviceSwitch;
            onLocalDataChange();
        }

        private void setDeviceSwitchByUser(boolean deviceSwitch, boolean commit) {
            setDeviceSwitch(deviceSwitch);
            onUserChangeData(genJsonObject(DEVICE_SWITCH, mDeviceSwitch), commit);
        }

        private Color getColor() {
            return mColor;
        }

        private void setColor(Color color) {
            mColor = color;
            onLocalDataChange();
        }

        public void setColorByUser(Color color, boolean commit) {
            setColor(color);
            onUserChangeData(genJsonObject(COLOR, mColor.getIndex()), commit);
        }

        public int getBrightness() {
            return mBrightness;
        }

        private void setBrightness(int brightness) {
            final int min = 0;
            final int max = 100;
            if (brightness < min || brightness > max) {
                throw new IllegalArgumentException("out of range [" + min + ", " + max + "]");
            }
            mBrightness = brightness;
            onLocalDataChange();
        }

        public void setBrightnessByUser(int brightness, boolean commit) {
            setBrightness(brightness);
            onUserChangeData(genJsonObject(BRIGHTNESS, mBrightness), commit);
        }

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

                object.put(DEVICE_SWITCH, mDeviceSwitch);
                object.put(COLOR, mColor.getIndex());
                object.put(BRIGHTNESS, mBrightness);
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

                case DEVICE_SWITCH:
                    boolean deviceSwitch = (boolean) obj;
                    result = mDataControlListener.onControlDeviceSwitch(deviceSwitch);
                    if (result) {
                        setDeviceSwitch(deviceSwitch);
                    }
                    break;
                case COLOR:
                    Color color = getColorByIndex((int) obj);
                    if (color == null) {
                        break;
                    }
                    result = mDataControlListener.onControlColor(color);
                    if (result) {
                        setColor(color);
                    }
                    break;
                case BRIGHTNESS:
                    int brightness = ((Number) obj).intValue();
                    result = mDataControlListener.onControlBrightness(brightness);
                    if (result) {
                        setBrightness(brightness);
                    }
                    break;
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

        private Color getColorByIndex(int index) {
            for (Color color : Color.values()) {
                if (color.getIndex() == index) {
                    return color;
                }
            }
            Log.e(TAG, "not value map to index = " + index);
            return null;
        }

    }

    public enum Color {
        RED(0, "red"),
        GREEN(1, "green"),
        BLUE(2, "blue");

        private int mIndex;
        private String mName;

        Color(int index, String name) {
            mIndex = index;
            mName = name;
        }

        public int getIndex() {
            return mIndex;
        }

        public String getName() {
            return mName;
        }
    }

    /**
     * 监听来自服务端的控制消息
     * 每个接口，处理完成后需要返回true，才能够正确修改设备数据。
     */
    public interface IDataControlListener {

        boolean onControlDeviceSwitch(boolean deviceSwitch);
        boolean onControlColor(Color color);
        boolean onControlBrightness(int brightness);
    }

    public interface ILocalDataListener {
        void onLocalDataChange(JSONObject localData);
        void onUserChangeData(JSONObject userDesired, boolean commit);
    }
}
