package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentFirstBinding;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private static final String BASE_URL = "https://app.the-safe-zone.online";

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonCreateTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_NewTeamFragment);
            }
        });

        binding.buttonSignIn.setOnClickListener(v -> {
            EditText username = view.findViewById(R.id.edittext_username);
            EditText password = view.findViewById(R.id.edittext_password);

            String usernameText = username.getText().toString();
            String passwordText = password.getText().toString();

            if (usernameText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter username and password", Toast.LENGTH_SHORT).show();
            } else {
                // Create a bundle and add the username to it
                Bundle bundle = new Bundle();
                bundle.putString("username", usernameText);

                // Pass the bundle to the AsyncTask
                new ValidateUserTask(bundle).execute(usernameText, passwordText);
            }
        });
    }

    public class ValidateUserTask extends AsyncTask<String, Void, Pair<Boolean, String>> {
        private static final String LOGIN_URL = BASE_URL + "/test-login";

        private final Bundle bundle;
        public ValidateUserTask(Bundle bundle) {
            this.bundle = bundle;
        }


        @Override
        protected Pair<Boolean, String> doInBackground(String... params) {
            String username = params[0];
            String password = params[1];

            try {
                URL url = new URL(LOGIN_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");  // Change to POST
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonInput = new JSONObject();
                jsonInput.put("username", username);
                jsonInput.put("password", password);
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
                    String status = jsonResponse.getString("status");
                    return new Pair<>(true, status);
                } else {
                    return new Pair<>(false, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new Pair<>(false, null);
            }
        }

        @Override
        protected void onPostExecute(Pair<Boolean, String> result) {
            Boolean isValid = result.first;
            String status = result.second;
            if (isValid) {
                if ("success_is_admin".equals(status)) {
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_ControlFragment,bundle);
                } else if ("success_is_not_admin".equals(status)) {
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_MapFragment,bundle);
                } else {
                    Toast.makeText(getActivity(), "Invalid status returned", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
