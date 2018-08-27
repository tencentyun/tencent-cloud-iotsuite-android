package com.tencent.qcloud.iot.app.tv;

import android.content.Context;

import com.tencent.qcloud.iot.account.TCIotAccountConfig;
import com.tencent.qcloud.iot.account.TCIotAccountService;
import com.tencent.qcloud.iot.account.listener.IAuthEventListener;
import com.tencent.qcloud.iot.apiclient.TCIotApiClient;
import com.tencent.qcloud.iot.apiclient.TCIotApiConfig;
import com.tencent.qcloud.iot.app.TCIotAppConfig;
import com.tencent.qcloud.iot.log.QLog;

/**
 * Created by rongerwu on 2018/7/9.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class TCIotAppTVService {
    private static final String TAG = TCIotAppTVService.class.getSimpleName();
    private static TCIotAppTVService sInstance;
    private TCIotAppTVClient mTCIotAppTVClient;

    private TCIotAppTVService(Context context, TCIotAppConfig tcIotAppConfig) {
        if (context == null || tcIotAppConfig == null) {
            throw new IllegalArgumentException("TCIotAppService init");
        }
        mTCIotAppTVClient = new TCIotAppTVClient(tcIotAppConfig);
    }

    public static TCIotAppTVService getInstance() {
        if (sInstance == null) {
            throw new RuntimeException("must call init first");
        }
        return sInstance;
    }

    /**
     * 必须调用init初始化。
     *
     * @param context
     * @param tcIotAppConfig
     * @param authEventListener
     */
    public static void init(Context context, TCIotAppConfig tcIotAppConfig, IAuthEventListener authEventListener) {
        if (sInstance == null) {
            synchronized (TCIotAppTVService.class) {
                if (sInstance == null) {
                    //初始化 ApiClient SDK, 这里填入自己的secretId
                    TCIotApiClient.init(new TCIotApiConfig(tcIotAppConfig.getSecretId()));
                    //初始化 Account SDK，secretId同其他地方保持一致
                    TCIotAccountService.init(new TCIotAccountConfig(tcIotAppConfig.getSecretId()));
                    TCIotAccountService.getInstance().getAccountClient().setAuthEventListener(authEventListener);

                    sInstance = new TCIotAppTVService(context, tcIotAppConfig);
                    sInstance.getAppTVClient().setAuthEventListener(authEventListener);
                }
            }
        } else {
            QLog.w(TAG, "you have init already");
            return;
        }
    }

    public static void destroy() {
        TCIotApiClient.destroy();
        TCIotAccountService.destroy();

        if (sInstance != null) {
            sInstance.mTCIotAppTVClient.destroy();
        }
    }

    public TCIotAppTVClient getAppTVClient() {
        return mTCIotAppTVClient;
    }

}
