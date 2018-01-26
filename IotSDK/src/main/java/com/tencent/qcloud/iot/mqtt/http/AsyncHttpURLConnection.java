/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.tencent.qcloud.iot.mqtt.http;

import com.tencent.qcloud.iot.utils.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

/**
 * Asynchronous http requests implementation.
 */
public class AsyncHttpURLConnection {
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    private static final int HTTP_TIMEOUT_MS = 8000;
    private final String mMethod;
    private final String mUrl;
    private final String mMessage;
    private final AsyncHttpEvents mEvents;
    private String mContentType;

    public interface AsyncHttpEvents {
        void onHttpError(String errorMessage);

        void onHttpComplete(String response);
    }

    public AsyncHttpURLConnection(String method, String url, String message, AsyncHttpEvents events) {
        this.mMethod = method;
        this.mUrl = url;
        this.mMessage = message;
        this.mEvents = events;
    }

    public void setContentType(String contentType) {
        this.mContentType = contentType;
    }

    public void send() {
        Runnable runHttp = new Runnable() {
            public void run() {
                sendHttpMessage();
            }
        };
        new Thread(runHttp).start();
    }

    private void sendHttpMessage() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(mUrl).openConnection();
            byte[] postData = new byte[0];
            if (mMessage != null) {
                postData = mMessage.getBytes(StringUtil.UTF8);
            }
            connection.setRequestMethod(mMethod);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setConnectTimeout(HTTP_TIMEOUT_MS);
            connection.setReadTimeout(HTTP_TIMEOUT_MS);
            boolean doOutput = false;
            if (mMethod.equals(METHOD_POST)) {
                doOutput = true;
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(postData.length);
            }
            if (mContentType == null) {
                connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            } else {
                connection.setRequestProperty("Content-Type", mContentType);
            }

            // Send POST request.
            if (doOutput && postData.length > 0) {
                OutputStream outStream = connection.getOutputStream();
                outStream.write(postData);
                outStream.close();
            }

            // Get response.
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                mEvents.onHttpError("Non-200 response to " + mMethod + " to URL: " + mUrl + " : " + connection.getHeaderField(null));
                connection.disconnect();
                return;
            }
            InputStream responseStream = connection.getInputStream();
            String response = drainStream(responseStream);
            responseStream.close();
            connection.disconnect();
            mEvents.onHttpComplete(response);
        } catch (SocketTimeoutException e) {
            mEvents.onHttpError("HTTP " + mMethod + " to " + mUrl + " timeout");
        } catch (IOException e) {
            mEvents.onHttpError("HTTP " + mMethod + " to " + mUrl + " error: " + e.getMessage());
        }
    }

    private static String drainStream(InputStream in) {
        Scanner s = new Scanner(in, StringUtil.UTF8.name()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
