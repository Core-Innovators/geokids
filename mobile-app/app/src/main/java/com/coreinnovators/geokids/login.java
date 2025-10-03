package com.coreinnovators.geokids;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Find the register TextView
        TextView registerText = findViewById(R.id.register);

        // Set click listener
        registerText.setOnClickListener(v -> {
            // Navigate to signup activity
            Intent intent = new Intent(login.this, signup.class);
            startActivity(intent);
        });
    }
}
