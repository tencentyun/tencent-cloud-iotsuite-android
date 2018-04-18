package com.tencent.qcloud.iot.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by rongerwu on 2018/1/15.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class BaseActivity extends AppCompatActivity {
    private Toast mToast;
    private String mToastText = "";
    private long mLastShowToastTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(getApplicationContext(), mToastText, Toast.LENGTH_LONG);
    }

    public void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToastPriv(text);
            }
        });
    }

    private void showToastPriv(final String text) {
        if (mToast == null || text == null) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        long duration = System.currentTimeMillis() - mLastShowToastTime;
        if (duration < 1000 || mToast.getView().getWindowVisibility() == View.VISIBLE) {
            mToastText += "\n" + text;
        } else {
            mToastText = text;
        }
        mLastShowToastTime = currentTime;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast.setText(mToastText);
                mToast.show();
            }
        });
    }
}
