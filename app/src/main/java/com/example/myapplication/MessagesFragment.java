package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;

public class MessagesFragment extends Fragment {

    private ListView listViewMessages;
    private EditText editTextMessage;
    private Button btnSendMessage;
    private ArrayList<String> messages;
    private ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        listViewMessages = view.findViewById(R.id.listViewMessages);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);

        messages = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, messages);
        listViewMessages.setAdapter(adapter);

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString();
                if (!message.isEmpty()) {
                    messages.add(message);
                    adapter.notifyDataSetChanged();
                    editTextMessage.setText("");
                }
            }
        });

        return view;
    }
}
