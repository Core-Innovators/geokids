package com.coreinnovators.geokids;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class driver_active_dashboard extends AppCompatActivity {

    private static final String TAG = "DriverActiveDashboard";

    private TextView driverNameTv;
    private ToggleButton toggleButton;
    private ImageView notificationBell;
    private LinearLayout activityFeedContainer;

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

        // Load activity feed
        loadActivityFeed();

        // Set up listeners
        setupClickListeners();
    }

    private void initializeViews() {
        driverNameTv = findViewById(R.id.driver_name);
        toggleButton = findViewById(R.id.toggleButton);
        notificationBell = findViewById(R.id.notification_bell);
        activityFeedContainer = findViewById(R.id.activity_feed);

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

        db.collection("drivers").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d(TAG, "Document exists: " + snapshot.exists());
                    if (snapshot.exists()) {
                        Log.d(TAG, "All document data: " + snapshot.getData());

                        String name = snapshot.getString("fullName");

                        if (name == null || name.isEmpty()) {
                            name = snapshot.getString("name");
                        }
                        if (name == null || name.isEmpty()) {
                            name = snapshot.getString("driverName");
                        }

                        Log.d(TAG, "Name field value: " + name);

                        if (name != null && !name.isEmpty()) {
                            driverNameTv.setText(name);
                            Log.d(TAG, "Name set successfully: " + name);
                        } else {
                            driverNameTv.setText("Driver");
                            Log.w(TAG, "Name field is null or empty");
                        }
                    } else {
                        Log.w(TAG, "Document does not exist for UID: " + uid);
                        driverNameTv.setText("Driver");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load driver data: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load driver data!", Toast.LENGTH_SHORT).show();
                    driverNameTv.setText("Driver");
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

    private void loadActivityFeed() {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "Cannot load activity feed: User not authenticated");
            return;
        }

        String driverId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Loading activity feed for driver: " + driverId);

        // Clear existing activities
        activityFeedContainer.removeAllViews();

        // Query pickups, dropoffs, and QR scanned activities
        loadActivitiesFromCollection("pickups", driverId);
        loadActivitiesFromCollection("dropoffs", driverId);
        loadActivitiesFromCollection("activities", driverId);
    }

    private void loadActivitiesFromCollection(String collectionName, String driverId) {
        Log.d(TAG, "Querying collection: " + collectionName + " for driver: " + driverId);

        // Query WITHOUT orderBy to avoid index requirement
        db.collection(collectionName)
                .whereEqualTo("driverId", driverId)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int docCount = queryDocumentSnapshots.size();
                    Log.d(TAG, "Found " + docCount + " documents in " + collectionName);

                    if (docCount == 0) {
                        Log.w(TAG, "No activities found in " + collectionName);
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d(TAG, "Processing document: " + document.getId() + " from " + collectionName);

                        String childName = document.getString("childName");
                        String actionType = document.getString("actionType");
                        Long timestamp = document.getLong("timestamp");

                        Log.d(TAG, "Document data - childName: " + childName +
                                ", actionType: " + actionType +
                                ", timestamp: " + timestamp);

                        if (childName != null && actionType != null && timestamp != null) {
                            addActivityItem(childName, actionType, timestamp);
                        } else {
                            Log.w(TAG, "Skipping document with missing data");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading " + collectionName + ": " + e.getMessage());
                    e.printStackTrace();

                    // Show error toast with actual error message
                    runOnUiThread(() -> {
                        Toast.makeText(this,
                                collectionName + " error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                });
    }

    private void addActivityItem(String childName, String actionType, long timestamp) {
        // Create activity item layout
        LinearLayout activityItem = new LinearLayout(this);
        activityItem.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 48); // 16dp bottom margin
        activityItem.setLayoutParams(params);

        // Format time
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String timeString = timeFormat.format(new Date(timestamp));

        // Time TextView
        TextView timeText = new TextView(this);
        timeText.setText(timeString);
        timeText.setTextColor(0xFF999999);
        timeText.setTextSize(12);
        activityItem.addView(timeText);

        // Divider
        android.view.View divider = new android.view.View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (1 * getResources().getDisplayMetrics().density)
        );
        dividerParams.setMargins(0, 12, 0, 12); // 4dp top and bottom margin
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(0xFFE0E0E0);
        activityItem.addView(divider);

        // Activity description TextView
        TextView descriptionText = new TextView(this);
        String description;

        if ("pickup".equals(actionType)) {
            description = childName + " has been picked up";
        } else if ("dropoff".equals(actionType)) {
            description = childName + " has been dropped off";
        } else if ("qr_scanned".equals(actionType)) {
            description = childName + "'s QR has been scanned";
        } else {
            description = childName + "'s activity recorded";
        }

        descriptionText.setText(description);
        descriptionText.setTextColor(0xFF0D2D4D);
        descriptionText.setTextSize(16);
        activityItem.addView(descriptionText);

        // Add to container at the top
        activityFeedContainer.addView(activityItem, 0);
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
        loadActivityFeed(); // Reload activity feed when returning to the dashboard
    }
}