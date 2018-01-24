package com.tencent.qcloud.iot.mqtt.callback;

/**
 * Created by rongerwu on 2018/1/17.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

/**
 * 监听mqtt topic的消息
 */
public interface IMqttMessageListener {
    /**
     * 收到服务端所订阅topic发送过来的消息时触发.
     *
     * @param topic
     * @param message
     */
    void onMessageArrived(String topic, String message);
}
