package com.tencent.qcloud.iot.device.data;

/**
 * Created by rongerwu on 2018/4/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public interface IDataEventListener {
    /**
     * 控制事件，可能是收到服务端的control，也可能是getShadow后触发
     * @param key key
     * @param value value
     * @param diff 标识value的是否和设备当前状态一致，如果一致，可以不做处理。
     * @param forInit 标识是否用于SDK启动后第一次设备初始化
     */
    void onControl(String key, Object value, boolean diff, boolean forInit);
}
