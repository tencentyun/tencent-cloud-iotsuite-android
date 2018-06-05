package com.tencent.qcloud.iot.sample.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.tencent.qcloud.iot.device.datatemplate.DataPointControlPacket;
import com.tencent.qcloud.iot.device.datatemplate.DataTemplate;
import com.tencent.qcloud.iot.sample.Connection;
import com.tencent.qcloud.iot.sample.constant.Constants;
import com.tencent.qcloud.iot.sample.MainActivity;
import com.tencent.qcloud.iot.sample.R;
import com.tencent.qcloud.iot.sample.constant.TCDataConstant;

/**
 * Created by rongerwu on 2018/1/15.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class ConnectionFragment extends Fragment {

    private static final String TAG = "ConnectionFragment";
    private View mRootView;
    private Switch mSwitchConnection;
    private TextView mTVState;
    private FragmentTabHost mTabHost;
    private Connection mConnection;

    private String mDeviceName = "token_test_1";
    private String mDeviceSecret = "1e3acdf1242b17b11f353505d75cbcfa";

    /**
     * 监听来自服务端的控制消息。
     */
    private DataTemplate.IDataControlListener mDataControlListener = new DataTemplate.IDataControlListener() {
        @Override
        public boolean onControlDataPoint(DataPointControlPacket dataPointControlPacket) {
            switch (dataPointControlPacket.getName()) {
                case TCDataConstant.DEVICE_SWITCH:
                    boolean deviceSwitch = (boolean) dataPointControlPacket.getValue();
                    if (dataPointControlPacket.isForInit()) {
                        //isForInit()标识是否是SDK启动后的第一次设备初始化操作，如果不希望初始化操作，可以判断forInit为true时不处理。
                    }
                    if (dataPointControlPacket.isDiff()) {
                        //isDiff()标识value是否和该数据点的当前值不相等，可以做一些逻辑（比如如果相等就不处理）。
                    }
                    break;
                case TCDataConstant.COLOR:
                    String color = (String) dataPointControlPacket.getValue();
                    break;
                case TCDataConstant.BRIGHTNESS:
                    //不能直接强转成double: ((double) dataPointControlPacket.getValue())
                    double brightness = ((Number) dataPointControlPacket.getValue()).doubleValue();
                    break;
                case TCDataConstant.ALIAS_NAME:
                    String aliasName = (String) dataPointControlPacket.getValue();
                    break;
            }
            getMainActivity().showToast("onControlDataPoint: " + dataPointControlPacket.toString());
            //处理完成后需要返回true，才能够修改上报设备数据。返回false不会修改上报设备数据。
            return true;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnection = new Connection();
        mConnection.setMessageNotifyListener(new Connection.IMessageNotifyListener() {
            @Override
            public void onMessage(String msg) {
                getMainActivity().showToast(msg);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            View rootView = inflater.inflate(R.layout.fragment_connection, container, false);
            mTabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
            mTabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.CONNECTION_KEY, mConnection);
            mTabHost.addTab(mTabHost.newTabSpec("Publish").setIndicator("Publish"), PublishFragment.class, bundle);
            mTabHost.addTab(mTabHost.newTabSpec("Subscribe").setIndicator("Subscribe"), SubscribeFragment.class, bundle);
            mTabHost.addTab(mTabHost.newTabSpec("Data").setIndicator("Data"), DataFragment.class, bundle);

            mTVState = (TextView) rootView.findViewById(R.id.tv_state);
            mSwitchConnection = (Switch) rootView.findViewById(R.id.switch_connection);

            mSwitchConnection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mConnection.connect(mDeviceName, mDeviceSecret, mDataControlListener);
                    } else {
                        mConnection.disconnect();
                        mTVState.setText("Closed");
                    }
                }
            });
            mConnection.setConnectionStateListener(new Connection.IConnectionStateListener() {
                @Override
                public void onConnecting() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTVState.setText("Connecting");
                        }
                    });
                }

                @Override
                public void onSuccess() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTVState.setText("Connected");
                        }
                    });
                }

                @Override
                public void onFailure() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTVState.setText("Closed");
                        }
                    });
                }
            });
            mRootView = rootView;
        }
        return mRootView;
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

}
