package com.tencent.qcloud.iot.sample;

import android.app.Application;

import com.tencent.qcloud.iot.device.TCIotDeviceService;

/**
 * Created by rongerwu on 2018/6/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化iot SDK
        TCIotDeviceService.init(this);
    }
}
