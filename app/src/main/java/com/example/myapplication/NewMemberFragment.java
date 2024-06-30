package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewMemberFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewMemberFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String BASE_URL = "https://app.the-safe-zone.online";

    private String mParam1;
    private String mParam2;

    public NewMemberFragment() {
        // Required empty public constructor
    }

    public static NewMemberFragment newInstance(String param1, String param2) {
        NewMemberFragment fragment = new NewMemberFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Add a New Team Friend");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_member, container, false);

        EditText editTextName = view.findViewById(R.id.editTextName);
        EditText editTextId = view.findViewById(R.id.editTextId);
        EditText editTextPhoneNumber = view.findViewById(R.id.editTextPhoneNumber);
        EditText editTextPassword = view.findViewById(R.id.editTextPassword);
        EditText editTextAddress = view.findViewById(R.id.editTextAddress);
        EditText editTextRole = view.findViewById(R.id.editTextRole);
        Button buttonAdd = view.findViewById(R.id.buttonAdd);

        buttonAdd.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String phoneNumber = editTextPhoneNumber.getText().toString();
            String address = editTextAddress.getText().toString();
            String id = editTextId.getText().toString();
            String password = editTextPassword.getText().toString();
            String role = editTextRole.getText().toString();

            if (!name.isEmpty() && !phoneNumber.isEmpty() && !address.isEmpty() && !id.isEmpty() && !password.isEmpty() && !role.isEmpty()) {
                new CreateUserTask().execute(name, phoneNumber, address, id, password, role);
            } else {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    public class CreateUserTask extends AsyncTask<String, Void, Boolean> {
        private static final String CREATE_USER_URL = BASE_URL + "/create-user";

        @Override
        protected Boolean doInBackground(String... params) {
            String name = params[0];
            String phoneNumber = params[1];
            String address = params[2];
            String id = params[3];
            String password = params[4];
            String role = params[5];

            try {
                URL url = new URL(CREATE_USER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonInput = new JSONObject();
                jsonInput.put("id", id);
                jsonInput.put("username", name);
                jsonInput.put("password", password);
                jsonInput.put("role", role);
                jsonInput.put("phone_number", phoneNumber);
                jsonInput.put("address", address);
                String jsonInputString = jsonInput.toString();

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine.trim());
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.has("status") && jsonResponse.getString("status").equals("success");
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if (isSuccess) {
                Toast.makeText(getActivity(), "User created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Failed to create user", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
