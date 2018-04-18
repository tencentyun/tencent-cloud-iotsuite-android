package com.tencent.qcloud.iot.mqtt.certificate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by rongerwu on 2018/1/11.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class CertificateProvider implements X509TrustManager {

    private KeyStore mKeyStore;
    private X509TrustManager mX509TrustManager;

    public CertificateProvider(InputStream inputStream) {
        try {
            mKeyStore = getKeyStore(inputStream);
            TrustManagerFactory factory = TrustManagerFactory.getInstance("X509");
            factory.init(mKeyStore);
            TrustManager[] managers = factory.getTrustManagers();
            mX509TrustManager = (X509TrustManager) managers[0];
        } catch (KeyStoreException e) {
            throw new QCloudCertificateException("KeyStoreException", e);
        } catch (CertificateException e) {
            throw new QCloudCertificateException("CertificateException", e);
        } catch (NoSuchAlgorithmException e) {
            throw new QCloudCertificateException("NoSuchAlgorithmException", e);
        } catch (IOException e) {
            throw new QCloudCertificateException("IOException", e);
        }
    }

    private KeyStore getKeyStore(InputStream inputStream) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) factory.generateCertificate(inputStream);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);
        keyStore.setCertificateEntry(certificate.getSubjectX500Principal().getName(), certificate);
        return keyStore;
    }

    public KeyStore getKeyStore() {
        return mKeyStore;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        mX509TrustManager.checkServerTrusted(chain, authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
