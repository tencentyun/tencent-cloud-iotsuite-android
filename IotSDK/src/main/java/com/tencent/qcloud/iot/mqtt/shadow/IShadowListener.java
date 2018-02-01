package com.tencent.qcloud.iot.mqtt.shadow;

import org.json.JSONObject;

/**
 * Created by rongerwu on 2018/1/31.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public interface IShadowListener {

    void onReplySuccess();

    void onReplyFail(int errorCode, String status);

    void onGetShadow(JSONObject desired, JSONObject reported);

    void onControl(JSONObject desired);
}
