package com.tencent.qcloud.iot.mqtt.certificate;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Created by rongerwu on 2018/1/12.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class CertificateProviderTest {

    @Test
    public void testGetKeyStore() throws IOException {

        KeyStore keyStore = getKeyStore();
        Assert.assertNotNull(keyStore);
    }

    public KeyStore getKeyStore() throws IOException {
        Context appContext = InstrumentationRegistry.getTargetContext();
        InputStream inputStream = appContext.getAssets().open("test.crt");
        CertificateProvider provider = new CertificateProvider(inputStream);
        inputStream.close();

        KeyStore keyStore = provider.getKeyStore();
        return keyStore;
    }
}
