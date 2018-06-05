package com.tencent.qcloud.iot.sample.utils;

/**
 * Created by rongerwu on 2018/5/31.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class ByteUtil {
    public static String toBinaryString(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (byte byteVal : bytes) {
            String strVal = String.format("%8s", Integer.toBinaryString(byteVal & 0xFF)).replace(' ', '0');
            stringBuffer.append(strVal).append(" ");
        }
        return stringBuffer.toString();
    }
}
