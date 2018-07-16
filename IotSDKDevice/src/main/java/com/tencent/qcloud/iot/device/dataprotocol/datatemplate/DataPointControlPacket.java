package com.tencent.qcloud.iot.device.dataprotocol.datatemplate;

/**
 * Created by rongerwu on 2018/6/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class DataPointControlPacket {
    private String mName;
    private Object mValue;

    /**
     * mDiff: 标识value是否和该数据点的当前值不相等。
     */
    private boolean mDiff;

    /**
     * mForInit 标识是否用于SDK启动后第一次设备初始化。
     */
    private boolean mForInit;

    public DataPointControlPacket(String name, Object value, boolean diff, boolean forInit) {
        mName = name;
        mValue = value;
        mDiff = diff;
        mForInit = forInit;
    }

    public String getName() {
        return mName;
    }

    public Object getValue() {
        return mValue;
    }

    public boolean isDiff() {
        return mDiff;
    }

    public boolean isForInit() {
        return mForInit;
    }

    @Override
    public String toString() {
        return "name = " + mName + ", value = " + mValue + ", diff = " + mDiff + ", forInit = " + mForInit;
    }
}
