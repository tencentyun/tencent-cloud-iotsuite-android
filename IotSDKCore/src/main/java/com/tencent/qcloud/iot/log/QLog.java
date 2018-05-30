package com.tencent.qcloud.iot.log;

import android.util.Log;

/**
 * Created by rongerwu on 2018/1/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class QLog {
    public static final int QLOG_LEVEL_DEBUG = 1;
    public static final int QLOG_LEVEL_INFO = 2;
    public static final int QLOG_LEVEL_WARN = 3;
    public static final int QLOG_LEVEL_ERROR = 4;
    public static final int QLOG_LEVEL_NONE = 5;

    private static int mLogLevel = QLOG_LEVEL_DEBUG;

    public static void setLogLevel(int logLevel) {
        if (logLevel < QLOG_LEVEL_DEBUG || logLevel > QLOG_LEVEL_NONE) {
            throw new IllegalArgumentException("illegal log level");
        }
        mLogLevel = logLevel;
    }

    public static boolean d(String tag, String message) {
        if (mLogLevel > QLOG_LEVEL_DEBUG) {
            return false;
        }
        Log.d(tag, message);
        return true;
    }

    public static boolean d(String tag, String message, Throwable throwable) {
        if (mLogLevel > QLOG_LEVEL_DEBUG) {
            return false;
        }
        Log.d(tag, message, throwable);
        return true;
    }

    public static boolean i(String tag, String message) {
        if (mLogLevel > QLOG_LEVEL_INFO) {
            return false;
        }
        Log.i(tag, message);
        return true;
    }

    public static boolean i(String tag, String message, Throwable throwable) {
        if (mLogLevel > QLOG_LEVEL_INFO) {
            return false;
        }
        Log.i(tag, message, throwable);
        return true;
    }

    public static boolean w(String tag, String message) {
        if (mLogLevel > QLOG_LEVEL_WARN) {
            return false;
        }
        Log.w(tag, message);
        return true;
    }

    public static boolean w(String tag, String message, Throwable throwable) {
        if (mLogLevel > QLOG_LEVEL_WARN) {
            return false;
        }
        Log.w(tag, message, throwable);
        return true;
    }

    public static boolean e(String tag, String message) {
        if (mLogLevel > QLOG_LEVEL_ERROR) {
            return false;
        }
        Log.e(tag, message);
        return true;
    }

    public static boolean e(String tag, String message, Throwable throwable) {
        if (mLogLevel > QLOG_LEVEL_ERROR) {
            return false;
        }
        Log.e(tag, message, throwable);
        return true;
    }
}
