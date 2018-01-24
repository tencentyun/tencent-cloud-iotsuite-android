package com.tencent.qcloud.iot.mqtt.constant;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by rongerwu on 2018/1/14.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class QCloudIotMqttQosTest {

    @Test
    public void testAsInt() {
        Assert.assertEquals(QCloudIotMqttQos.QOS0.asInt(), 0);
        Assert.assertEquals(QCloudIotMqttQos.QOS1.asInt(), 1);
    }
}
