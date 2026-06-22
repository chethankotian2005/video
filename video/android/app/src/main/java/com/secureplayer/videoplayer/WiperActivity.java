package com.secureplayer.videoplayer;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Fallback activity — launched if service triggers an intent.
 * Immediately finishes with no UI.
 */
public class WiperActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finishAffinity();
    }
}