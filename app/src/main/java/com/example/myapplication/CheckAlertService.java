//package com.example.myapplication;
//
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.Service;
//import android.content.Intent;
//import android.os.Build;
//import android.os.IBinder;
//
//import androidx.annotation.Nullable;
//
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//public class CheckAlertService extends Service {
//
//    private ScheduledExecutorService scheduler;
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        startForegroundService();
//        scheduler = Executors.newScheduledThreadPool(1);
//        scheduler.scheduleWithFixedDelay(new Runnable() {
//            @Override
//            public void run() {
//                new CheckAlertTask(getApplicationContext()).execute();
//            }
//        }, 0, 3, TimeUnit.SECONDS);
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (scheduler != null) {
//            scheduler.shutdown();
//        }
//    }
//
//    private void startForegroundService() {
//        String channelId = "alert_service_channel";
//        String channelName = "Alert Service Channel";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            if (manager != null) {
//                manager.createNotificationChannel(channel);
//            }
//        }
//
//
//    }
//}
