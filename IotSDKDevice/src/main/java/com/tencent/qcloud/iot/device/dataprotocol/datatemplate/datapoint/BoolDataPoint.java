package com.tencent.qcloud.iot.device.dataprotocol.datatemplate.datapoint;

import java.util.ArrayList;

/**
 * Created by rongerwu on 2018/6/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class BoolDataPoint extends DataPoint {
    public BoolDataPoint(String name, String mode, String type, String description, ArrayList<Object> range) {
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

    @Override
    public Object getClientFormatValue(Object value) {
        //0 == false, 1 == true
        return ((int) value == 1);
    }

    @Override
    public Object getServerFormatValue(Object value) {
        if ((boolean) value) {
            return 1;
        } else {
            return 0;
        }
    }
}
