package com.tencent.qcloud.iot.device.datatemplate.datapoint;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by rongerwu on 2018/6/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class BoolDataPoint extends DataPoint {
    public BoolDataPoint(String name, String mode, String type, String description, ArrayList<Object> range) throws JSONException {
        super(name, mode, type, description, range);
    }

    @Override
    protected void initValue() {
        mValue = false;
    }

    @Override
    protected boolean checkValueType(Object value) {
        return (value instanceof Boolean);
    }

    @Override
    protected boolean checkValueRange(Object value) {
        return true;
    }
}
