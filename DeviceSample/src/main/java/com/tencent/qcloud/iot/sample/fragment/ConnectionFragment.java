package com.tencent.qcloud.iot.sample.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.tencent.qcloud.iot.sample.Connection;
import com.tencent.qcloud.iot.sample.Constants;
import com.tencent.qcloud.iot.sample.MainActivity;
import com.tencent.qcloud.iot.sample.R;
import com.tencent.qcloud.iot.sample.qcloud.JsonFileData;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MainActivity activity = (MainActivity) getActivity();
        mConnection = new Connection();
        mConnection.setMessageNotifyListener(new Connection.IMessageNotifyListener() {
            @Override
            public void onMessage(String msg) {
                activity.showToast(msg);
            }
        });
        //监听来自服务端的控制消息,每个接口，处理完成后需要返回true，才能够正确修改上报设备数据。
        mConnection.setDataControlListener(new JsonFileData.IDataControlListener() {
            @Override
            public boolean onControlDeviceSwitch(boolean deviceSwitch) {
                Log.d(TAG, "onControlDeviceSwitch: " + deviceSwitch);
                activity.showToast("onControlDeviceSwitch: " + deviceSwitch);
                return true;
            }

            @Override
            public boolean onControlColor(JsonFileData.Color color) {
                Log.d(TAG, "onControlColor: " + color);
                activity.showToast("onControlColor: " + color);
                return true;
            }

            @Override
            public boolean onControlBrightness(int brightness) {
                Log.d(TAG, "onControlBrightness: " + brightness);
                activity.showToast("onControlBrightness: " + brightness);
                return true;
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
                        mConnection.connect(mDeviceName, mDeviceSecret);
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
                            mSwitchConnection.setChecked(false);
                        }
                    });
                }
            });
            mRootView = rootView;
        }
        return mRootView;
    }

}
