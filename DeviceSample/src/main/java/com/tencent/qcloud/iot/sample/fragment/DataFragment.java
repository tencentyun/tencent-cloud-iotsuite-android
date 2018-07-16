package com.tencent.qcloud.iot.sample.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tencent.qcloud.iot.device.dataprotocol.datatemplate.DataTemplate;
import com.tencent.qcloud.iot.device.exception.DeviceException;
import com.tencent.qcloud.iot.sample.BaseActivity;
import com.tencent.qcloud.iot.sample.Connection;
import com.tencent.qcloud.iot.sample.R;
import com.tencent.qcloud.iot.sample.constant.Constants;
import com.tencent.qcloud.iot.sample.constant.TCDataConstant;

/**
 * Created by rongerwu on 2018/1/15.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

/**
 * 展示一台温度相关的设备如何操作影子
 */
public class DataFragment extends Fragment {
    private static final String TAG = DataFragment.class.getSimpleName();
    private View mRootView;
    private Connection mConnection;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mConnection = (Connection) bundle.getParcelable(Constants.CONNECTION_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            final View rootView = inflater.inflate(R.layout.fragment_data, container, false);
            final Button btnTestUserChangeData = rootView.findViewById(R.id.btn_test);
            btnTestUserChangeData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    testSetDataTemplate();
                }
            });
            mRootView = rootView;
        }
        return mRootView;
    }

    public void testSetDataTemplate() {
        DataTemplate dataTemplate = mConnection.getDataTemplate();
        if (dataTemplate == null) {
            ((BaseActivity) getActivity()).showToast("please connect first");
            return;
        }
        //当本地控制设备状态变化时，需要调用接口设置数据点的值。
        //第三个参数表示是否立即上报服务端。如果需要设置多个数据点的值，建议只在修改最后一个数据点值时设为true，这样就只会上报一次，避免频繁上报。
        try {
            //数值型
            dataTemplate.setDataPointByUser(TCDataConstant.BRIGHTNESS, 15.5, false);
            //字符串型
            dataTemplate.setDataPointByUser(TCDataConstant.ALIAS_NAME, "first", false);
            //枚举型
            dataTemplate.setDataPointByUser(TCDataConstant.COLOR, TCDataConstant.COLOR_BLUE, true);
        } catch (DeviceException e) {
            Log.e(TAG, "setDataPointByUser error", e);
        }
    }
}
