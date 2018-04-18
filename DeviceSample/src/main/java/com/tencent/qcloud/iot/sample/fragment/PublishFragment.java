package com.tencent.qcloud.iot.sample.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.tencent.qcloud.iot.sample.Connection;
import com.tencent.qcloud.iot.sample.Constants;
import com.tencent.qcloud.iot.sample.MainActivity;
import com.tencent.qcloud.iot.sample.R;

/**
 * Created by rongerwu on 2018/1/15.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class PublishFragment extends Fragment {

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
            final View rootView = inflater.inflate(R.layout.fragment_publish, container, false);
            final EditText topicText = (EditText) rootView.findViewById(R.id.et_topic);
            final EditText messageText = (EditText) rootView.findViewById(R.id.et_message);
            final Button btnPublish = (Button) rootView.findViewById(R.id.btn_publish);

            btnPublish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity activity = (MainActivity) getActivity();
                    String topic = topicText.getText().toString();
                    String message = messageText.getText().toString();
                    if (TextUtils.isEmpty(topic) || TextUtils.isEmpty(message)) {
                        activity.showToast("topic and message can not be empty");
                        return;
                    }
                    mConnection.publish(topic, message);
                }
            });
            mRootView = rootView;
        }
        return mRootView;
    }
}
