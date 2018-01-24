package com.tencent.qcloud.iot.common;

/**
 * Created by rongerwu on 2018/1/10.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

/**
 * 连接失败或断开时的重连帮助类.
 * 两次重连间隔的逻辑见getRetryDelay.
 */
public class ReconnectHelper {
    public final static int MIN_RETRY_TIME_MS = 3000;
    public final static int MAX_RETRY_TIME_MS = 30000;
    public final static int MAX_RETRIES = 5;

    private boolean mAutoReconnect;
    private int mMinRetryTimeMs = MIN_RETRY_TIME_MS;
    private int mMaxRetryTimeMs = MAX_RETRY_TIME_MS;
    private int mMaxRetryTimes = MAX_RETRIES;
    private int mRetryTimes;

    public ReconnectHelper(boolean autoReconnect) {
        mAutoReconnect = autoReconnect;
        reset();
    }

    public void reset() {
        mRetryTimes = 0;
    }

    public boolean shouldRetry() {
        return (mAutoReconnect && mRetryTimes < mMaxRetryTimes);
    }

    /**
     * 获取下一次重连的间隔时长.
     * @return
     */
    public int getRetryDelay() {
        return (int) Math.min(mMinRetryTimeMs * (long) Math.pow(2, mRetryTimes), mMaxRetryTimeMs);
    }

    public void addRetryTimes() {
        mRetryTimes++;
    }

    public ReconnectHelper setMinRetryTimeMs(int minRetryTimeMs) {
        if (minRetryTimeMs <= 0) {
            throw new IllegalArgumentException("minRetryTimeMs must large then 0");
        }
        mMinRetryTimeMs = minRetryTimeMs;
        return this;
    }

    public ReconnectHelper setMaxRetryTimeMs(int maxRetryTimeMs) {
        if (maxRetryTimeMs <= mMinRetryTimeMs) {
            throw new IllegalArgumentException("maxRetryTimeMs must large then minRetryTimeMs");
        }
        mMaxRetryTimeMs = maxRetryTimeMs;
        return this;
    }

    public ReconnectHelper setMaxRetryTimes(int maxRetryTimes) {
        if (maxRetryTimes <= 0) {
            throw new IllegalArgumentException("maxRetryTimes must large then 0");
        }
        mMaxRetryTimes = maxRetryTimes;
        return this;
    }
}
