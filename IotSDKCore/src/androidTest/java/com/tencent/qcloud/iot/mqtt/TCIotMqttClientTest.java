package com.tencent.qcloud.iot.mqtt;

import android.util.Log;

import com.tencent.qcloud.iot.mqtt.callback.IMqttConnectStateCallback;
import com.tencent.qcloud.iot.mqtt.certificate.CertificateProviderTest;
import com.tencent.qcloud.iot.mqtt.constant.MqttConnectState;
import com.tencent.qcloud.iot.mqtt.request.MqttPublishRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttSubscribeRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttUnSubscribeRequest;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyStore;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by rongerwu on 2018/1/12.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class TCIotMqttClientTest {
    private static final int MIN_RETRY_TIME_MS = 1000;
    private static final int MAX_RETRY_TIME_MS = 2000;
    private static final String TAG = TCIotMqttClientTest.class.getSimpleName();
    private TCIotMqttClient mTCIotMqttClient;
    private CountDownLatch mCountDownLatch;
    private KeyStore mKeyStore;

    public TCIotMqttClientTest() throws IOException {

    }

    @Test
    public void testConstructor() {
        try {
            new TCIotMqttClient(null);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testConnect() throws IOException, InterruptedException {

        mTCIotMqttClient.connect(new IMqttConnectStateCallback() {
            @Override
            public void onStateChanged(MqttConnectState state) {
                Log.d(TAG, "onStateChanged: " + state);
                if (state == MqttConnectState.CLOSED || state == MqttConnectState.CONNECTED) {
                    mCountDownLatch.countDown();
                }
            }
        });
        boolean success = mCountDownLatch.await(5000, TimeUnit.MILLISECONDS);
        Assert.assertTrue(success);

        try {
            mTCIotMqttClient.connectInternal(null);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }
    }

    @Test
    public void testDisconnect() {
        try {
            mTCIotMqttClient.disconnectInternal();
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }

        mTCIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                mTCIotMqttClient.disconnectInternal();
            }
        });
    }

    @Test
    public void testPublish() {
        final MqttPublishRequest request = new MqttPublishRequest();
        try {
            mTCIotMqttClient.publishInternal(request);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }

        mTCIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    mTCIotMqttClient.publishInternal(null);
                    Assert.fail();
                } catch (Exception e) {
                    Assert.assertTrue(e instanceof IllegalArgumentException);
                }
            }
        });

        mTCIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                mTCIotMqttClient.publishInternal(request);
            }
        });
    }

    @Test
    public void testSubcribe() {
        final MqttSubscribeRequest request = new MqttSubscribeRequest();
        try {
            mTCIotMqttClient.subscribeInternal(request);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }

        mTCIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    mTCIotMqttClient.subscribeInternal(null);
                    Assert.fail();
                } catch (Exception e) {
                    Assert.assertTrue(e instanceof IllegalArgumentException);
                }
            }
        });

        mTCIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                mTCIotMqttClient.subscribeInternal(request);
            }
        });
    }

    @Test
    public void testUnSubcribe() {
        final MqttUnSubscribeRequest request = new MqttUnSubscribeRequest();
        try {
            mTCIotMqttClient.unSubscribeInternal(request);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }

        mTCIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    mTCIotMqttClient.unSubscribeInternal(null);
                    Assert.fail();
                } catch (Exception e) {
                    Assert.assertTrue(e instanceof IllegalArgumentException);
                }
            }
        });

        mTCIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                mTCIotMqttClient.unSubscribeInternal(request);
            }
        });
    }

    private void connect() {
        mCountDownLatch = new CountDownLatch(1);
        mTCIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                mCountDownLatch.countDown();
                mTCIotMqttClient.connectInternal(null);
            }
        });
        try {
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPublish2() {
        connect();
        testPublish();
    }

    @Test
    public void testSubscribe2() {
        connect();
        testSubcribe();
    }

    @Test
    public void testUnSubscribe2() {
        connect();
        testUnSubcribe();
    }
}
