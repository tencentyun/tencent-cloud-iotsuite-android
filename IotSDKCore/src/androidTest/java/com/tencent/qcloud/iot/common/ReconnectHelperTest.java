package com.tencent.qcloud.iot.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by rongerwu on 2018/1/14.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class ReconnectHelperTest {
    private ReconnectHelper mReconnectHelper;

    public ReconnectHelperTest() {
        mReconnectHelper = new ReconnectHelper(true);
    }

    @Test
    public void testShouldRetry() {
        ReconnectHelper reconnectHelper1 = new ReconnectHelper(true);
        Assert.assertTrue(reconnectHelper1.shouldRetry());

        reconnectHelper1.setMaxRetryTimes(2);
        reconnectHelper1.addRetryTimes();
        Assert.assertTrue(reconnectHelper1.shouldRetry());
        reconnectHelper1.addRetryTimes();
        Assert.assertFalse(reconnectHelper1.shouldRetry());

        ReconnectHelper reconnectHelper2 = new ReconnectHelper(false);
        Assert.assertFalse(reconnectHelper2.shouldRetry());
    }

    @Test
    public void testGetRetryDelay() {
        mReconnectHelper.setMinRetryTimeMs(1000);
        mReconnectHelper.setMaxRetryTimeMs(10000);
        Assert.assertEquals(mReconnectHelper.getRetryDelay(), 1000);
        for (int i = 0; i < 4; i++) {
            mReconnectHelper.addRetryTimes();
        }
        Assert.assertEquals(mReconnectHelper.getRetryDelay(), 10000);
    }

    @Test
    public void testSetMinRetryTimeMs() {
        try {
            mReconnectHelper.setMinRetryTimeMs(0);
            Assert.fail("must larger then 0");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testSetMaxRetryTimeMs() {
        try {
            mReconnectHelper.setMinRetryTimeMs(10);
            mReconnectHelper.setMaxRetryTimeMs(10);
            Assert.fail("must larger then min retry time");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testSetMaxRetryTimes() {
        try {
            mReconnectHelper.setMaxRetryTimes(0);
            Assert.fail("must larger then 0");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }
}
