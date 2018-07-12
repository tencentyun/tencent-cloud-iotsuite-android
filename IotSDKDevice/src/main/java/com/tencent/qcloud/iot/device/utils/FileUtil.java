package com.tencent.qcloud.iot.device.utils;

import android.content.Context;
import android.content.res.AssetManager;

import com.tencent.qcloud.iot.utils.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by rongerwu on 2018/6/4.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class FileUtil {

    public static String getAssetFileString(Context context, String fileName) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        AssetManager assetManager = context.getAssets();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(assetManager.open(fileName), StringUtil.UTF8));
        try {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } finally {
            bufferedReader.close();
        }
        return stringBuilder.toString();
    }
}
