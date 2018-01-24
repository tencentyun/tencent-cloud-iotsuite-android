package com.tencent.qcloud.iot.sample.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.tencent.qcloud.iot.mqtt.QCloudMqttConfig;
import com.tencent.qcloud.iot.mqtt.QCloudMqttConfig.QCloudMqttConnectionMode;
import com.tencent.qcloud.iot.sample.Connection;
import com.tencent.qcloud.iot.sample.Constants;
import com.tencent.qcloud.iot.sample.MainActivity;
import com.tencent.qcloud.iot.sample.R;

/**
 * Created by rongerwu on 2018/1/15.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class ConnectionFragment extends Fragment {
    private static final String TAG = "ConnectionFragment";
    private Switch mSwitchConnection;
    private TextView mTVState;
    private Button mBtnEditInfo;
    private FragmentTabHost mTabHost;
    private Connection mConnection;

    private QCloudMqttConfig.QCloudMqttConnectionMode mConnectionMode = QCloudMqttConnectionMode.MODE_DIRECT;
    private AlertDialog mEditInfoDialog;
    private RadioGroup mRGConnectionMode;
    private EditText mETMqttHost;
    private EditText mETProductKey;
    private EditText mETProductId;
    private EditText mETDeviceName;
    private EditText mETDeviceSecret;
    private EditText mETUserName;
    private EditText mETPassword;

    //直连模式参数
    private String mDirectMqttHost = "mqtt-m2i58z3s.ap-guangzhou.mqtt.tencentcloudmq.com";
    private String mDirectProductKey = "mqtt-m2i58z3s";
    private String mDirectProductId = "iot-6xzr8ap8";
    private String mDirectDeviceName = "test_android_1";
    private String mDirectDeviceSecret = "48bf05179b6f1be3b38c89f27c804f11";
    private String mDirectUserName = "AKIDNgssgTw1pW2NahKR4oRt9D6ofNuGgSKG";
    private String mDirectPassword = "085Nmo6yhgR/TMjSPfFWP+TEVrggjVNFtAyvZUCxp0U=";
    //token认证模式参数
    private String mTokenMqttHost = "mqtt-5oo05hhn8.ap-guangzhou.mqtt.tencentcloudmq.com";
    private String mTokenProductKey = "mqtt-5oo05hhn8";
    private String mTokenProductId = "iot-kaqvlhxc";
    private String mTokenDeviceName = "test_android_2";
    private String mTokenDeviceSecret = "4a3a3b49c5103f8d4cfea154169f6b25";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnection = new Connection();
        mConnection.setMessageNotifyListener(new Connection.IMessageNotifyListener() {
            @Override
            public void onMessage(String msg) {
                MainActivity activity = (MainActivity) getActivity();
                activity.showToast(msg);
            }
        });

        initEditInfoView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connection, container, false);
        mTabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.CONNECTION_KEY, mConnection);
        mTabHost.addTab(mTabHost.newTabSpec("Publish").setIndicator("Publish"), PublishFragment.class, bundle);
        mTabHost.addTab(mTabHost.newTabSpec("Subscribe").setIndicator("Subscribe"), SubscribeFragment.class, bundle);

        mTVState = (TextView) rootView.findViewById(R.id.tv_state);
        mBtnEditInfo = (Button) rootView.findViewById(R.id.btn_edit_info);
        mSwitchConnection = (Switch) rootView.findViewById(R.id.switch_connection);

        mBtnEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEditInfoView();
                mEditInfoDialog.show();
            }
        });
        mSwitchConnection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mConnectionMode == QCloudMqttConnectionMode.MODE_DIRECT) {
                        mConnection.connectDirectMode(mDirectMqttHost, mDirectProductKey, mDirectProductId, mDirectDeviceName, mDirectDeviceSecret, mDirectUserName, mDirectPassword);
                    } else {
                        mConnection.connectTokenMode(mTokenMqttHost, mTokenProductKey, mTokenProductId, mTokenDeviceName, mTokenDeviceSecret);
                    }
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
        return rootView;
    }

    private void initEditInfoView() {
        LayoutInflater layoutInflater = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View editInfoView = layoutInflater.inflate(R.layout.dialog_connection_info, null);
        mRGConnectionMode = (RadioGroup) editInfoView.findViewById(R.id.rg_connection_mode);
        mETMqttHost = (EditText) editInfoView.findViewById(R.id.et_mqtt_host);
        mETProductKey = (EditText) editInfoView.findViewById(R.id.et_product_key);
        mETProductId = (EditText) editInfoView.findViewById(R.id.et_product_id);
        mETDeviceName = (EditText) editInfoView.findViewById(R.id.et_device_name);
        mETDeviceSecret = (EditText) editInfoView.findViewById(R.id.et_device_secret);
        mETUserName = (EditText) editInfoView.findViewById(R.id.et_username);
        mETPassword = (EditText) editInfoView.findViewById(R.id.et_password);

        mRGConnectionMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_mode_direct:
                        mConnectionMode = QCloudMqttConnectionMode.MODE_DIRECT;
                        break;
                    case R.id.rb_mode_token:
                        mConnectionMode = QCloudMqttConnectionMode.MODE_TOKEN;
                        break;
                }
                updateEditInfoView();
            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(editInfoView);
        alertDialogBuilder.setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mConnectionMode == QCloudMqttConnectionMode.MODE_DIRECT) {
                    mDirectMqttHost = mETMqttHost.getText().toString();
                    mDirectProductKey = mETProductKey.getText().toString();
                    mDirectProductId = mETProductId.getText().toString();
                    mDirectDeviceName = mETDeviceName.getText().toString();
                    mDirectDeviceSecret = mETDeviceSecret.getText().toString();
                    mDirectUserName = mETUserName.getText().toString();
                    mDirectPassword = mETPassword.getText().toString();
                } else if (mConnectionMode == QCloudMqttConnectionMode.MODE_TOKEN) {
                    mTokenMqttHost = mETMqttHost.getText().toString();
                    mTokenProductKey = mETProductKey.getText().toString();
                    mTokenProductId = mETProductId.getText().toString();
                    mTokenDeviceName = mETDeviceName.getText().toString();
                    mTokenDeviceSecret = mETDeviceSecret.getText().toString();
                }
            }

        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        mEditInfoDialog = alertDialogBuilder.create();
        mEditInfoDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void updateEditInfoView() {
        if (mConnectionMode == QCloudMqttConnectionMode.MODE_DIRECT) {
            mETUserName.setFocusable(true);
            mETPassword.setFocusable(true);

            mRGConnectionMode.check(R.id.rb_mode_direct);
            mETMqttHost.setText(mDirectMqttHost);
            mETProductKey.setText(mDirectProductKey);
            mETProductId.setText(mDirectProductId);
            mETDeviceName.setText(mDirectDeviceName);
            mETDeviceSecret.setText(mDirectDeviceSecret);
            mETUserName.setText(mDirectUserName);
            mETPassword.setText(mDirectPassword);
        } else if (mConnectionMode == QCloudMqttConnectionMode.MODE_TOKEN) {
            mETUserName.setFocusable(false);
            mETPassword.setFocusable(false);
            mETUserName.setText("null");
            mETPassword.setText("null");

            mRGConnectionMode.check(R.id.rb_mode_token);
            mETMqttHost.setText(mTokenMqttHost);
            mETProductKey.setText(mTokenProductKey);
            mETProductId.setText(mTokenProductId);
            mETDeviceName.setText(mTokenDeviceName);
            mETDeviceSecret.setText(mTokenDeviceSecret);
        }
    }

}
