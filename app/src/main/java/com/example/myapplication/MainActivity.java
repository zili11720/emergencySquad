package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float MOVEMENT_THRESHOLD = 1.0f; // Adjust threshold for movement detection
    private static final float ALPHA = 0.8f; // Smoothing factor for low-pass filter
    private boolean isAlertShown = false;
    private float[] gravity = new float[3];

    private Handler handler;
    private Runnable noMovementRunnable;
    private static final long NO_MOVEMENT_TIMEOUT = 180000; // 3 minutes timeout


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        // Sensor setup
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // Handler and Runnable setup for no movement detection
        handler = new Handler(Looper.getMainLooper());
        noMovementRunnable = this::showAlertDialog;
        resetNoMovementTimer();
    }

    private void showAlertDialog() {
        if (!isAlertShown) {
            Log.d(TAG, "Showing alert dialog");
//            new AlertDialog.Builder(this)
//                    .setTitle("Alert")
//                    .setMessage("The device has not moved for 3 minuteד.")
//                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
//                        isAlertShown = false;
//                        resetNoMovementTimer(); // Reset the timer after the alert is dismissed
//                    })
                   // .show();
            isAlertShown = true;
        }
    }

    private void resetNoMovementTimer() {
        handler.removeCallbacks(noMovementRunnable);
        handler.postDelayed(noMovementRunnable, NO_MOVEMENT_TIMEOUT);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Apply low-pass filter to isolate gravity
        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * event.values[0];
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * event.values[1];
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * event.values[2];

        float x = event.values[0] - gravity[0];
        float y = event.values[1] - gravity[1];
        float z = event.values[2] - gravity[2];

        Log.d(TAG, "Sensor changed. x: " + x + ", y: " + y + ", z: " + z);

        if (Math.abs(x) > MOVEMENT_THRESHOLD || Math.abs(y) > MOVEMENT_THRESHOLD || Math.abs(z) > MOVEMENT_THRESHOLD) {
            resetNoMovementTimer();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something if sensor accuracy changes
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(noMovementRunnable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            // Pass the result to the fragment
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
            if (navHostFragment != null) {
                navHostFragment.getChildFragmentManager().getFragments().forEach(fragment -> {
                    if (fragment instanceof MapFragment) {
                        ((MapFragment) fragment).onRequestPermissionsResult(requestCode, permissions, grantResults);
                    }
                });
            }
        }
    }
}
