package com.tencent.qcloud.iot.sample.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.tencent.qcloud.iot.sample.Connection;
import com.tencent.qcloud.iot.sample.R;
import com.tencent.qcloud.iot.sample.components.SubscrbeListItemAdapter;
import com.tencent.qcloud.iot.sample.constant.Constants;
import com.tencent.qcloud.iot.sample.model.Subscribe;

import java.util.ArrayList;

/**
 * Created by rongerwu on 2018/1/15.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */

public class SubscribeFragment extends Fragment {

    private View mRootView;
    private Connection mConnection;
    private ArrayList<Subscribe> mSubscribes;

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
            View rootView = inflater.inflate(R.layout.fragment_subscribe, container, false);
            Button btnSubscribe = rootView.findViewById(R.id.subscribe_button);

            btnSubscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showInputDialog();
                }
            });

            ListView subscribeListView = rootView.findViewById(R.id.subscribe_list_view);
            mSubscribes = mConnection.getSubscribes();
            final SubscrbeListItemAdapter adapter = new SubscrbeListItemAdapter(getActivity(), mSubscribes);

            adapter.addOnUnsubscribeListner(new SubscrbeListItemAdapter.OnUnsubscribeListner() {
                @Override
                public void onUnsubscribe(Subscribe subscribe) {
                    mConnection.unsubscribe(subscribe.getTopic());
                }
            });
            mConnection.setSubscribeStateListener(new Connection.ISubscribeStateListener() {
                @Override
                public void onSubscribeStateChanged() {
                    mSubscribes.clear();
                    mSubscribes.addAll(mConnection.getSubscribes());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            });
            subscribeListView.setAdapter(adapter);
            mRootView = rootView;
        }
        return mRootView;
    }

    private void showInputDialog() {
        LayoutInflater layoutInflater = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View promptView = layoutInflater.inflate(R.layout.dialog_subscribe, null);
        final EditText topicText = promptView.findViewById(R.id.subscribe_topic_edit_text);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String topic = topicText.getText().toString();
                mConnection.subscribe(topic);
            }

        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alert.show();
    }
}
