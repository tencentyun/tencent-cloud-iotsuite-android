package com.tencent.qcloud.iot.device.dataprotocol.datatemplate.datapoint;

import java.util.ArrayList;

/**
 * Created by rongerwu on 2018/6/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class NumberDataPoint extends DataPoint {
    public NumberDataPoint(String name, String mode, String type, String description, ArrayList<Object> range) {
        super(name, mode, type, description, range);
    }

    @Override
    protected void initValue() {
        mValue = ((Number) mRange.get(0)).doubleValue();
    }

    @Override
    protected boolean checkValueType(Object value) {
        return (value instanceof Number);
    }

    @Override
    protected boolean checkValueRange(Object value) {
        double min = ((Number) mRange.get(0)).doubleValue();
        double max = ((Number) mRange.get(1)).doubleValue();
        double doubleValue = ((Number) value).doubleValue();

        return (doubleValue >= min && doubleValue <= max);
    }
}
