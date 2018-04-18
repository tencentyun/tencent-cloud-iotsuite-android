package com.tencent.qcloud.iot.mqtt;

import com.tencent.qcloud.iot.mqtt.callback.IMqttActionCallback;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by rongerwu on 2018/1/17.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class MqttActionUserContextTest {
    @Test
    public void testSetGet() {

        String tempActionType = "tempActionType";
        String tempTopic = "tempTopic";
        IMqttActionCallback tempCallback = new IMqttActionCallback() {

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(Throwable exception) {

            }
        };

        MqttActionUserContext userContext = new MqttActionUserContext()
                .setActionType(tempActionType)
                .setTopic(tempTopic)
                .setCallback(tempCallback);
        Assert.assertTrue(tempActionType.equals(userContext.getActionType()));
        Assert.assertTrue(tempTopic.equals(userContext.getTopic()));
        Assert.assertEquals(tempCallback, userContext.getCallback());

        try {
            userContext.setActionType("");
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }

        try {
            userContext.setTopic("");
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }
}
