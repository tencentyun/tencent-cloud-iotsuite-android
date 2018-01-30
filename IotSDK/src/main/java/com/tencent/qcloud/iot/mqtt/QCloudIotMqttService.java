package com.tencent.qcloud.iot.mqtt;

import com.tencent.qcloud.iot.mqtt.callback.IMqttConnectStateCallback;
import com.tencent.qcloud.iot.mqtt.callback.IMqttMessageListener;
import com.tencent.qcloud.iot.mqtt.request.MqttPublishRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttSubscribeRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttUnSubscribeRequest;

/**
 * Created by rongerwu on 2018/1/30.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class QCloudIotMqttService {
    private static final String TAG = QCloudIotMqttService.class.getSimpleName();
    private QCloudIotMqttClient mQCloudIotMqttClient;

    public QCloudIotMqttService(QCloudMqttConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null!");
        }
        mQCloudIotMqttClient = new QCloudIotMqttClient(config);
    }

    public void setMqttMessageListener(IMqttMessageListener mqttMessageListener) {
        mQCloudIotMqttClient.setMqttMessageListener(mqttMessageListener);
    }

    public void connect(final IMqttConnectStateCallback connectStateCallback) {
        mQCloudIotMqttClient.connect(connectStateCallback);
    }

    public void disconnect() {
        mQCloudIotMqttClient.disconnect();
    }

    public void publish(final MqttPublishRequest request) {
        mQCloudIotMqttClient.publish(request);
    }

    public void subscribe(final MqttSubscribeRequest request) {
        mQCloudIotMqttClient.subscribe(request);
    }

    public void unSubscribe(final MqttUnSubscribeRequest request) {
        mQCloudIotMqttClient.unSubscribe(request);
    }
}
