package com.tencent.qcloud.iot.mqtt.certificate;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by rongerwu on 2018/1/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class TCTLSSocketFactory {
    private static final String TLS_V_1_2 = "TLSv1.2";

    public static SSLSocketFactory getSocketFactory(KeyStore keyStore) throws TCSSLSocketException {
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance("X509");
            factory.init(keyStore);
            TrustManager[] managers = factory.getTrustManagers();
            X509TrustManager trustManager = (X509TrustManager) managers[0];

            SSLContext context = SSLContext.getInstance(TLS_V_1_2);
            context.init(null, new TrustManager[]{trustManager}, null);
            //context.init(null, managers, null);
            return context.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            throw new TCSSLSocketException("NoSuchAlgorithmException", e);
        } catch (KeyStoreException e) {
            throw new TCSSLSocketException("KeyStoreException", e);
        } catch (KeyManagementException e) {
            throw new TCSSLSocketException("KeyManagementException", e);
        }

    }
}
