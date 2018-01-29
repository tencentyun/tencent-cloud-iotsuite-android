package com.tencent.qcloud.iot.mqtt.certificate;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import com.tencent.qcloud.iot.common.QLog;
import com.tencent.qcloud.iot.mqtt.QCloudMqttClientException;
import com.tencent.qcloud.iot.mqtt.constant.QCloudConstants;
import com.tencent.qcloud.iot.mqtt.http.AsyncHttpURLConnection;
import com.tencent.qcloud.iot.utils.StringUtil;

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
    private static final String TAG = TokenHelper.class.getSimpleName();

    private String mProductId;
    private String mDeviceName;
    private String mDeviceSecret;

    public TokenHelper(String productId, String deviceName, String deviceSecret) {
        if (productId == null || deviceName == null || deviceSecret == null) {
            throw new IllegalArgumentException("productId / deviceName / deviceSecret cannot be null");
        }
        mProductId = productId;
        mDeviceName = deviceName;
        mDeviceSecret = deviceSecret;
    }

    public void requestToken(final String clientId, final ITokenListener listener) {
        if (clientId == null) {
            throw new IllegalArgumentException("clientId is null");
        }
        String url = generateUrl(clientId);
        AsyncHttpURLConnection httpConnection = new AsyncHttpURLConnection(AsyncHttpURLConnection.METHOD_GET, url, null, new AsyncHttpURLConnection.AsyncHttpEvents() {
            @Override
            public void onHttpError(String errorMessage) {
                QLog.e(TAG, "get token error: " + errorMessage);
                if (listener != null) {
                    listener.onFailed(errorMessage);
                }
            }

            @Override
            public void onHttpComplete(String response) {
                if (TextUtils.isEmpty(response)) {
                    if (listener != null) {
                        listener.onFailed("empty response");
                    }
                    return;
                }
                QLog.d(TAG, "get token complete, response = " + response);
                try {
                    JSONObject responseJson = new JSONObject(response);
                    int code = responseJson.getInt("returnCode");
                    if (code != QCloudConstants.TOKEN_RETURN_CODE_SUCCESS) {
                        if (listener != null) {
                            listener.onFailed(response);
                        }
                        return;
                    }
                    JSONObject dataJson = responseJson.getJSONObject("data");
                    String userName = dataJson.getString("id");
                    String password = dataJson.getString("secret");
                    //String expire = dataJson.getString("expire");
                    if (listener != null) {
                        listener.onSuccessed(userName, password);
                    }
                } catch (JSONException e) {
                    QLog.d(TAG, "onHttpComplete, " + e);
                    if (listener != null) {
                        listener.onFailed(response);
                    }
                }
            }
        });
        httpConnection.send();
    }

    private String generateUrl(final String clientId) {
        HashMap<String, String> paramMap = new HashMap();
        paramMap.put("clientId", clientId);
        paramMap.put("deviceName", mDeviceName);
        paramMap.put("expire", "60");
        paramMap.put("nonce", String.valueOf(new Random().nextInt(10000000)));
        paramMap.put("productId", mProductId);
        paramMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        Set<String> keySet = paramMap.keySet();

        Uri.Builder builder = Uri.parse(QCloudConstants.TOKEN_URL).buildUpon();
        for (String key : keySet) {
            builder.appendQueryParameter(key, paramMap.get(key));
        }
        builder.appendQueryParameter("signature", genSignature(paramMap, mDeviceSecret));
        return builder.toString();
    }

    private String genSignature(Map<String, String> input, String secret) {
        String content = genContent(input);
        byte[] hashHmac = hashHmac(content, secret);
        String signature = Base64.encodeToString(hashHmac, Base64.DEFAULT);
        if (signature.endsWith("\n")) {
            signature = signature.replace("\n", "");
        }
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
            throw new QCloudMqttClientException("hash hmac error: " + e);
        } catch (InvalidKeyException e) {
            throw new QCloudMqttClientException("hash hmac error: " + e);
        }
    }

    public interface ITokenListener {
        void onSuccessed(String userName, String password);

        void onFailed(String message);
    }

}
