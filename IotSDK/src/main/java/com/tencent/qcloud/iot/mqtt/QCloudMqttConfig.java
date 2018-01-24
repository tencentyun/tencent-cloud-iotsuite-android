package com.tencent.qcloud.iot.mqtt;

import android.text.TextUtils;

import com.tencent.qcloud.iot.common.ReconnectHelper;

import java.security.KeyStore;

/**
 * Created by rongerwu on 2018/1/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class QCloudMqttConfig {

    private static final int DEFAULT_KEEP_ALIVE_SECONDS = 10;

    /**
     * 控制台上获取的产品key
     */
    private String mProductKey = "unknown";

    /**
     * 控制台上获取的产品id
     */
    private String mProductId = "unknown";

    /**
     * 控制台上获取的设备名
     */
    private String mDeviceName = "unknown";

    /**
     * 控制台上获取的设备秘钥
     */
    private String mDeviceSecret = "unknown";

    /**
     * mqtt连接的username.
     * 直连模式的username从控制台获取.token模式的username通过http请求获取
     */
    private String mMqttUserName = "unknown";

    /**
     * mqtt连接的password.
     * 直连模式的password从控制台获取.token模式的password通过http请求获取
     */
    private String mMqttPassword = "unknown";

    /**
     * mqtt host.
     * 从控制台上获取.例如：mqtt-1d5mqrnp6.ap-guangzhou.mqtt.tencentcloudmq.com
     */
    private String mMqttHost = "unknown";

    /**
     * 连接模式.分为直连模式和token模式
     */
    private QCloudMqttConnectionMode mConnectionMode = QCloudMqttConnectionMode.MODE_DIRECT;

    private KeyStore mKeyStore;

    /**
     * 设置是否在连接失败或连接异常断开时自动重连
     */
    private boolean mAutoReconnect = true;

    /**
     * 最小重连时间间隔
     */
    private int mMinRetryTimeMs = ReconnectHelper.MIN_RETRY_TIME_MS;

    /**
     * 最大重连时间间隔
     */
    private int mMaxRetryTimeMs = ReconnectHelper.MAX_RETRY_TIME_MS;

    /**
     * 最大重试次数
     */
    private int mMaxRetryTimes = ReconnectHelper.MAX_RETRIES;

    /**
     * 检测mqtt连接是否存活的间隔
     */
    private int mKeepAliveSeconds = DEFAULT_KEEP_ALIVE_SECONDS;

    public QCloudMqttConfig(String mqttHost, String productKey, String deviceName, String deviceSecret) {
        if (TextUtils.isEmpty(mqttHost)) {
            throw new IllegalArgumentException("mqttHost is empty");
        }
        if (TextUtils.isEmpty(productKey)) {
            throw new IllegalArgumentException("productKey is empty");
        }
        if (TextUtils.isEmpty(deviceName)) {
            throw new IllegalArgumentException("deviceName is empty");
        }
        if (TextUtils.isEmpty(deviceSecret)) {
            throw new IllegalArgumentException("deviceSecret is empty");
        }
        mMqttHost = mqttHost;
        mProductKey = productKey;
        mDeviceName = deviceName;
        mDeviceSecret = deviceSecret;
    }

    public String getProductKey() {
        return mProductKey;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getDeviceSecret() {
        return mDeviceSecret;
    }

    public String getMqttUserName() {
        return mMqttUserName;
    }

    public String getProductId() {
        return mProductId;
    }

    public QCloudMqttConfig setProductId(String productId) {
        if (TextUtils.isEmpty(productId)) {
            throw new IllegalArgumentException("productId is empty");
        }
        mProductId = productId;
        return this;
    }

    public QCloudMqttConfig setMqttUserName(String mqttUserName) {
        if (TextUtils.isEmpty(mqttUserName)) {
            throw new IllegalArgumentException("mqttUserName is empty");
        }
        mMqttUserName = mqttUserName;
        return this;
    }

    public String getMqttPassword() {
        return mMqttPassword;
    }

    public QCloudMqttConfig setMqttPassword(String mqttPassword) {
        if (TextUtils.isEmpty(mqttPassword)) {
            throw new IllegalArgumentException("mqttPassword is empty");
        }
        mMqttPassword = mqttPassword;
        return this;
    }

    public QCloudMqttConnectionMode getConnectionMode() {
        return mConnectionMode;
    }

    public QCloudMqttConfig setConnectionMode(QCloudMqttConnectionMode connectionMode) {
        if (connectionMode == null) {
            throw new IllegalArgumentException("connectionMode is null");
        }
        mConnectionMode = connectionMode;
        return this;
    }

    public String getMqttHost() {
        return mMqttHost;
    }

    public QCloudMqttConfig setMqttHost(String mqttHost) {
        if (TextUtils.isEmpty(mqttHost)) {
            throw new IllegalArgumentException("mqttHost is empty");
        }
        mMqttHost = mqttHost;
        return this;
    }

    public KeyStore getKeyStore() {
        return mKeyStore;
    }

    public QCloudMqttConfig setKeyStore(KeyStore keyStore) {
        mKeyStore = keyStore;
        return this;
    }

    public boolean isAutoReconnect() {
        return mAutoReconnect;
    }

    public QCloudMqttConfig setAutoReconnect(boolean autoReconnect) {
        mAutoReconnect = autoReconnect;
        return this;
    }

    public int getMinRetryTimeMs() {
        return mMinRetryTimeMs;
    }

    public QCloudMqttConfig setMinRetryTimeMs(int minRetryTimeMs) {
        mMinRetryTimeMs = minRetryTimeMs;
        return this;
    }

    public int getMaxRetryTimeMs() {
        return mMaxRetryTimeMs;
    }

    public QCloudMqttConfig setMaxRetryTimeMs(int maxRetryTimeMs) {
        mMaxRetryTimeMs = maxRetryTimeMs;
        return this;
    }

    public int getMaxRetryTimes() {
        return mMaxRetryTimes;
    }

    public QCloudMqttConfig setMaxRetryTimes(int maxRetryTimes) {
        mMaxRetryTimes = maxRetryTimes;
        return this;
    }

    public int getKeepAliveSeconds() {
        return mKeepAliveSeconds;
    }

    public QCloudMqttConfig setKeepAliveSeconds(int keepAliveSeconds) {
        mKeepAliveSeconds = keepAliveSeconds;
        return this;
    }

    public enum QCloudMqttConnectionMode {
        MODE_DIRECT,
        MODE_TOKEN,
    }
}
