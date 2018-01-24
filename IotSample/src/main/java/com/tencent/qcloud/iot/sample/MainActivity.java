package com.tencent.qcloud.iot.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.tencent.qcloud.iot.common.QLog;
import com.tencent.qcloud.iot.mqtt.certificate.CertificateProvider;
import com.tencent.qcloud.iot.sample.fragment.ConnectionFragment;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Created by rongerwu on 2018/1/15.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QLog.setLogLevel(QLog.QLOG_LEVEL_DEBUG);
        displayView();
    }

    private void displayView() {
        Fragment fragment = new ConnectionFragment();
        displayFragment(fragment, "title");
    }

    private void displayFragment(Fragment fragment, String title) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            getSupportActionBar().setTitle(title);
        }
    }
}
