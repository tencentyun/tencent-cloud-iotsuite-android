package com.tencent.qcloud.iot.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;

import com.tencent.qcloud.iot.log.QLog;
import com.tencent.qcloud.iot.sample.fragment.ConnectionFragment;

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        QLog.setLogLevel(QLog.QLOG_LEVEL_DEBUG);
        displayView();
    }

    private void displayView() {
        Fragment fragment = new ConnectionFragment();
        displayFragment(fragment);
    }

    private void displayFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();
        }
    }
}
