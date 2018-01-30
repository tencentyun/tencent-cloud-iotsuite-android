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

public class QCloudIotMqttClientTest {
    private static final int MIN_RETRY_TIME_MS = 1000;
    private static final int MAX_RETRY_TIME_MS = 2000;
    private static final String TAG = QCloudIotMqttClientTest.class.getSimpleName();
    private QCloudIotMqttClient mQCloudIotMqttClient;
    private CountDownLatch mCountDownLatch;
    private KeyStore mKeyStore;

    public QCloudIotMqttClientTest() throws IOException {
        QCloudMqttConfig config = new QCloudMqttConfig("mqtt-m2i58z3s.ap-guangzhou.mqtt.tencentcloudmq.com", "mqtt-m2i58z3s", "test_android_1", "48bf05179b6f1be3b38c89f27c804f11")
                .setProductId("iot-6xzr8ap8")
                .setConnectionMode(QCloudMqttConfig.QCloudMqttConnectionMode.MODE_DIRECT)
                .setMqttUserName("AKIDNgssgTw1pW2NahKR4oRt9D6ofNuGgSKG")
                .setMqttPassword("085Nmo6yhgR/TMjSPfFWP+TEVrggjVNFtAyvZUCxp0U=")
                .setMinRetryTimeMs(MIN_RETRY_TIME_MS)
                .setMaxRetryTimeMs(MAX_RETRY_TIME_MS)
                .setMaxRetryTimes(2);
        mQCloudIotMqttClient = new QCloudIotMqttClient(config);
        mCountDownLatch = new CountDownLatch(1);
        CertificateProviderTest certificateProvider = new CertificateProviderTest();
        //mKeyStore = certificateProvider.getKeyStore();
    }

    @Test
    public void testConstructor() {
        try {
            new QCloudIotMqttClient(null);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testConnect() throws IOException, InterruptedException {

        mQCloudIotMqttClient.connect(new IMqttConnectStateCallback() {
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
            mQCloudIotMqttClient.connectInternal(null);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }
    }

    @Test
    public void testDisconnect() {
        try {
            mQCloudIotMqttClient.disconnectInternal();
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }

        mQCloudIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                mQCloudIotMqttClient.disconnectInternal();
            }
        });
    }

    @Test
    public void testPublish() {
        final MqttPublishRequest request = new MqttPublishRequest();
        try {
            mQCloudIotMqttClient.publishInternal(request);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }

        mQCloudIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    mQCloudIotMqttClient.publishInternal(null);
                    Assert.fail();
                } catch (Exception e) {
                    Assert.assertTrue(e instanceof IllegalArgumentException);
                }
            }
        });

        mQCloudIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                mQCloudIotMqttClient.publishInternal(request);
            }
        });
    }

    @Test
    public void testSubcribe() {
        final MqttSubscribeRequest request = new MqttSubscribeRequest();
        try {
            mQCloudIotMqttClient.subscribeInternal(request);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }

        mQCloudIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    mQCloudIotMqttClient.subscribeInternal(null);
                    Assert.fail();
                } catch (Exception e) {
                    Assert.assertTrue(e instanceof IllegalArgumentException);
                }
            }
        });

        mQCloudIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                mQCloudIotMqttClient.subscribeInternal(request);
            }
        });
    }

    @Test
    public void testUnSubcribe() {
        final MqttUnSubscribeRequest request = new MqttUnSubscribeRequest();
        try {
            mQCloudIotMqttClient.unSubscribeInternal(request);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }

        mQCloudIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    mQCloudIotMqttClient.unSubscribeInternal(null);
                    Assert.fail();
                } catch (Exception e) {
                    Assert.assertTrue(e instanceof IllegalArgumentException);
                }
            }
        });

        mQCloudIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                mQCloudIotMqttClient.unSubscribeInternal(request);
            }
        });
    }

    private void connect() {
        mCountDownLatch = new CountDownLatch(1);
        mQCloudIotMqttClient.getHandler().post(new Runnable() {
            @Override
            public void run() {
                mCountDownLatch.countDown();
                mQCloudIotMqttClient.connectInternal(null);
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
