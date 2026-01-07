package com.coreinnovators.geokids;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class driver_dashboard extends AppCompatActivity {

    TextView hello;
    Button add;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);
        hello = findViewById(R.id.hello);
        add = findViewById(R.id.add);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserName();
        setupAddButton();
    }

    private void loadUserName() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String username = snapshot.getString("username");
                        if (username != null) {
                            hello.setText("Hello, " + username);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load user!", Toast.LENGTH_SHORT).show()
                );
    }

    private void setupAddButton() {
        add.setOnClickListener(v -> {
            Intent intent = new Intent(driver_dashboard.this, DriverFormActivity.class);
            startActivity(intent);
        });
    }
}