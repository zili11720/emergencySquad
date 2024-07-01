package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import androidx.fragment.app.Fragment;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MessagesFragment extends Fragment {

    // Views and data for displaying messages
    private ListView listViewMessages;
    private EditText editTextMessage;
    private Button btnSendMessage;
    private ArrayList<String> messages;
    private ArrayAdapter<String> adapter;
    private static final String BASE_URL = "https://app.the-safe-zone.online";

    public MessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        // Initialize views
        listViewMessages = view.findViewById(R.id.listViewMessages);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);

        // Initialize message list and adapter
        messages = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, messages);
        listViewMessages.setAdapter(adapter);

        // Set up the button click listener
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the message text, user name, and current timestamp
                String message = editTextMessage.getText().toString();
                String userName = getUserName(); // Function to get the username
                String timestamp = getCurrentTimestamp(); // Function to get the current timestamp

                // Check if the message is not empty
                if (!message.isEmpty()) {
                    // Add the message to the list with username and update the adapter
                    messages.add(userName + ": " + timestamp + "                                                                " + message);
                    adapter.notifyDataSetChanged();

                    // Execute the task to send the message to the server
                    new SendMessageTask().execute(message, userName, timestamp);

                    // Clear the input field
                    editTextMessage.setText("");
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch messages when the fragment is resumed
        fetchMessages();
    }

    // Function to get the username (could be retrieved from SharedPreferences or other sources)
    private String getUserName() {
        return "exampleUserName";
    }

    // Function to get the current timestamp in the desired format
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }


    // Function to fetch messages from the server
    private void fetchMessages() {
        new FetchMessagesTask(this).execute();
    }

    // Function to update the messages in the ListView
    public void updateMessages(ArrayList<String> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        adapter.notifyDataSetChanged();
    }

    // AsyncTask to fetch messages from the server
    private static class FetchMessagesTask extends AsyncTask<Void, Void, ArrayList<String>> {
        private static final String FETCH_MESSAGES_URL = BASE_URL + "/read_messages/";
        private MessagesFragment fragment;

        public FetchMessagesTask(MessagesFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> messageList = new ArrayList<>();
            try {
                URL url = new URL(FETCH_MESSAGES_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine.trim());
                }
                in.close();

                // Parse JSON response
                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String messageContent = jsonObject.getString("content");
                    String sender = jsonObject.getString("send");
                    String timestamp = jsonObject.getString("time");
                    messageList.add(sender + ": " + timestamp + "                                                  " + messageContent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return messageList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> messages) {
            fragment.updateMessages(messages);
        }
    }

    // AsyncTask to send the message to the server
    private static class SendMessageTask extends AsyncTask<String, Void, Boolean> {
        private static final String SEND_MESSAGE_URL = BASE_URL + "/create_message/";

        @Override
        protected Boolean doInBackground(String... params) {
            // Extract parameters
            String content = params[0]; // Message content
            String userName = params[1]; // User name
            String timestamp = params[2]; // Timestamp

            try {
                // Create URL object for the API endpoint
                URL url = new URL(SEND_MESSAGE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // Create JSON object with the message data
                JSONObject jsonInput = new JSONObject();
                jsonInput.put("send", userName);
                jsonInput.put("content", content);
                jsonInput.put("time", timestamp);
                String jsonInputString = jsonInput.toString();

                // Write JSON data to output stream
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Get the response code from the server
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read response from input stream
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine.trim());
                    }
                    in.close();

                    // Parse response and check for success
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.has("message") && jsonResponse.getString("message").equals("Message created successfully");
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
