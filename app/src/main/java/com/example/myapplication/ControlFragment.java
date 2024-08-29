package com.example.myapplication;

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
    static final String BASE_URL = "https://app.the-safe-zone.online";
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

        binding.setAMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CreateMeetingTask(bundle).execute();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        //handler.removeCallbacks(checkAlertRunnable); // הפסקת הבדיקה המחזורית
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
        private static final String REQUEST_TRUE_URL = BASE_URL + "/act_true/";
        private static final String REQUEST_FALSE_URL = BASE_URL + "/act_false/";

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpURLConnection conn = null;
            OutputStream os = null;
            try {
                URL url = new URL(REQUEST_TRUE_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonInput = new JSONObject();
                String jsonInputString = jsonInput.toString();

                os = conn.getOutputStream();
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);

                int responseCode = conn.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       // new SendFalseRequestTask().execute();
                    }
                }, 30000); // 30000 milliseconds = 30 seconds
            }
        }
    }
}