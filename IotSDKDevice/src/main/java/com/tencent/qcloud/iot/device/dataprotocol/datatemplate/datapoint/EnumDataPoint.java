package com.tencent.qcloud.iot.device.dataprotocol.datatemplate.datapoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by rongerwu on 2018/6/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class EnumDataPoint extends DataPoint {
    public EnumDataPoint(String name, String mode, String type, String description, ArrayList<Object> range) throws JSONException {
        super(name, mode, type, description, range);
    }

    @Override
    protected void initValue() {
        mValue = (mRange.size() > 0) ? mRange.get(0) : "";
    }

    @Override
    protected boolean checkValueType(Object value) {
        return (value instanceof String);
    }

    @Override
    protected boolean checkValueRange(Object value) {
        return (mRange.contains(value));
    }

    @Override
    public JSONObject genJsonObject() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(mName, mRange.indexOf(mValue));
        return object;
    }

    @Override
    public Object getClientFormatValue(Object value) {
        return mRange.get((int) value);
    }

    @Override
    public Object getServerFormatValue(Object value) {
        return mRange.indexOf(value);
    }
}
