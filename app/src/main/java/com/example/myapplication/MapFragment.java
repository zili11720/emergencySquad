package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
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

    private static final int REQUEST_PHONE_PERMISSION = 2;

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
            getLastLocation();
        }

    }

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

    private void getLastLocation() {
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
                            Log.d(TAG, "Latitude: " + latitude + ", Longitude: " + longitude);
                            List<double[]> locations = generateRandomLocations(latitude, longitude, 5); // Generate 5 random locations
                            loadMapWithLocation(latitude, longitude, locations);
                            checkProximityAndPlaySound(requireContext(), latitude, longitude, locations); // Check proximity and play sound
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
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alert); // Replace 'my_sound' with your actual sound file name without extension
        mediaPlayer.start();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadMapWithLocation(double latitude, double longitude, List<double[]> locations) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "WebView loaded URL: " + url);
                String jsCode = "javascript:updateMapLocation(" + latitude + ", " + longitude + ", " + locationsToJson(locations) + ")";
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onPhoneButtonClick(View view) {
        String phoneNumber ="0587300206"; // Replace with your desired phone number
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Phone permission denied.");
            // Optionally, handle permission not granted case
        }
    }
//פונקציה למציאת הנקודה הקרובה ביותר עבור כל המיקומים במפה
//    public class MapUtils {
//
//        // פונקציה לחישוב מרחק בין שתי נקודות במפה
//        private static float distanceBetweenLocations(Location loc1, Location loc2) {
//            float[] results = new float[1];
//            Location.distanceBetween(loc1.getLatitude(), loc1.getLongitude(),
//                    loc2.getLatitude(), loc2.getLongitude(), results);
//            return results[0];
//        }
//
//        // פונקציה למציאת נקודת האמצע של רשימת מיקומים
//        public static Location findCenterPoint(List<Location> locations) {
//            Location centerPoint = null;
//            double minDistanceSum = Double.MAX_VALUE;
//
//            // ללולאה כדי לחשב את נקודת האמצע
//            for (Location loc : locations) {
//                double distanceSum = 0;
//                for (Location otherLoc : locations) {
//                    if (!loc.equals(otherLoc)) {
//                        distanceSum += distanceBetweenLocations(loc, otherLoc);
//                    }
//                }
//                if (distanceSum < minDistanceSum) {
//                    minDistanceSum = distanceSum;
//                    centerPoint = loc;
//                }
//            }
//
//            return centerPoint;
//        }
//    }

}
