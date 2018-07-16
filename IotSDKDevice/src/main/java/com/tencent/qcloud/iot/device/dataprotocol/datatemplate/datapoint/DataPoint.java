package com.tencent.qcloud.iot.device.dataprotocol.datatemplate.datapoint;

import com.tencent.qcloud.iot.device.exception.DeviceException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by rongerwu on 2018/6/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public abstract class DataPoint implements Cloneable {

    protected final String mName;
    protected final boolean mWriteable;
    protected final String mType;
    protected final String mDescription;
    protected final ArrayList<Object> mRange;

    protected Object mValue;

    public DataPoint(String name, String mode, String type, String description, ArrayList<Object> range) throws JSONException {
        mName = name;
        mWriteable = mode.contains("w");
        mType = type;
        mDescription = description;
        mRange = range;

        initValue();
    }

    public String getName() {
        return mName;
    }

    public boolean isWriteable() {
        return mWriteable;
    }

    public String getType() {
        return mType;
    }

    public Object getValue() {
        return mValue;
    }

    public DataPoint setValue(Object value) throws DeviceException {
        if (!mWriteable) {
            throw new DeviceException("not writeable, name = " + mName);
        }
        if (!checkValueType(value)) {
            throw new DeviceException("illegal value type, name = " + mName + ", value = " + value);
        } else if (!checkValueRange(value)) {
            throw new DeviceException("out of range, name = " + mName + ", value = " + value + ", range = " + mRange.toString());
        }
        mValue = value;
        return this;
    }

    public JSONObject genJsonObject() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(mName, mValue);
        return object;
    }

    /**
     * 转为client需要的格式，例如枚举型需要从int转为String
     *
     * @param value
     * @return
     */
    public Object getClientFormatValue(Object value) {
        return value;
    }

    /**
     * 转为server需要的格式，例如枚举型需要从String转为int
     *
     * @param value
     * @return
     */
    public Object getServerFormatValue(Object value) {
        return value;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    protected abstract void initValue();

    protected abstract boolean checkValueType(Object value);

    protected abstract boolean checkValueRange(Object value);

}
