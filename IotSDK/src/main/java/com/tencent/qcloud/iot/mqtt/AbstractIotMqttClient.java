package com.tencent.qcloud.iot.mqtt;

import android.os.Handler;
import android.os.HandlerThread;

import com.tencent.qcloud.iot.mqtt.callback.IMqttConnectStateCallback;
import com.tencent.qcloud.iot.mqtt.request.MqttPublishRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttSubscribeRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttUnSubscribeRequest;

import java.security.KeyStore;

/**
 * Created by rongerwu on 2018/1/12.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public abstract class AbstractIotMqttClient {
    private Handler mHandler;

    public AbstractIotMqttClient() {
    }

    /**
     * 建立mqtt连接
     * @param connectStateCallback 回调连接状态
     */
    public void connect(final IMqttConnectStateCallback connectStateCallback) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                connectInternal(connectStateCallback);
            }
        });
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        getHandler().removeCallbacksAndMessages(null);

        getHandler().post(new Runnable() {
            @Override
            public void run() {
                disconnectInternal();
            }
        });
    }

    /**
     * 用于子类中，连接失败或异常中断时重连
     * @param delay
     */
    protected void reconnect(int delay) {
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                reconnectInternal();
            }
        }, delay);
    }

    /**
     * 向topic发布消息
     * @param request
     */
    public void publish(final MqttPublishRequest request) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                publishInternal(request);
            }
        });
    }

    /**
     * 订阅topic
     * @param request
     */
    public void subscribe(final MqttSubscribeRequest request) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                subscribeInternal(request);
            }
        });
    }

    /**
     * 取消订阅
     * @param request
     */
    public void unSubscribe(final MqttUnSubscribeRequest request) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                unSubscribeInternal(request);
            }
        });
    }

    protected synchronized Handler getHandler() {
        if (mHandler == null) {
            HandlerThread handlerThread = new HandlerThread("QCloudIotMqttClient thread");
            handlerThread.start();
            mHandler = new Handler(handlerThread.getLooper());
        }
        return mHandler;
    }

    abstract protected void connectInternal(final IMqttConnectStateCallback connectStateCallback);

    abstract protected void disconnectInternal();

    abstract protected void reconnectInternal();

    abstract protected void publishInternal(final MqttPublishRequest request);

    abstract protected void subscribeInternal(final MqttSubscribeRequest request);

    abstract protected void unSubscribeInternal(final MqttUnSubscribeRequest request);
}
