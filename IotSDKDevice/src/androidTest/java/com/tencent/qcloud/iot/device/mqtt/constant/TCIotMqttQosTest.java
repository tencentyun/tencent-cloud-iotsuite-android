package com.tencent.qcloud.iot.device.mqtt.constant;

import com.tencent.qcloud.iot.device.mqtt.constant.TCIotMqttQos;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by rongerwu on 2018/1/14.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class TCIotMqttQosTest {

    @Test
    public void testAsInt() {
        Assert.assertEquals(TCIotMqttQos.QOS0.asInt(), 0);
        Assert.assertEquals(TCIotMqttQos.QOS1.asInt(), 1);
    }
}
