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
import android.os.Looper;
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
    private double latitudeMeet;
    private double longitudeMeet;
    private boolean isInDanger;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable locationRunnable;
    private Runnable mapRunnable;
    private boolean isRunning = false;

    String username = "";
    String adminPhoneNumber="";
    private static final int REQUEST_PHONE_PERMISSION = 2;
    private static final String BASE_URL = "https://app.the-safe-zone.online";

    //list of locations
    private List<LocationData> locations = new ArrayList<>();

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
            latitudeMeet = bundle.getDouble("latitudeMeet", 0.0);
            longitudeMeet = bundle.getDouble("longitudeMeet", 0.0);

        }

        webView = binding.mapview;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Call the JavaScript function after the page has loaded
                if (latitudeMeet != 0.0 && longitudeMeet != 0.0) {
                    webView.evaluateJavascript("updateAverageLocation(" + latitudeMeet + ", " + longitudeMeet + ");", null);
                }
            }
        });




        binding.buttonMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(MapFragment.this)
                        .navigate(R.id.action_MapFragment_to_MessagesFragment, bundle);
            }
        });

        binding.buttonPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPhoneButtonClick(v);
            }
        });



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

        binding.buttonEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendEmergencyRequestTask().execute(username);
            }
        });

        binding.buttonCancelEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendCancelEmergencyRequestTask().execute(username);
            }
        });

        new GetAdminPhoneTask().execute();
    }

    private void startLocationUpdates(final String username) {
        isRunning = true;

        locationRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    getLastLocation(username);
                    handler.postDelayed(this, 7000); // 10 שניות
                }
            }
        };

        mapRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    loadMapWithLocation(latitude, longitude, locations, username,isInDanger);
                }
                handler.postDelayed(this, 7000); // 5 שניות
            }

        };

        handler.post(locationRunnable);
        handler.post(mapRunnable);
    }

    private void stopLocationUpdates() {
        isRunning = false;
        handler.removeCallbacks(locationRunnable);
        handler.removeCallbacks(mapRunnable);
    }

    @Override
    public void onStart() {
        super.onStart();
        startLocationUpdates(username); // החלף בשם המשתמש המתאים
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLocationUpdates();
    }


    public void createMeetingWithAverageLocation(double latitude, double longitude) {
        // Call JavaScript function from WebView
        if(latitude!= 0 && longitude!= 0)
        {String jsCode = "javascript:updateAverageLocation(" + latitude + ", " + longitude + ")";
            webView.evaluateJavascript(jsCode, null);}

    }
    // Phone ----------------------------------------------------------
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
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" +adminPhoneNumber));

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Phone permission denied.");
            // Optionally, handle permission not granted case
        }
    }

    private class GetAdminPhoneTask extends AsyncTask<Void, Void, String> {
        private static final String ADMIN_PHONE_URL = BASE_URL + "/admin-phone/";

        @Override
        protected String doInBackground(Void... voids) {
            String response = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(ADMIN_PHONE_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder responseBuffer = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        responseBuffer.append(inputLine);
                    }
                    in.close();
                    response = responseBuffer.toString();
                } else {
                    response = "Error: " + responseCode;
                }
            } catch (Exception e) {
                response = "Exception: " + e.getMessage();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "Admin phone number: " + result);
            adminPhoneNumber = result; // Store the phone number in the member variable
            // Handle the response here if needed, e.g., update UI
        }
    }

    // Locations-------------------------------------------------------------------


    // new class
    public static class LocationData {
        private double latitude;
        private double longitude;
        private String username;
        private boolean isInDanger;

        public LocationData(double latitude, double longitude, String username, boolean isInDanger) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.username = username;
            this.isInDanger = isInDanger;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getUsername() {
            return username;
        }
        public boolean getIsInDanger() {
            return isInDanger;
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
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
                && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            //if (hasLocationChanged(latitude, longitude)) {
                                Log.d(TAG, "Latitude: " + latitude + ", Longitude: " + longitude);

                                // Send current location to the server
                                sendLocationToServer(latitude, longitude, username,isInDanger);
                                // Fetch locations from the server instead of generating random ones
                                fetchLocationsFromServer();
                                // Update last known location

                                lastLatitude = latitude;
                                lastLongitude = longitude;
                           // }
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
    private void loadMapWithLocation(double latitude, double longitude, List<LocationData> locations, String username,boolean isInDanger) {
        String jsCode = "javascript:updateMapLocation(" + latitude + ", " + longitude + ", " + locationsToJson(locations) + ", '" + username +"', "+isInDanger+ ")";
        webView.evaluateJavascript(jsCode, null);
    }



    private String locationsToJson(List<LocationData> locations) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < locations.size(); i++) {
            LocationData loc = locations.get(i);
            json.append("[")
                    .append(loc.getLatitude()).append(", ")
                    .append(loc.getLongitude()).append(", '")
                    .append(loc.getUsername()).append("', ")
                    .append(loc.getIsInDanger()) // Add isInDanger parameter
                    .append("]");
            if (i < locations.size() - 1) {
                json.append(", ");
            }
        }
        json.append("]");
        return json.toString();
    }




    private void sendLocationToServer(double latitude, double longitude, String username,boolean isInDanger) {
        new SendLocationTask().execute(latitude, longitude, username,isInDanger);
    }

    private class SendLocationTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            double latitude = (double) params[0];
            double longitude = (double) params[1];
            String username = (String) params[2];
            boolean isInDanger=(boolean) params[3];
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
                jsonParam.put("isInDanger", isInDanger);

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

    private class FetchLocationsTask extends AsyncTask<Void, Void, List<LocationData>> {
        @Override
        protected List<LocationData> doInBackground(Void... voids) {
            List<LocationData> locations = new ArrayList<>();
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
                        String username = jsonObject.getString("username");
                        boolean isInDanger = jsonObject.getBoolean("isInDanger");
                        locations.add(new LocationData(lat, lon, username,isInDanger));
                        //locations.add(new LocationData(lat, lon, username));
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
        protected void onPostExecute(List<LocationData> fetchedLocations) {
            super.onPostExecute(fetchedLocations);
            if (fetchedLocations != null) {
                locations.clear();
                locations.addAll(fetchedLocations);
                // Load the map with the updated locations
                loadMapWithLocation(latitude, longitude, locations, username,isInDanger);

                createMeetingWithAverageLocation(latitudeMeet, longitudeMeet);
                // Check proximity to the fetched locations and play sound if necessary
                checkProximityAndPlaySound(requireContext(), latitude, longitude, fetchedLocations);
            }
        }
    }


    // Sound-------------------------------------------------------------------------------
    private void checkProximityAndPlaySound(Context context, double currentLatitude, double currentLongitude, List<LocationData> locations) {
        final float[] distance = new float[1];
        for (LocationData location : locations)
            if(!location.username.equals( username))
            {
                Location.distanceBetween(currentLatitude, currentLongitude, location.getLatitude(), location.getLongitude(), distance);
                if (distance[0] <= 10) { // If within 50 meters
                    playSound(context);
                    break; // Play sound once if any location is within 50 meters
                }
            }
    }


    private void playSound(Context context) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.bird);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mediaPlayer.start();
    }

    //emergency--------------------------------------------------------------------------------------

    private class SendCancelEmergencyRequestTask extends AsyncTask<String, Void, String> {
        private static final String EMERGENCY_URL = BASE_URL + "/isdangerfalse?user_name=";

        @Override
        protected String doInBackground(String... params) {
            isInDanger=false;
            String response = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(EMERGENCY_URL + params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setDoOutput(true);

                OutputStream os = urlConnection.getOutputStream();
                os.write("".getBytes("UTF-8"));
                os.close();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder responseBuffer = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        responseBuffer.append(inputLine);
                    }
                    in.close();
                    response = responseBuffer.toString();
                } else {
                    response = "Error: " + responseCode;
                }
            } catch (Exception e) {
                response = "Exception: " + e.getMessage();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "Response from server: " + result);
            // Handle the response here if needed
        }
    }

    private class SendEmergencyRequestTask extends AsyncTask<String, Void, String> {
        private static final String EMERGENCY_URL = BASE_URL + "/isdangertrue?user_name=";

        @Override
        protected String doInBackground(String... params) {
            isInDanger=true;
            String response = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(EMERGENCY_URL + params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setDoOutput(true);

                OutputStream os = urlConnection.getOutputStream();
                os.write("".getBytes("UTF-8"));
                os.close();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder responseBuffer = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        responseBuffer.append(inputLine);
                    }
                    in.close();
                    response = responseBuffer.toString();
                } else {
                    response = "Error: " + responseCode;
                }
            } catch (Exception e) {
                response = "Exception: " + e.getMessage();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "Response from server: " + result);
            // Handle the response here if needed
        }
    }

    public void animateEmergencyLocation(View v) {
        String jsCode = "javascript:updateEmergencyLocation(" + latitude + ", " + longitude + ")";
        webView.evaluateJavascript(jsCode, null);
    }

    //meeting---------------------------------------------------------------
    void createMeetingWithAverageLocation() {
        new CreateMeetingTask().execute();
    }
    class CreateMeetingTask extends AsyncTask<Void, Void, double[]> {
        private static final String GET_MEETING_URL = BASE_URL + "/get-meeting";

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
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    double latitude = jsonResponse.getDouble("latitude");
                    double longitude = jsonResponse.getDouble("longitude");

                    return new double[]{latitude, longitude};
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
        protected void onPostExecute(double[] averageLocation) {
            if (averageLocation != null) {
                Log.d(TAG, "Average location: Latitude " + averageLocation[0] + ", Longitude " + averageLocation[1]);
                // Update the map with the average location and add special icon
                loadMapWithLocation(averageLocation[0], averageLocation[1], locations, username,isInDanger);
                updateAverageLocationOnMap(averageLocation[0], averageLocation[1]);
            } else {
                Log.d(TAG, "Failed to get average location from server");
            }
        }
    }

    private void updateAverageLocationOnMap(double latitude, double longitude) {
        String jsCode = "javascript:updateAverageLocation(" + latitude + ", " + longitude + ")";
        webView.evaluateJavascript(jsCode, null);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}