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
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapFragment extends Fragment {

    private FragmentMapBinding binding;
    private static final String TAG = "MapFragment";
    private FusedLocationProviderClient fusedLocationClient;
    private WebView webView;

    private double latitude;
    private double longitude;
    private double lastLatitude;
    private double lastLongitude;
    String username = "";
    private static final int REQUEST_PHONE_PERMISSION = 2;

    private Handler handler = new Handler();
    private Runnable locationRunnable;

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
            getLastLocation(username);
        }

        // Initialize location update runnable
        locationRunnable = new Runnable() {
            @Override
            public void run() {
                checkLocationChange();
                handler.postDelayed(this, 5000); // Run every 5 seconds
            }
        };
        handler.post(locationRunnable);
    }

    // phone---------------------------------------------
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

    // locations----------------------------------------------------------------------------
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
                            lastLatitude = latitude;
                            lastLongitude = longitude;
                            Log.d(TAG, "Latitude: " + latitude + ", Longitude: " + longitude);
                            List<double[]> locations = generateRandomLocations(latitude, longitude, 5); // Generate 5 random locations
                            loadMapWithLocation(latitude, longitude, locations, username);
                            checkProximityAndPlaySound(requireContext(), latitude, longitude, locations); // Check proximity and play sound
                            sendLocationToServer(latitude, longitude, username); // Send location to server
                        } else {
                            Log.d(TAG, "Location is null");
                        }
                    }
                });
    }

    private void checkLocationChange() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double newLatitude = location.getLatitude();
                            double newLongitude = location.getLongitude();
                            if (newLatitude != lastLatitude || newLongitude != lastLongitude) {
                                lastLatitude = newLatitude;
                                lastLongitude = newLongitude;
                                Log.d(TAG, "New Latitude: " + newLatitude + ", New Longitude: " + newLongitude);
                                sendLocationToServer(newLatitude, newLongitude, username);
                            }
                        } else {
                            Log.d(TAG, "Location is null");
                        }
                    }
                });
    }

    private List<double[]> generateRandomLocations(double latitude, double longitude, int count) {
        List<double[]> locations = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            double latOffset = (random.nextDouble() - 0.5) / 1000; // random offset in the range of ±0.0005
            double lonOffset = (random.nextDouble() - 0.5) / 1000; // random offset in the range of ±0.0005
            locations.add(new double[]{latitude + latOffset, longitude + lonOffset});
        }
        return locations;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadMapWithLocation(double latitude, double longitude, List<double[]> locations, String username) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "WebView loaded URL: " + url);
                String jsCode = "javascript:updateMapLocation(" + latitude + ", " + longitude + ", " + locationsToJson(locations) + ", '" + username + "')";
                webView.evaluateJavascript(jsCode, null);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "WebView error: " + description + " at " + failingUrl);
            }
        });
        webView.loadUrl("file:///android_asset/leaflet_map.html");
        Log.d(TAG, "Loading URL: file:///android_asset/leaflet_map.html");
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

    // sound-----------------------------------------------------------------------------
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        handler.removeCallbacks(locationRunnable); // Stop the runnable when the fragment is destroyed
    }

    @SuppressLint("StaticFieldLeak")
    private void sendLocationToServer(double latitude, double longitude, String username) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    URL url = new URL("https://app.the-safe-zone.online/add_location/");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("latitude", latitude);
                    jsonParam.put("longitude", longitude);
                    jsonParam.put("username", username);

                    OutputStream os = connection.getOutputStream();
                    os.write(jsonParam.toString().getBytes());
                    os.flush();
                    os.close();

                    int responseCode = connection.getResponseCode();
                    connection.disconnect();
                    return responseCode == HttpURLConnection.HTTP_OK;

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @SuppressLint("StaticFieldLeak")
            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                String message = success ? "Location sent successfully" : "Failed to send location";
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
}
