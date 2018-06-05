package com.tencent.qcloud.iot.device.datatemplate.datapoint;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by rongerwu on 2018/6/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class StringDataPoint extends DataPoint {
    public StringDataPoint(String name, String mode, String type, String description, ArrayList<Object> range) throws JSONException {
        super(name, mode, type, description, range);
    }

    @Override
    protected void initValue() {
        int minLen = ((Number) mRange.get(0)).intValue();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < minLen; i++) {
            stringBuilder.append(" ");
        }
        mValue = stringBuilder.toString();
    }

    @Override
    protected boolean checkValueType(Object value) {
        return (value instanceof String);
    }

    @Override
    protected boolean checkValueRange(Object value) {
        int minLen = ((Number) mRange.get(0)).intValue();
        int maxLen = ((Number) mRange.get(1)).intValue();
        int stringLen = ((String) value).length();

        return (stringLen >= minLen && stringLen <= maxLen);
    }
}
