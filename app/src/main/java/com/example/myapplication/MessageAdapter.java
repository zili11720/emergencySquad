package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MessageAdapter extends ArrayAdapter<Message> {
    public MessageAdapter(Context context, ArrayList<Message> messages) {
        super(context, 0, messages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_message, parent, false);
        }

        TextView tvSender = convertView.findViewById(R.id.tvSender);
        TextView tvTimestamp = convertView.findViewById(R.id.tvTimestamp);
        TextView tvMessageContent = convertView.findViewById(R.id.tvMessageContent);

        if (message != null) {
            tvSender.setText(message.getSender());
            tvTimestamp.setText(message.getTimestamp());
            tvMessageContent.setText(message.getContent());
        }

        return convertView;
    }
}
