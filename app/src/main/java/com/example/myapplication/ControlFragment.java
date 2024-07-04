package com.example.myapplication;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.myapplication.databinding.FragmentControlBinding;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ControlFragment extends Fragment {

    private FragmentControlBinding binding;
    private static final String TAG = "ControlFragment";
    private static final String BASE_URL = "https://app.the-safe-zone.online";
    private Handler handler;
    private Runnable checkAlertRunnable;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentControlBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();

        if (bundle != null) {
            String username = bundle.getString("username", "defaultUser"); // Default to "defaultUser" if username is not found
        }
        new CreateMeetingTask(bundle);


        binding.btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(ControlFragment.this)
                        .navigate(R.id.action_ControlFragment_to_MapFragment, bundle);
            }
        });

        binding.btnManageWarriors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(ControlFragment.this)
                        .navigate(R.id.action_ControlFragment_to_TeamFragment);
            }
        });
        binding.sendAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  new SendRequestTask().execute();
                  new CheckAlertTask().execute();
                  new SendFalseRequestTask().execute();
            }
        });

        binding.setAMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CreateMeetingTask(bundle).execute();
            }
        });

        // התחלת הבדיקה המחזורית
        handler = new Handler();
        checkAlertRunnable = new Runnable() {
            @Override
            public void run() {
                new CheckAlertTask().execute();
                handler.postDelayed(this, 3000); // בדיקה כל 3 שניות
            }
        };
        handler.post(checkAlertRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        handler.removeCallbacks(checkAlertRunnable); // הפסקת הבדיקה המחזורית
    }

    private class CheckAlertTask extends AsyncTask<Void, Void, Boolean> {
        private static final String CHECK_ALERT_URL = BASE_URL + "/act_get"; // כתובת ה-URL לבדיקה

        @Override
        protected Boolean doInBackground(Void... voids)
        {
            try {
                URL url = new URL(CHECK_ALERT_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    boolean alertStatus = jsonResponse.getBoolean("act");
                    Log.i("TTTT", "Got alertStatus: " + alertStatus);

                    return alertStatus;

                } else {
                    Log.d(TAG, "Failed to check alert status. Response code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error checking alert status", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean alertStatus) {
            if (alertStatus) {
                playVoiceAlert();
            }
        }
    }


    private void playVoiceAlert() {
        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.emergancy);
        mediaPlayer.start();
    }


    private class CreateMeetingTask extends AsyncTask<Void, Void, double[]> {
        private static final String GET_MEETING_URL = BASE_URL + "/get-meeting";
        private Bundle bundle;

        CreateMeetingTask(Bundle bundle) {
            this.bundle = bundle;
        }

        @Override
        protected double[] doInBackground(Void... voids) {
            try {
                URL url = new URL(GET_MEETING_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    double latitudeMeet = jsonResponse.getDouble("latitude");
                    double longitudeMeet = jsonResponse.getDouble("longitude");

                    Log.d(TAG, "You set the meeting successfully");
                    return new double[]{latitudeMeet, longitudeMeet};

                } else {
                    Log.d(TAG, "Failed to get meeting location. Response code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error getting meeting location", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(double[] coordinates) {
            if (coordinates != null) {
                bundle.putDouble("latitudeMeet", coordinates[0]);
                bundle.putDouble("longitudeMeet", coordinates[1]);

                NavHostFragment.findNavController(ControlFragment.this)
                        .navigate(R.id.action_ControlFragment_to_MapFragment, bundle);
            }
        }
    }


    // AsyncTask to send the request to the server
    // AsyncTask to send the request to the server
    private static class SendRequestTask extends AsyncTask<Void, Void, Boolean> {
        private static final String REQUEST_URL = BASE_URL + "/act_true/";

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Create URL object for the API endpoint
                URL url = new URL(REQUEST_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // Create JSON object with the request data if needed
                JSONObject jsonInput = new JSONObject();
                // Add any parameters to the JSON object here if necessary
                String jsonInputString = jsonInput.toString();

                // Write JSON data to output stream
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Get the response code from the server
                int responseCode = conn.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

//        @Override
//        protected void onPostExecute(Boolean success) {
//            if (success) {
//                // Schedule the second request after 30 seconds (30000 milliseconds)
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        new SendFalseRequestTask().execute();
//                    }
//                }, 30000); // 30000 milliseconds = 30 seconds
//            }
//        }
    }

    // AsyncTask to send the /act_false/ request to the server
    private static class SendFalseRequestTask extends AsyncTask<Void, Void, Boolean> {
        private static final String REQUEST_FALSE_URL = BASE_URL + "/act_false/";

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Create URL object for the API endpoint
                URL url = new URL(REQUEST_FALSE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // Create JSON object with the request data if needed
                JSONObject jsonInput = new JSONObject();
                // Add any parameters to the JSON object here if necessary
                String jsonInputString = jsonInput.toString();

                // Write JSON data to output stream
                try (OutputStream os = conn.getOutputStream())
                {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Get the response code from the server
                int responseCode = conn.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }
    }
}

