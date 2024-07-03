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
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.myapplication.databinding.FragmentFirstBinding;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private static final String BASE_URL = "https://app.the-safe-zone.online";

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private Bundle userBundle; // Store user bundle for later use

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

        // Set up BiometricPrompt
        setupBiometricPrompt();

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

                // Store the bundle for later use in biometric prompt
                userBundle = bundle;

                // Pass the bundle to the AsyncTask
                new ValidateUserTask(bundle).execute(usernameText, passwordText);
            }
        });
    }

    private void setupBiometricPrompt() {
        // Check BiometricManager and show a corresponding Toast
        BiometricManager biometricManager = BiometricManager.from(requireContext());
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                //Toast.makeText(requireContext(), "Biometric authentication is available", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(requireContext(), "This device doesn't support biometric authentication", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(requireContext(), "Biometric authentication is currently unavailable", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(requireContext(), "No biometric credentials are enrolled", Toast.LENGTH_SHORT).show();
                break;
        }

        // Create BiometricPrompt and configure it
        Executor executor = ContextCompat.getMainExecutor(requireContext());
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(requireContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(requireContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();

                // Check the user status from the bundle and navigate accordingly
                String status = userBundle.getString("status");
                if ("success_is_admin".equals(status)) {
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_ControlFragment, userBundle);
                } else if ("success_is_not_admin".equals(status)) {
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_MapFragment, userBundle);
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Configure the PromptInfo for biometric authentication
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build();
    }

    private void promptBiometricAuthentication() {
        // Trigger biometric authentication
        biometricPrompt.authenticate(promptInfo);
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
                conn.setRequestMethod("POST");
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

                    // Put the status in the bundle
                    bundle.putString("status", status);

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
                if ("success_is_admin".equals(status) || "success_is_not_admin".equals(status)) {
                    promptBiometricAuthentication(); // Prompt for biometric authentication if credentials are valid
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
