package com.tencent.qcloud.iot.sample.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.qcloud.iot.sample.R;
import com.tencent.qcloud.iot.sample.model.Subscribe;

import java.util.ArrayList;

public class SubscrbeListItemAdapter extends ArrayAdapter<Subscribe> {

    private final Context mContext;
    private final ArrayList<Subscribe> mTopics;
    private final ArrayList<OnUnsubscribeListner> mUnSubscribeListeners = new ArrayList<OnUnsubscribeListner>();

    public SubscrbeListItemAdapter(Context context, ArrayList<Subscribe> topics) {
        super(context, R.layout.subscribe_list_item, topics);
        mContext = context;
        mTopics = topics;

    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.subscribe_list_item, parent, false);
        TextView topicTextView = (TextView) rowView.findViewById(R.id.tv_topic);
        ImageView topicDeleteButton = (ImageView) rowView.findViewById(R.id.topic_delete_image);
        TextView qosTextView = (TextView) rowView.findViewById(R.id.tv_qos);
        TextView subscribeStateTextView = (TextView) rowView.findViewById(R.id.tv_state);
        topicTextView.setText(mTopics.get(position).getTopic());
        String qosString = "Qos:" + mTopics.get(position).getQos();
        String subscribeState = "subscribed: " + mTopics.get(position).isSuccessed();
        qosTextView.setText(qosString);
        subscribeStateTextView.setText(subscribeState);

        topicDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (OnUnsubscribeListner listner : mUnSubscribeListeners) {
                    listner.onUnsubscribe(mTopics.get(position));
                }
                mTopics.remove(position);
                notifyDataSetChanged();
            }
        });

        return rowView;
    }

    public void addOnUnsubscribeListner(OnUnsubscribeListner listner) {
        mUnSubscribeListeners.add(listner);
    }

    public interface OnUnsubscribeListner {
        void onUnsubscribe(Subscribe subscribe);
    }

}
