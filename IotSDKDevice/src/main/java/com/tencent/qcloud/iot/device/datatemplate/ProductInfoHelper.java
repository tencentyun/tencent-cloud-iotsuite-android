package com.tencent.qcloud.iot.device.datatemplate;

import com.tencent.qcloud.iot.mqtt.TCMqttConfig;

/**
 * Created by rongerwu on 2018/4/19.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class ProductInfoHelper {
    private JsonFileData mJsonFileData;

    public ProductInfoHelper(JsonFileData jsonFileData) {
        mJsonFileData = jsonFileData;
    }

    /**
     * 根据产品信息，生成配置类实例
     *
     * @return
     */
    public TCMqttConfig genTCMqttConfig() {
        TCMqttConfig config = new TCMqttConfig(mJsonFileData.getHost(), mJsonFileData.getProductKey(), mJsonFileData.getProductId(), mJsonFileData.getRegion());
        TCMqttConfig.TCMqttConnectionMode connectionMode = getConnectionMode();
        config.setConnectionMode(connectionMode);
        if (connectionMode == TCMqttConfig.TCMqttConnectionMode.MODE_DIRECT) {
            config.setMqttUserName(mJsonFileData.getUserName())
                    .setMqttPassword(mJsonFileData.getPassword());
        }
        return config;
    }

    public TCMqttConfig.TCMqttConnectionMode getConnectionMode() {
        int authType = mJsonFileData.getAuthType();
        if (authType == JsonFileData.AUTH_TYPE_DIRECT) {
            return TCMqttConfig.TCMqttConnectionMode.MODE_DIRECT;
        } else if (authType == JsonFileData.AUTH_TYPE_TOKEN) {
            return TCMqttConfig.TCMqttConnectionMode.MODE_TOKEN;
        } else {
            throw new IllegalStateException("illegal auth type = " + authType);
        }
    }
}
