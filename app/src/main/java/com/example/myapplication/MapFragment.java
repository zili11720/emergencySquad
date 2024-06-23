package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
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

        binding.buttonSecond.setOnClickListener(v ->
                NavHostFragment.findNavController(MapFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment)
        );

        webView = binding.mapview;

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
}
