package com.tencent.qcloud.iot.mqtt.certificate;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import com.tencent.qcloud.iot.log.QLog;
import com.tencent.qcloud.iot.mqtt.TCMqttClientException;
import com.tencent.qcloud.iot.mqtt.constant.TCConstants;
import com.tencent.qcloud.iot.mqtt.http.AsyncHttpURLConnection;
import com.tencent.qcloud.iot.utils.StringUtil;
import com.tencent.qcloud.iot.utils.TCUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by rongerwu on 2018/1/18.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

/**
 * 帮助获取token的类
 */
public class TokenHelper {
    public static final int TOKEN_RETURN_CODE_SUCCESS = 0;

    private static final String TAG = TokenHelper.class.getSimpleName();

    private String mRegion;
    private String mProductId;
    private String mDeviceName;
    private String mDeviceSecret;
    private String mScheme;

    public TokenHelper(String region, String productId, String deviceName, String deviceSecret, String scheme) {
        if (TextUtils.isEmpty(region) || TextUtils.isEmpty(productId) || TextUtils.isEmpty(deviceName) || TextUtils.isEmpty(deviceSecret) || TextUtils.isEmpty(scheme)) {
            throw new IllegalArgumentException("region / productId / deviceName / deviceSecret / scheme cannot be empty");
        }
        mRegion = region;
        mProductId = productId;
        mDeviceName = deviceName;
        mDeviceSecret = deviceSecret;
        mScheme = scheme;
    }

    public void getToken(final String clientId, final ITokenListener listener) {
        if (clientId == null) {
            throw new IllegalArgumentException("clientId is null");
        }
        //先跟服务器同步时间
        AsyncHttpURLConnection httpConnection = new AsyncHttpURLConnection(AsyncHttpURLConnection.METHOD_GET, TCUtil.getTimeUrl(TCConstants.Scheme.HTTP, mRegion), null,
                new AsyncHttpURLConnection.AsyncHttpEvents() {
                    @Override
                    public void onHttpError(String errorMessage) {
                        QLog.e(TAG, "get server timestamp error: " + errorMessage);
                        if (listener != null) {
                            listener.onFailure(errorMessage);
                        }
                    }

                    @Override
                    public void onHttpComplete(String response) {
                        if (TextUtils.isEmpty(response)) {
                            if (listener != null) {
                                listener.onFailure("empty response");
                            }
                            return;
                        }
                        QLog.d(TAG, "get server timestamp complete, response = " + response);
                        String tokenUrl = generateTokenUrl(clientId, response);
                        requestToken(tokenUrl, listener);
                    }
                });
        httpConnection.send();
    }

    private void requestToken(final String url, final ITokenListener listener) {
        AsyncHttpURLConnection httpConnection = new AsyncHttpURLConnection(AsyncHttpURLConnection.METHOD_GET, url, null, new AsyncHttpURLConnection.AsyncHttpEvents() {
            @Override
            public void onHttpError(String errorMessage) {
                QLog.e(TAG, "get token error: " + errorMessage);
                if (listener != null) {
                    listener.onFailure(errorMessage);
                }
            }

            @Override
            public void onHttpComplete(String response) {
                if (TextUtils.isEmpty(response)) {
                    if (listener != null) {
                        listener.onFailure("empty response");
                    }
                    return;
                }
                //QLog.d(TAG, "get token complete, response = " + response);
                try {
                    JSONObject responseJson = new JSONObject(response);
                    int code = responseJson.getInt("returnCode");
                    if (code != TOKEN_RETURN_CODE_SUCCESS) {
                        if (listener != null) {
                            listener.onFailure(response);
                        }
                        return;
                    }
                    JSONObject dataJson = responseJson.getJSONObject("data");
                    String userName = dataJson.getString("id");
                    String password = dataJson.getString("secret");
                    //String expire = dataJson.getString("expire");
                    if (listener != null) {
                        listener.onSuccess(userName, password);
                    }
                } catch (JSONException e) {
                    QLog.d(TAG, "onHttpComplete", e);
                    if (listener != null) {
                        listener.onFailure(response);
                    }
                }
            }
        });
        httpConnection.send();
    }

    private String generateTokenUrl(final String clientId, String timestamp) {
        int expireTime = 20160 * 60;//单位s，两周
        HashMap<String, String> paramMap = new HashMap();
        paramMap.put("clientId", clientId);
        paramMap.put("deviceName", mDeviceName);
        paramMap.put("expire", String.valueOf(expireTime));
        paramMap.put("nonce", String.valueOf(new Random().nextInt(10000000)));
        paramMap.put("productId", mProductId);
        paramMap.put("timestamp", timestamp);
        Set<String> keySet = paramMap.keySet();

        Uri.Builder builder = Uri.parse(TCUtil.getTokenUrl(mScheme, mRegion)).buildUpon();
        for (String key : keySet) {
            builder.appendQueryParameter(key, paramMap.get(key));
        }
        builder.appendQueryParameter("signature", genSignature(paramMap, mDeviceSecret));
        return builder.toString();
    }

    private String genSignature(Map<String, String> input, String secret) {
        String content = genContent(input);
        byte[] hashHmac = hashHmac(content, secret);
        String signature = Base64.encodeToString(hashHmac, Base64.NO_WRAP);
        //QLog.d(TAG, "content = " + content + ", signature = " + signature);
        return signature;
    }

    private String genContent(Map<String, String> input) {
        StringBuffer buffer = new StringBuffer();
        Set<String> keySet = input.keySet();
        String[] keyArray = (String[]) keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        for (String key : keyArray) {
            buffer.append(key).append("=").append(input.get(key));
            if (!key.equals(keyArray[keyArray.length - 1])) {
                buffer.append("&");
            }
        }
        return buffer.toString();
    }

    private byte[] hashHmac(String content, String secret) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StringUtil.UTF8), "HmacSHA256");
            Mac mac = Mac.getInstance(keySpec.getAlgorithm());
            mac.init(keySpec);
            byte[] byteArray = mac.doFinal(content.toString().getBytes(StringUtil.UTF8));
            return byteArray;

        } catch (NoSuchAlgorithmException e) {
            throw new TCMqttClientException("hash hmac error", e);
        } catch (InvalidKeyException e) {
            throw new TCMqttClientException("hash hmac error", e);
        }
    }

    public interface ITokenListener {
        void onSuccess(String userName, String password);

        void onFailure(String message);
    }

}
