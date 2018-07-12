package com.tencent.qcloud.iot.device;

import android.content.Context;

import com.tencent.qcloud.iot.device.data.DeviceDataHandler;
import com.tencent.qcloud.iot.device.data.IDataEventListener;
import com.tencent.qcloud.iot.device.data.OtaHandler;
import com.tencent.qcloud.iot.device.data.ShadowHandler;
import com.tencent.qcloud.iot.device.datatemplate.DataTemplate;
import com.tencent.qcloud.iot.device.datatemplate.JsonFileData;
import com.tencent.qcloud.iot.device.datatemplate.ProductInfoHelper;
import com.tencent.qcloud.iot.device.exception.DeviceRuntimeException;
import com.tencent.qcloud.iot.device.utils.FileUtil;
import com.tencent.qcloud.iot.log.QLog;
import com.tencent.qcloud.iot.mqtt.TCIotMqttClient;
import com.tencent.qcloud.iot.mqtt.TCMqttConfig;
import com.tencent.qcloud.iot.mqtt.TopicHelper;
import com.tencent.qcloud.iot.mqtt.callback.IMqttActionCallback;
import com.tencent.qcloud.iot.mqtt.callback.IMqttConnectStateCallback;
import com.tencent.qcloud.iot.mqtt.callback.IMqttMessageListener;
import com.tencent.qcloud.iot.mqtt.constant.MqttConnectState;
import com.tencent.qcloud.iot.mqtt.constant.TCIotMqttQos;
import com.tencent.qcloud.iot.mqtt.ota.OtaManager;
import com.tencent.qcloud.iot.mqtt.request.MqttPublishRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttSubscribeRequest;
import com.tencent.qcloud.iot.mqtt.request.MqttUnSubscribeRequest;
import com.tencent.qcloud.iot.mqtt.shadow.ShadowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by rongerwu on 2018/4/18.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class TCIotDeviceService {
    private static final String TAG = TCIotDeviceService.class.getSimpleName();
    private static final String JSON_FILE_NAME = "tc_iot/tc_iot_product.json";

    private TCMqttConfig mTCMqttConfig;
    private TCIotMqttClient mTCIotMqttClient;
    private ShadowManager mShadowManager;
    private OtaManager mOtaManager;
    private TopicHelper mTopicHelper;
    private IMqttMessageListener mMqttMessageListener;
    private ShadowHandler mShadowHandler;
    private OtaHandler mOtaHandler;
    private DeviceDataHandler mDeviceDataHandler;
    private static JsonFileData sJsonFileData;
    private DataTemplate mDataTemplate;

    public static void init(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null");
        }
        sJsonFileData = initJsonData(context.getApplicationContext());
    }

    private static JsonFileData initJsonData(Context appContext) {
        try {
            String jsonData = FileUtil.getAssetFileString(appContext, JSON_FILE_NAME);
            return new JsonFileData(jsonData);
        } catch (IOException e) {
            throw new DeviceRuntimeException(String.format("ensure %s exist", JSON_FILE_NAME));
        } catch (JSONException e) {
            throw new DeviceRuntimeException(String.format("check %s legal", JSON_FILE_NAME));
        }
    }

    public static TCMqttConfig genTCMqttConfig() {
        return new ProductInfoHelper(sJsonFileData).genTCMqttConfig();
    }

    public TCIotDeviceService(TCMqttConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null!");
        }
        if (sJsonFileData == null) {
            throw new DeviceRuntimeException("must call init first");
        }
        mDataTemplate = sJsonFileData.getDataTemplate();

        mTCMqttConfig = config;
        mTCIotMqttClient = new TCIotMqttClient(config);
        mTopicHelper = new TopicHelper(config.getProductId(), config.getDeviceName());
        mShadowManager = new ShadowManager(mTCIotMqttClient, config, mTopicHelper);
        mOtaManager = new OtaManager(mTCIotMqttClient, mTopicHelper);
        mDeviceDataHandler = DeviceDataHandler.getInstance(mShadowManager);
        mShadowHandler = new ShadowHandler(mDeviceDataHandler);
        mOtaHandler = new OtaHandler();

        //监听来自服务端的控制消息
        mDeviceDataHandler.setDataEventListener(new IDataEventListener() {
            @Override
            public void onControl(String key, Object value, boolean diff, boolean forInit) {
                try {
                    mDataTemplate.onControl(key, value, diff, forInit);
                } catch (ClassCastException e) {
                    QLog.w(TAG, "onControl, key = " + key + ", value = " + value + ", diff = " + diff + ", forInit = " + forInit, e);
                }
            }
        });
        //监听设备数据修改，用于传入SDK处理，触发上传到服务器
        mDataTemplate.setLocalDataListener(new DataTemplate.ILocalDataListener() {
            @Override
            public void onLocalDataChange(JSONObject localData) {
                mDeviceDataHandler.updateLocalDeviceData(localData);
            }

            @Override
            public void onUserChangeData(JSONObject userDesired, boolean commit) {
                try {
                    mDeviceDataHandler.onUserChangeData(userDesired, commit);
                } catch (JSONException e) {
                    QLog.e(TAG, "onUserChangeData", e);
                }
            }
        });
        //触发初始化SDK内部的local data
        mDataTemplate.onLocalDataChange();
    }

    public void setMqttMessageListener(IMqttMessageListener mqttMessageListener) {
        mMqttMessageListener = mqttMessageListener;
    }

    /**
     * 建立mqtt连接
     *
     * @param connectStateCallback 连接状态回调
     */
    public void connect(final IMqttConnectStateCallback connectStateCallback) {
        mTCIotMqttClient.connect(new IMqttConnectStateCallback() {
            @Override
            public void onStateChanged(MqttConnectState state) {
                if (state == MqttConnectState.CONNECTED) {
                    //connect后getShadow以得到desired，用来初始化
                    try {
                        mShadowManager.getShadow();
                        reportDeviceInfo();
                    } catch (JSONException e) {
                        QLog.e(TAG, "getShadow after connect", e);
                    }
                }
                if (connectStateCallback != null) {
                    connectStateCallback.onStateChanged(state);
                }
            }
        });

        mTCIotMqttClient.setMqttMessageListener(new IMqttMessageListener() {
            @Override
            public void onMessageArrived(String topic, String message) {
                //影子消息在内部处理
                if (topic.equals(mTopicHelper.getShadowGetTopic())) {
                    mShadowHandler.parseShadowMessage(message);
                } else if (topic.equals(mTopicHelper.getOtaGetTopic())) {
                    mOtaHandler.parseMessage(message);
                } else {
                    if (mMqttMessageListener != null) {
                        mMqttMessageListener.onMessageArrived(topic, message);
                    }
                }
            }
        });
        subscribeMessage(mTopicHelper.getShadowGetTopic());
        subscribeMessage(mTopicHelper.getOtaGetTopic());
    }

    /**
     * 断开mqtt连接
     */
    public void disconnect() {
        mTCIotMqttClient.disconnect();
    }

    /**
     * 向topic发布消息，必须在connect调用之后，否则无效。
     *
     * @param request 请求
     */
    public void publish(final MqttPublishRequest request) {
        mTCIotMqttClient.publish(request);
    }

    /**
     * 订阅topic，必须在connect调用之后，否则无效。
     *
     * @param request 请求
     */
    public void subscribe(final MqttSubscribeRequest request) {
        mTCIotMqttClient.subscribe(request);
    }

    /**
     * 取消订阅topic，必须在connect调用之后，否则无效。
     *
     * @param request 请求
     */
    public void unSubscribe(final MqttUnSubscribeRequest request) {
        mTCIotMqttClient.unSubscribe(request);
    }

    /**
     * 订阅消息
     */
    private void subscribeMessage(final String topic) {
        MqttSubscribeRequest request = new MqttSubscribeRequest()
                .setTopic(topic)
                .setQos(TCIotMqttQos.QOS1)
                .setCallback(new IMqttActionCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        QLog.e(TAG, "subscribe failed, topic = " + topic, exception);
                    }
                });
        subscribe(request);
    }

    /**
     * 上报SDK版本、deviceName等
     */
    private void reportDeviceInfo() throws JSONException {
        JSONObject deviceInfoObject = new JSONObject();
        deviceInfoObject.put("product", mTCMqttConfig.getProductId())
                .put("sdk-ver", BuildConfig.VERSION_NAME)
                .put("firm-ver", "android");
        mOtaManager.reportDeviceInfo(deviceInfoObject);
    }

    /**
     * 设置监听服务端对数据点的控制消息
     *
     * @param dataControlListener
     */
    public void setDataControlListener(DataTemplate.IDataControlListener dataControlListener) {
        if (dataControlListener == null) {
            throw new IllegalArgumentException("dataControlListener is null");
        }
        //监听本地处理后的来自服务端的控制消息
        mDataTemplate.setDataControlListener(dataControlListener);
    }

    public JsonFileData getJsonFileData() {
        return sJsonFileData;
    }

    public DataTemplate getDataTemplate() {
        return mDataTemplate;
    }
}
