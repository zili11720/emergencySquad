package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private FragmentMapBinding binding;
    private static final String TAG = "MapFragment";
    private FusedLocationProviderClient fusedLocationClient;
    private WebView webView;

    private double latitude;
    private double longitude;
    private double lastLatitude = Double.NaN;
    private double lastLongitude = Double.NaN;
    String username = "";
    private static final int REQUEST_PHONE_PERMISSION = 2;

    private List<double[]> locations = new ArrayList<>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve the username from the bundle
        Bundle bundle = getArguments();

        if (bundle != null) {
            username = bundle.getString("username", "defaultUser"); // Default to "defaultUser" if username is not found
        }

        binding.buttonMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(MapFragment.this)
                        .navigate(R.id.action_MapFragment_to_MessagesFragment);
            }
        });
        binding.buttonPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPhoneButtonClick(v);
            }
        });

        webView = binding.mapview;

        // Request phone permission when the app is opened
        requestPhonePermission();

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Check for permission and get location
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            loadMap();
        }
    }

    // Phone Permission
    private void requestPhonePermission() {
        if (ActivityCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_PHONE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PHONE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                // Optionally handle permission granted
            } else {
                // Permission denied
                Log.d(TAG, "Phone permission denied");
            }
        }
    }

    public void onPhoneButtonClick(View view) {
        String phoneNumber = "0587300206"; // Replace with your desired phone number
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Phone permission denied.");
            // Optionally, handle permission not granted case
        }
    }

    // Locations
    private void loadMap() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "WebView loaded URL: " + url);
                // Get last known location and load map with it
                getLastLocation(username);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "WebView error: " + description + " at " + failingUrl);
            }
        });
        webView.loadUrl("file:///android_asset/leaflet_map.html");
        Log.d(TAG, "Loading URL: file:///android_asset/leaflet_map.html");
    }

    private void getLastLocation(String username) {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            if (hasLocationChanged(latitude, longitude)) {
                                Log.d(TAG, "Latitude: " + latitude + ", Longitude: " + longitude);
                                // Fetch locations from the server instead of generating random ones
                                fetchLocationsFromServer();
                                // Send current location to the server
                                sendLocationToServer(latitude, longitude, username);
                                // Update last known location
                                lastLatitude = latitude;
                                lastLongitude = longitude;
                            }
                        } else {
                            Log.d(TAG, "Location is null");
                        }
                    }
                });
    }

    private boolean hasLocationChanged(double latitude, double longitude) {
        return Double.isNaN(lastLatitude) || Double.isNaN(lastLongitude) || lastLatitude != latitude || lastLongitude != longitude;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadMapWithLocation(double latitude, double longitude, List<double[]> locations, String username) {
        String jsCode = "javascript:updateMapLocation(" + latitude + ", " + longitude + ", " + locationsToJson(locations) + ", '" + username + "')";
        webView.evaluateJavascript(jsCode, null);
    }

    private String locationsToJson(List<double[]> locations) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < locations.size(); i++) {
            double[] loc = locations.get(i);
            json.append("[").append(loc[0]).append(", ").append(loc[1]).append("]");
            if (i < locations.size() - 1) {
                json.append(", ");
            }
        }
        json.append("]");
        return json.toString();
    }

    // Sound
    private void checkProximityAndPlaySound(Context context, double currentLatitude, double currentLongitude, List<double[]> locations) {
        final float[] distance = new float[1];
        for (double[] location : locations) {
            Location.distanceBetween(currentLatitude, currentLongitude, location[0], location[1], distance);
            if (distance[0] <= 50) { // If within 50 meters
                playSound(context);
                break; // Play sound once if any location is within 50 meters
            }
        }
    }

    private void playSound(Context context) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.bird); // Replace 'my_sound' with your actual sound file name without extension
        mediaPlayer.start();
    }

    // Server
    private void sendLocationToServer(double latitude, double longitude, String username) {
        new SendLocationTask().execute(latitude, longitude, username);
    }

    private class SendLocationTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            double latitude = (double) params[0];
            double longitude = (double) params[1];
            String username = (String) params[2];
            try {
                URL url = new URL("https://app.the-safe-zone.online/add_location/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("latitude", latitude);
                jsonParam.put("longitude", longitude);
                jsonParam.put("username", username);

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Location updated successfully");
                } else {
                    Log.d(TAG, "Failed to update location. Response code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error updating location", e);
            }
            return null;
        }
    }

    private void fetchLocationsFromServer() {
        new FetchLocationsTask().execute();
    }

    private class FetchLocationsTask extends AsyncTask<Void, Void, List<double[]>> {
        @Override
        protected List<double[]> doInBackground(Void... voids) {
            List<double[]> locations = new ArrayList<>();
            try {
                URL url = new URL("https://app.the-safe-zone.online/get-locations");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        double lat = jsonObject.getDouble("latitude");
                        double lon = jsonObject.getDouble("longitude");
                        locations.add(new double[]{lat, lon});
                    }

                } else {

                    Log.d(TAG, "Failed to fetch locations. Response code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error fetching locations", e);
            }
            return locations;
        }

        @Override
        protected void onPostExecute(List<double[]> fetchedLocations) {
            super.onPostExecute(fetchedLocations);
            if (fetchedLocations != null) {
                locations.clear();
                locations.addAll(fetchedLocations);
                // Load the map with the updated locations
                loadMapWithLocation(latitude, longitude, locations, username);
                // Check proximity to the fetched locations and play sound if necessary
                checkProximityAndPlaySound(requireContext(), latitude, longitude, fetchedLocations);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
