package com.tencent.qcloud.iot.device.datatemplate;

import com.tencent.qcloud.iot.device.datatemplate.datapoint.BoolDataPoint;
import com.tencent.qcloud.iot.device.datatemplate.datapoint.DataPoint;
import com.tencent.qcloud.iot.device.datatemplate.datapoint.EnumDataPoint;
import com.tencent.qcloud.iot.device.datatemplate.datapoint.NumberDataPoint;
import com.tencent.qcloud.iot.device.datatemplate.datapoint.StringDataPoint;
import com.tencent.qcloud.iot.device.exception.DeviceException;
import com.tencent.qcloud.iot.log.QLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rongerwu on 2018/6/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class DataTemplate {
    private static final String DT_NAME = "name";
    private static final String DT_MODE = "mode";
    private static final String DT_TYPE = "type";
    private static final String DT_RANGE = "range";
    private static final String DT_DESCRIPTION = "desc";

    private static final String DT_TYPE_ENUM = "enum";
    private static final String DT_TYPE_NUMBER = "number";
    private static final String DT_TYPE_BOOL = "bool";
    private static final String DT_TYPE_STRING = "string";

    private static final String TAG = DataTemplate.class.getSimpleName();

    private ConcurrentHashMap<String, DataPoint> mDataMap;
    private IDataControlListener mDataControlListener;
    private ILocalDataListener mLocalDataListener;

    public DataTemplate(final JSONArray jsonArray) throws JSONException {
        mDataMap = new ConcurrentHashMap();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            String name = object.getString(DT_NAME);
            String mode = object.getString(DT_MODE);
            String type = object.getString(DT_TYPE);
            String description = object.getString(DT_DESCRIPTION);
            JSONArray rangeArray = object.getJSONArray(DT_RANGE);

            ArrayList<Object> range = new ArrayList<>();
            for (int j = 0; j < rangeArray.length(); j++) {
                range.add(rangeArray.get(j));
            }

            DataPoint dataPoint;
            switch (type) {
                case DT_TYPE_ENUM:
                    dataPoint = new EnumDataPoint(name, mode, type, description, range);
                    break;
                case DT_TYPE_NUMBER:
                    dataPoint = new NumberDataPoint(name, mode, type, description, range);
                    break;
                case DT_TYPE_BOOL:
                    dataPoint = new BoolDataPoint(name, mode, type, description, range);
                    break;
                case DT_TYPE_STRING:
                    dataPoint = new StringDataPoint(name, mode, type, description, range);
                    break;
                default:
                    QLog.e(TAG, "unknown data type = " + type);
                    return;
            }
            mDataMap.put(dataPoint.getName(), dataPoint);
        }
    }

    private DataPoint setDataPoint(String dataPointName, Object value) throws DeviceException {
        if (dataPointName == null || mDataMap.get(dataPointName) == null) {
            throw new DeviceException(String.format("%s no exist", dataPointName));
        }
        DataPoint dataPoint = mDataMap.get(dataPointName);
        dataPoint.setValue(value);
        onLocalDataChange();
        return dataPoint;
    }

    public void setDataPointByUser(String dataPointName, Object value, boolean commit) throws DeviceException {
        DataPoint dataPoint = setDataPoint(dataPointName, value);
        try {
            JSONObject jsonObject = dataPoint.genJsonObject();
            onUserChangeData(jsonObject, commit);
        } catch (JSONException e) {
            QLog.e(TAG, "genJsonObject error. dataPointName = " + dataPointName, e);
        }
    }

    public void onLocalDataChange() {
        if (mLocalDataListener != null) {
            try {
                JSONObject jsonObject = toJson();
                mLocalDataListener.onLocalDataChange(jsonObject);
            } catch (JSONException e) {
                QLog.e(TAG, "toJson error", e);
            }
        }
    }

    private void onUserChangeData(JSONObject userDesired, boolean commit) {
        if (mLocalDataListener != null) {
            mLocalDataListener.onUserChangeData(userDesired, commit);
        }
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        for (DataPoint dataPoint : mDataMap.values()) {
            object.put(dataPoint.getName(), dataPoint.getServerFormatValue(dataPoint.getValue()));
        }
        return object;
    }

    /**
     * 解析并处理来自服务端的控制消息
     *
     * @param key
     * @param value
     * @return
     */
    //TODO: 根据diff判断是否需要执行
    public boolean onControl(String key, Object value, boolean diff, boolean forInit) {
        if (mDataControlListener == null) {
            return false;
        }
        Object transformedValue = mDataMap.get(key).getClientFormatValue(value);
        DataPointControlPacket controlPacket = new DataPointControlPacket(key, transformedValue, diff, forInit);
        boolean result = mDataControlListener.onControlDataPoint(controlPacket);
        if (result) {
            try {
                setDataPoint(key, transformedValue);
            } catch (DeviceException e) {
                QLog.e(TAG, "onControl error", e);
            }
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

    public DataPoint getDataPoint(String dataPointName) throws DeviceException {
        if (dataPointName == null || mDataMap.get(dataPointName) == null) {
            throw new DeviceException(String.format("%s no exist", dataPointName));
        }
        DataPoint dataPoint = mDataMap.get(dataPointName);
        try {
            return (DataPoint) dataPoint.clone();
        } catch (CloneNotSupportedException e) {
            throw new DeviceException(e);
        }
    }

    /**
     * 监听来自服务端的控制消息
     * 每个接口，处理完成后需要返回true，才能够正确修改设备数据。
     */
    public interface IDataControlListener {
        boolean onControlDataPoint(DataPointControlPacket dataPointControlPacket);
    }

    public interface ILocalDataListener {
        void onLocalDataChange(JSONObject localData);

        void onUserChangeData(JSONObject userDesired, boolean commit);
    }
}
