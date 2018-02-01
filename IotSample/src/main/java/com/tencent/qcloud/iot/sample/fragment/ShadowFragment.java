package com.tencent.qcloud.iot.sample.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.tencent.qcloud.iot.sample.Connection;
import com.tencent.qcloud.iot.sample.Constants;
import com.tencent.qcloud.iot.sample.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rongerwu on 2018/1/15.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

/**
 * 展示一台温度相关的设备如何操作影子
 */
public class ShadowFragment extends Fragment {
    private static final String TAG = ShadowFragment.class.getSimpleName();
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
            final View rootView = inflater.inflate(R.layout.fragment_shadow_temperature, container, false);
            final EditText reportTemperature = (EditText) rootView.findViewById(R.id.et_report_temperature);
            final Button btnGet = (Button) rootView.findViewById(R.id.btn_get);
            final Button btnReport = (Button) rootView.findViewById(R.id.btn_report);
            final Button btnDelete = (Button) rootView.findViewById(R.id.btn_delete);

            btnGet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //获取影子
                    mConnection.getShadow();
                }
            });
            btnReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        //更新设备的温度属性到json，然后汇报到服务器更新影子
                        jsonObject.put("temperature", reportTemperature.getText().toString());
                        mConnection.reportShadow(jsonObject);
                    } catch (JSONException e) {
                        Log.d(TAG, "", e);
                    }
                }
            });
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        //删除影子的temperature属性
                        jsonObject.put("temperature", JSONObject.NULL);
                        mConnection.deleteShadow(jsonObject);
                    } catch (JSONException e) {
                        Log.d(TAG, "", e);
                    }
                }
            });

            mRootView = rootView;
        }
        return mRootView;
    }
}
