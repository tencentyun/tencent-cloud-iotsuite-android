package com.tencent.qcloud.iot.app.tv;

import com.tencent.qcloud.iot.account.TCIotAccountService;
import com.tencent.qcloud.iot.apiclient.TCIotApiClient;
import com.tencent.qcloud.iot.apiclient.exception.ApiClientException;
import com.tencent.qcloud.iot.apiclient.exception.ApiServiceException;
import com.tencent.qcloud.iot.apiclient.listener.IApiCallback;
import com.tencent.qcloud.iot.apiclient.websocket.listener.IConnectListener;
import com.tencent.qcloud.iot.apiclient.websocket.listener.IMessageListener;
import com.tencent.qcloud.iot.app.TCIotAppClient;
import com.tencent.qcloud.iot.app.TCIotAppConfig;
import com.tencent.qcloud.iot.app.listener.ITCIotActionCallback;
import com.tencent.qcloud.iot.app.model.DataAttribute;
import com.tencent.qcloud.iot.app.model.DeviceInfo;
import com.tencent.qcloud.iot.app.model.request.yunapi.ControlDeviceRequest;
import com.tencent.qcloud.iot.app.model.request.yunapi.GetDeviceRequest;
import com.tencent.qcloud.iot.app.model.request.yunapi.GetDevicesRequest;
import com.tencent.qcloud.iot.app.tv.model.other.CommonRequestResult;
import com.tencent.qcloud.iot.app.tv.model.request.TVControlDeviceRequest;
import com.tencent.qcloud.iot.app.tv.model.request.WaitBindTVRequest;
import com.tencent.qcloud.iot.app.tv.model.response.WaitBindTVResponse;
import com.tencent.qcloud.iot.app.tv.model.serverpush.BindTVServerPush;
import com.tencent.qcloud.iot.log.QLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import bolts.Task;

/**
 * Created by rongerwu on 2018/7/9.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class TCIotAppTVClient extends TCIotAppClient {
    public static final String SERVER_PUSH_ACTION_BIND_TV = "TvBind";
    private static final String TAG = TCIotAppTVClient.class.getSimpleName();

    private String mTVId = "";
    private ITCIotActionCallback mWatiBindTVCallback;
    private boolean mWaitingBind;

    private IMessageListener mMessageListener = new IMessageListener() {
        @Override
        public void onServerInitiativeMessage(String action, String message) {
            try {
                parseServerInitiativeMessage(action, message);
            } catch (JSONException e) {
                QLog.e(TAG, "parseServerInitiativeMessage", e);
            }
        }
    };

    private IConnectListener mConnectListener = new IConnectListener() {
        @Override
        public void onConnected() {

        }

        @Override
        public void onConnecting() {

        }

        @Override
        public void onReconnecting() {

        }

        @Override
        public void onClosing() {

        }

        @Override
        public void onClosed() {
            onWebsocketClosed(new ApiClientException("iot_connect_close", "connect closed"));
        }

        @Override
        public void onError(Exception e) {
            onWebsocketClosed(new ApiClientException("iot_connect_error", "connect error", e));
        }
    };

    TCIotAppTVClient(TCIotAppConfig tcIotAppConfig) {
        super(tcIotAppConfig);
        setMessageListener(mMessageListener);
        TCIotApiClient.getInstance().setConnectListener(mConnectListener);
    }

    private void parseServerInitiativeMessage(final String action, final String message) throws JSONException {
        if (action.equals(SERVER_PUSH_ACTION_BIND_TV)) {
            BindTVServerPush serverPush = new BindTVServerPush(message);
            if (serverPush.getTVId().equals(mTVId)) {
                setToken(serverPush.getAccessToken());
                onActionSuccess(mWatiBindTVCallback);
            } else {
                onServiceException(new ApiServiceException("tvId not match, ori = " + mTVId + ", get = " + serverPush.getTVId()), mWatiBindTVCallback);
            }
            mWaitingBind = false;
        } else {
            QLog.i(TAG, "unknown action = " + action + ", message = " + message);
        }
    }

    private void setToken(String token) {
        if (token == null) {
            throw new IllegalArgumentException("token is null");
        }
        TCIotAccountService.getInstance().getAccountSession().getUserInfo().setToken(token);
    }

    private void onWebsocketClosed(final ApiClientException e) {
        //断网导致websocket断开，服务器不会触发onclose，当手机扫码绑定时服务器仍然会返回绑定成功，但实际上没有绑定成功。因此这里返回错误。
        if (mWaitingBind) {
            mWaitingBind = false;
            onClientException(e, mWatiBindTVCallback);
        }
    }

    public String getQRCodeTextByTvId(String tvId) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Type", "tv");
            jsonObject.put("TvId", tvId);
            return jsonObject.toString();
        } catch (JSONException e) {
            QLog.e(TAG, "getQRCodeTextByTvId", e);
        }
        return "error";
    }

    public void waitBindTV(final WaitBindTVRequest request, final ITCIotActionCallback iotActionCallback) {
        final IApiCallback callback = new IApiCallback<WaitBindTVResponse>() {
            @Override
            public void onSuccess(WaitBindTVResponse response) {
                QLog.i(TAG, "send bind tv request success");
                mWaitingBind = true;
            }

            @Override
            public void onFailure(ApiClientException e) {
                onClientException(e, iotActionCallback);
            }

            @Override
            public void onFailure(ApiServiceException e) {
                onServiceException(e, iotActionCallback);
            }
        };
        mWatiBindTVCallback = iotActionCallback;
        mTVId = request.getTVId();
        mTCIotApiClient.request(request, new WaitBindTVResponse(), callback);
    }

    /**
     * 获取绑定设备列表，包含每个设备的数据点
     *
     * @param iotActionCallback 回调
     */
    public void getDevicesAndAttributes(final ITCIotActionCallback iotActionCallback) {
        final GetDevicesRequest request = new GetDevicesRequest(TCIotAccountService.getInstance().getAccountSession().getToken());
        super.getDevices(request, new ITCIotActionCallback() {
            @Override
            public void onSuccess() {
                Task.callInBackground(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        getAttributes(iotActionCallback);
                        return null;
                    }
                });
            }

            @Override
            public void onFailure(String errorCode, Exception e) {
                if (iotActionCallback != null) {
                    iotActionCallback.onFailure(errorCode, e);
                }
            }
        });
    }

    private void getAttributes(final ITCIotActionCallback iotActionCallback) {
        ArrayList<DeviceInfo> deviceInfos = mDeviceManager.getDeviceInfos();
        final CountDownLatch latch = new CountDownLatch(deviceInfos.size());
        final CommonRequestResult requestResult = new CommonRequestResult();
        //遍历获取每个设备的信息，包括数据点
        for (DeviceInfo deviceInfo : deviceInfos) {
            GetDeviceRequest getDeviceRequest = new GetDeviceRequest(TCIotAccountService.getInstance().getAccountSession().getToken()
                    , deviceInfo.getRegion(), deviceInfo.getProductId(), deviceInfo.getDeviceName());
            getDevice(getDeviceRequest, new ITCIotActionCallback() {
                @Override
                public void onSuccess() {
                    latch.countDown();
                }

                @Override
                public void onFailure(String errorCode, Exception e) {
                    if (!requestResult.isError()) {
                        requestResult.setError(true);
                        if (iotActionCallback != null) {
                            iotActionCallback.onFailure(errorCode, e);
                        }
                    }
                    while (latch.getCount() > 0) {
                        latch.countDown();
                    }
                }
            });
        }
        try {
            final int timeout = 6000;
            boolean latchResult = latch.await(timeout, TimeUnit.MILLISECONDS);
            if (latchResult) {
                if (!requestResult.isError()) {
                    onActionSuccess(iotActionCallback);
                }
            } else {
                throw new ApiClientException("wait result timeout. timeout = " + timeout);
            }
        } catch (Exception e) {
            onClientException(new ApiClientException("getAttributes", e), iotActionCallback);
        }
    }

    public void controlDevice(final TVControlDeviceRequest request, final ITCIotActionCallback iotActionCallback) {
        DeviceInfo deviceInfo = mDeviceManager.getDeviceInfoById(request.getDeviceId());
        if (deviceInfo == null) {
            onClientException(new ApiClientException("device id not found"), iotActionCallback);
            return;
        }
        DataAttribute dataAttribute = deviceInfo.getAttributeByName(request.getAttributeName());
        if (dataAttribute == null) {
            onClientException(new ApiClientException("attribute name not found"), iotActionCallback);
            return;
        }
        boolean setValueResult = dataAttribute.setValue(request.getAttributeValue());
        if (!setValueResult) {
            onClientException(new ApiClientException("attribute value illegal"), iotActionCallback);
        }
        try {
            String controlData = deviceInfo.getControlData(dataAttribute);
            ControlDeviceRequest controlDeviceRequest = new ControlDeviceRequest(TCIotAccountService.getInstance().getAccountSession().getToken()
                    , deviceInfo.getRegion(), deviceInfo.getProductId(), deviceInfo.getDeviceName(), controlData);
            controlDevice(controlDeviceRequest, iotActionCallback);
        } catch (JSONException e) {
            onClientException(new ApiClientException("controlDevice", e), iotActionCallback);
        }
    }
}
