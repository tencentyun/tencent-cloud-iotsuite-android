package com.tencent.qcloud.iot.utils;

import junit.framework.Assert;

import org.junit.Test;

import java.nio.charset.Charset;

/**
 * Created by rongerwu on 2018/1/14.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class StringUtilTest {

    @Test
    public void testValue() {
        Assert.assertEquals(StringUtil.UTF8, Charset.forName("UTF-8"));
    }
}
