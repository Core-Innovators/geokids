package com.coreinnovators.geokids;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class driver_active_dashboard extends AppCompatActivity {

    private static final String TAG = "DriverActiveDashboard";

    private TextView driverNameTv;
    private ToggleButton toggleButton;
    private ImageView notificationBell;

    // Action Cards
    private CardView availablePickupsCard, viewRequestsCard, contactSupportCard;

    // Bottom Navigation
    private LinearLayout navHome, navLocation, navQr, navProfile;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean isRideActive = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_active_dashboard);


        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Load driver name
        loadDriverName();

        // Load ride status
        loadRideStatus();

        // Set up listeners
        setupClickListeners();
    }

    private void initializeViews() {
        driverNameTv = findViewById(R.id.driver_name);
        toggleButton = findViewById(R.id.toggleButton);
        notificationBell = findViewById(R.id.notification_bell);

        // Action cards
        availablePickupsCard = findViewById(R.id.available_pickups_card);
        viewRequestsCard = findViewById(R.id.view_requests_card);
        contactSupportCard = findViewById(R.id.contact_support_card);

        // Bottom navigation
        navHome = findViewById(R.id.nav_home);
        navLocation = findViewById(R.id.nav_location);
        navQr = findViewById(R.id.nav_qr);
        navProfile = findViewById(R.id.nav_profile);

        // Set toggle button text
        toggleButton.setTextOn("Active");
        toggleButton.setTextOff("Inactive");
    }

    private void loadDriverName() {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "Current user is null!");
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        Log.d(TAG, "Loading name for UID: " + uid);

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d(TAG, "Document exists: " + snapshot.exists());
                    if (snapshot.exists()) {
                        Log.d(TAG, "All document data: " + snapshot.getData());
                        String name = snapshot.getString("name");
                        Log.d(TAG, "Name field value: " + name);

                        if (name != null) {
                            driverNameTv.setText(name);
                            Log.d(TAG, "Name set successfully: " + name);
                        } else {
                            Log.w(TAG, "Name field is null");
                        }
                    } else {
                        Log.w(TAG, "Document does not exist for UID: " + uid);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load user data: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load user!", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRideStatus() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("drivers").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Boolean rideActive = snapshot.getBoolean("rideActive");
                        isRideActive = (rideActive != null) ? rideActive : false;
                        toggleButton.setChecked(isRideActive);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error loading ride status: " + e.getMessage())
                );
    }

    private void setupClickListeners() {
        // Toggle button listener
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateRideStatus(isChecked);
        });

        // Action cards
        availablePickupsCard.setOnClickListener(v -> {
            startActivity(new Intent(driver_active_dashboard.this, available_pickup.class));
        });

        viewRequestsCard.setOnClickListener(v -> {
            startActivity(new Intent(driver_active_dashboard.this, view_request.class));
        });

        contactSupportCard.setOnClickListener(v -> {
            Toast.makeText(this, "Contact Support: +94 XX XXX XXXX", Toast.LENGTH_LONG).show();
        });

        // Notification bell
        notificationBell.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation
        navHome.setOnClickListener(v -> {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        });

        navLocation.setOnClickListener(v -> {
            startActivity(new Intent(driver_active_dashboard.this, driver_map.class));
        });

        navQr.setOnClickListener(v -> {
            startActivity(new Intent(driver_active_dashboard.this, QR_scan.class));
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(driver_active_dashboard.this, driver_profile.class));
        });
    }

    private void updateRideStatus(boolean isActive) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> rideStatus = new HashMap<>();
        rideStatus.put("rideActive", isActive);
        rideStatus.put("lastUpdated", System.currentTimeMillis());

        db.collection("drivers").document(uid)
                .set(rideStatus, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    isRideActive = isActive;
                    String message = isActive ? "Ride started" : "Ride ended";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Ride status updated: " + isActive);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating ride status: " + e.getMessage());
                    Toast.makeText(this, "Failed to update ride status", Toast.LENGTH_SHORT).show();
                    toggleButton.setChecked(!isActive);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRideStatus();
    }
}