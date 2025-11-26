package com.coreinnovators.geokids;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.coreinnovators.geokids.R;
import com.coreinnovators.geokids.login;

public class MainActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 2500; // 2.5 seconds
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        animateProgressBar();

        // Navigate to LoginActivity after splash duration
        new Handler().postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, login.class));
            finish(); // close splash screen
        }, SPLASH_DURATION_MS);
    }

    private void animateProgressBar() {
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        animation.setDuration(SPLASH_DURATION_MS);
        animation.start();
    }
}
