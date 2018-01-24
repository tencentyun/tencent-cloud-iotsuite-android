/*
 * Created by rongerwu on 18-1-17 上午11:46
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved
 */

package com.tencent.qcloud.iot.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by rongerwu on 2018/1/17.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class QLogTest {
    private static final String TAG = QLogTest.class.getSimpleName();

    @Test
    public void testConstructor() {
        new QLog();
    }

    @Test
    public void testSetLogLevel() {
        try {
            QLog.setLogLevel(0);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }

        try {
            QLog.setLogLevel(6);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testLevelDebug() {
        QLog.setLogLevel(QLog.QLOG_LEVEL_DEBUG);

        Assert.assertTrue(QLog.d(TAG, "debug"));
        Assert.assertTrue(QLog.d(TAG, "debug", null));

        Assert.assertTrue(QLog.i(TAG, "info"));
        Assert.assertTrue(QLog.i(TAG, "info", null));

        Assert.assertTrue(QLog.w(TAG, "warn"));
        Assert.assertTrue(QLog.w(TAG, "warn", null));

        Assert.assertTrue(QLog.e(TAG, "error"));
        Assert.assertTrue(QLog.e(TAG, "error", null));
    }

    @Test
    public void testLevelInfo() {
        QLog.setLogLevel(QLog.QLOG_LEVEL_INFO);

        Assert.assertFalse(QLog.d(TAG, "debug"));
        Assert.assertFalse(QLog.d(TAG, "debug", null));

        Assert.assertTrue(QLog.i(TAG, "info"));
        Assert.assertTrue(QLog.i(TAG, "info", null));

        Assert.assertTrue(QLog.w(TAG, "warn"));
        Assert.assertTrue(QLog.w(TAG, "warn", null));

        Assert.assertTrue(QLog.e(TAG, "error"));
        Assert.assertTrue(QLog.e(TAG, "error", null));
    }

    @Test
    public void testLevelWarn() {
        QLog.setLogLevel(QLog.QLOG_LEVEL_WARN);

        Assert.assertFalse(QLog.d(TAG, "debug"));
        Assert.assertFalse(QLog.d(TAG, "debug", null));

        Assert.assertFalse(QLog.i(TAG, "info"));
        Assert.assertFalse(QLog.i(TAG, "info", null));

        Assert.assertTrue(QLog.w(TAG, "warn"));
        Assert.assertTrue(QLog.w(TAG, "warn", null));

        Assert.assertTrue(QLog.e(TAG, "error"));
        Assert.assertTrue(QLog.e(TAG, "error", null));
    }

    @Test
    public void testLevelError() {
        QLog.setLogLevel(QLog.QLOG_LEVEL_ERROR);

        Assert.assertFalse(QLog.d(TAG, "debug"));
        Assert.assertFalse(QLog.d(TAG, "debug", null));

        Assert.assertFalse(QLog.i(TAG, "info"));
        Assert.assertFalse(QLog.i(TAG, "info", null));

        Assert.assertFalse(QLog.w(TAG, "warn"));
        Assert.assertFalse(QLog.w(TAG, "warn", null));

        Assert.assertTrue(QLog.e(TAG, "error"));
        Assert.assertTrue(QLog.e(TAG, "error", null));
    }

    @Test
    public void testLevelNone() {
        QLog.setLogLevel(QLog.QLOG_LEVEL_NONE);

        Assert.assertFalse(QLog.d(TAG, "debug"));
        Assert.assertFalse(QLog.d(TAG, "debug", null));

        Assert.assertFalse(QLog.i(TAG, "info"));
        Assert.assertFalse(QLog.i(TAG, "info", null));

        Assert.assertFalse(QLog.w(TAG, "warn"));
        Assert.assertFalse(QLog.w(TAG, "warn", null));

        Assert.assertFalse(QLog.e(TAG, "error"));
        Assert.assertFalse(QLog.e(TAG, "error", null));
    }

}
