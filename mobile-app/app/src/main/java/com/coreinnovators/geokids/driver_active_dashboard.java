package com.coreinnovators.geokids;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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
    private String activeTripId = null;   // track current trip doc ID

    // ─── GPS Tracking ────────────────────────────────────────────────
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isTrackingLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_active_dashboard);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupLocationCallback();

        initializeViews();
        loadDriverName();
        loadRideStatus();
        loadActivityFeed();
        setupClickListeners();
    }

    // ─────────────────────────────────────────────────────────────────
    //  GPS TRACKING
    // ─────────────────────────────────────────────────────────────────

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    writeLocationToFirestore(location.getLatitude(), location.getLongitude());
                }
            }
        };
    }

    private void startLocationTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                 Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000L)  // 5-second interval
                .setMinUpdateIntervalMillis(3000L)
                .build();

        fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper());

        isTrackingLocation = true;
        Log.d(TAG, "GPS tracking STARTED");
    }

    private void stopLocationTracking() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        isTrackingLocation = false;
        clearLocationFromFirestore();
        Log.d(TAG, "GPS tracking STOPPED");
    }

    private void writeLocationToFirestore(double latitude, double longitude) {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude",        latitude);
        locationData.put("longitude",       longitude);
        locationData.put("locationUpdatedAt", System.currentTimeMillis());

        db.collection("drivers").document(uid)
                .set(locationData, SetOptions.merge())
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to update location: " + e.getMessage()));

        Log.d(TAG, "Location pushed → lat:" + latitude + " lng:" + longitude);
    }

    private void clearLocationFromFirestore() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> clear = new HashMap<>();
        clear.put("latitude",  null);
        clear.put("longitude", null);

        db.collection("drivers").document(uid)
                .set(clear, SetOptions.merge())
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to clear location: " + e.getMessage()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted — if ride is active, start tracking now
                if (isRideActive) {
                    startLocationTracking();
                }
            } else {
                Toast.makeText(this,
                        "Location permission is required for GPS tracking",
                        Toast.LENGTH_LONG).show();
                toggleButton.setChecked(false);
            }
        }
    }

    private void initializeViews() {
        driverNameTv         = findViewById(R.id.driver_name);
        toggleButton         = findViewById(R.id.toggleButton);
        notificationBell     = findViewById(R.id.notification_bell);
        activityFeedContainer = findViewById(R.id.activity_feed);

        availablePickupsCard = findViewById(R.id.available_pickups_card);
        viewRequestsCard     = findViewById(R.id.view_requests_card);
        contactSupportCard   = findViewById(R.id.contact_support_card);

        navHome     = findViewById(R.id.nav_home);
        navLocation = findViewById(R.id.nav_location);
        navQr       = findViewById(R.id.nav_qr);
        navProfile  = findViewById(R.id.nav_profile);

        toggleButton.setTextOn("Active");
        toggleButton.setTextOff("Inactive");
    }

    // ─────────────────────────────────────────────────────────────────
    //  DRIVER NAME
    // ─────────────────────────────────────────────────────────────────

    private void loadDriverName() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("drivers").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.getString("fullName");
                        if (name == null || name.isEmpty()) name = snapshot.getString("name");
                        if (name == null || name.isEmpty()) name = snapshot.getString("driverName");
                        driverNameTv.setText((name != null && !name.isEmpty()) ? name : "Driver");
                    } else {
                        driverNameTv.setText("Driver");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load driver data: " + e.getMessage());
                    driverNameTv.setText("Driver");
                });
    }

    // ─────────────────────────────────────────────────────────────────
    //  RIDE STATUS
    // ─────────────────────────────────────────────────────────────────

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

                        // Also find the active trip ID if ride is active
                        if (isRideActive) {
                            findActiveTripId(uid);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error loading ride status: " + e.getMessage()));
    }

    private void findActiveTripId(String driverId) {
        db.collection("trips")
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("status", "active")
                .limit(1)
                .get()
                .addOnSuccessListener(q -> {
                    if (!q.isEmpty()) {
                        activeTripId = q.getDocuments().get(0).getId();
                        Log.d(TAG, "Resumed active trip: " + activeTripId);
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────
    //  TOGGLE  →  START / END TRIP
    // ─────────────────────────────────────────────────────────────────

    private void updateRideStatus(boolean isActive) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isActive) {
            startTrip();
        } else {
            endTrip();
        }
    }

    /**
     * Creates a new trip document in Firestore and saves assigned children as pending.
     */
    private void startTrip() {
        String uid = auth.getCurrentUser().getUid();

        // Load assigned children first
        db.collection("drivers").document(uid)
                .get()
                .addOnSuccessListener(driverDoc -> {
                    List<String> assignedChildren = new ArrayList<>();
                    if (driverDoc.exists()) {
                        List<String> stored = (List<String>) driverDoc.get("assignedChildren");
                        if (stored != null) assignedChildren.addAll(stored);
                    }

                    // Build trip document
                    String tripId = UUID.randomUUID().toString();
                    Map<String, Object> tripData = new HashMap<>();
                    tripData.put("tripId",           tripId);
                    tripData.put("driverId",         uid);
                    tripData.put("status",           "active");
                    tripData.put("startTime",        System.currentTimeMillis());
                    tripData.put("endTime",          null);
                    tripData.put("pendingChildren",  assignedChildren);   // all assigned start as pending
                    tripData.put("pickedUpChildren", new ArrayList<>());  // cleared when QR scanned
                    tripData.put("createdAt",        System.currentTimeMillis());

                    db.collection("trips").document(tripId)
                            .set(tripData)
                            .addOnSuccessListener(aVoid -> {
                                activeTripId = tripId;
                                isRideActive = true;
                                updateDriverRideFlag(true);
                                // ✅ Start GPS tracking when ride starts
                                startLocationTracking();
                                Toast.makeText(this, "Ride started! GPS tracking ON", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Trip created: " + tripId);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to create trip: " + e.getMessage());
                                Toast.makeText(this,
                                        "Failed to start trip", Toast.LENGTH_SHORT).show();
                                toggleButton.setChecked(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load driver before starting trip: " + e.getMessage());
                    Toast.makeText(this,
                            "Failed to start trip", Toast.LENGTH_SHORT).show();
                    toggleButton.setChecked(false);
                });
    }

    /**
     * Closes the active trip document and sets endTime.
     */
    private void endTrip() {
        if (activeTripId == null) {
            // Try to find it
            String uid = auth.getCurrentUser().getUid();
            db.collection("trips")
                    .whereEqualTo("driverId", uid)
                    .whereEqualTo("status", "active")
                    .limit(1)
                    .get()
                    .addOnSuccessListener(q -> {
                        if (!q.isEmpty()) {
                            activeTripId = q.getDocuments().get(0).getId();
                            doEndTrip();
                        } else {
                            // No active trip found, just update driver flag
                            updateDriverRideFlag(false);
                            isRideActive = false;
                            Toast.makeText(this, "Ride ended", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            doEndTrip();
        }
    }

    private void doEndTrip() {
        Map<String, Object> update = new HashMap<>();
        update.put("status",  "completed");
        update.put("endTime", System.currentTimeMillis());

        db.collection("trips").document(activeTripId)
                .update(update)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Trip ended: " + activeTripId);
                    activeTripId = null;
                    isRideActive = false;
                    // ✅ Stop GPS tracking when ride ends
                    stopLocationTracking();
                    updateDriverRideFlag(false);
                    Toast.makeText(this, "Ride ended", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to end trip: " + e.getMessage());
                    Toast.makeText(this,
                            "Failed to end trip", Toast.LENGTH_SHORT).show();
                    toggleButton.setChecked(true); // revert UI
                });
    }

    private void updateDriverRideFlag(boolean isActive) {
        String uid = auth.getCurrentUser().getUid();
        Map<String, Object> rideStatus = new HashMap<>();
        rideStatus.put("rideActive",   isActive);
        rideStatus.put("lastUpdated",  System.currentTimeMillis());

        db.collection("drivers").document(uid)
                .set(rideStatus, SetOptions.merge())
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error updating ride flag: " + e.getMessage()));
    }

    // ─────────────────────────────────────────────────────────────────
    //  ACTIVITY FEED
    // ─────────────────────────────────────────────────────────────────

    private void loadActivityFeed() {
        if (auth.getCurrentUser() == null) return;
        String driverId = auth.getCurrentUser().getUid();

        activityFeedContainer.removeAllViews();
        loadActivitiesFromCollection("pickups",    driverId);
        loadActivitiesFromCollection("dropoffs",   driverId);
        loadActivitiesFromCollection("activities", driverId);
    }

    private void loadActivitiesFromCollection(String collectionName, String driverId) {
        db.collection(collectionName)
                .whereEqualTo("driverId", driverId)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String childName  = document.getString("childName");
                        String actionType = document.getString("actionType");
                        Long   timestamp  = document.getLong("timestamp");

                        if (childName != null && actionType != null && timestamp != null) {
                            addActivityItem(childName, actionType, timestamp);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error loading " + collectionName + ": " + e.getMessage()));
    }

    private void addActivityItem(String childName, String actionType, long timestamp) {
        LinearLayout activityItem = new LinearLayout(this);
        activityItem.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 48);
        activityItem.setLayoutParams(params);

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String timeString = timeFormat.format(new Date(timestamp));

        TextView timeText = new TextView(this);
        timeText.setText(timeString);
        timeText.setTextColor(0xFF999999);
        timeText.setTextSize(12);
        activityItem.addView(timeText);

        android.view.View divider = new android.view.View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int)(1 * getResources().getDisplayMetrics().density));
        dividerParams.setMargins(0, 12, 0, 12);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(0xFFE0E0E0);
        activityItem.addView(divider);

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

        activityFeedContainer.addView(activityItem, 0);
    }

    // ─────────────────────────────────────────────────────────────────
    //  CLICK LISTENERS
    // ─────────────────────────────────────────────────────────────────

    private void setupClickListeners() {
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateRideStatus(isChecked));

        availablePickupsCard.setOnClickListener(v ->
                startActivity(new Intent(driver_active_dashboard.this,
                        available_pickup.class)));

        viewRequestsCard.setOnClickListener(v ->
                startActivity(new Intent(driver_active_dashboard.this,
                        view_request.class)));

        contactSupportCard.setOnClickListener(v ->
                Toast.makeText(this,
                        "Contact Support: +94 XX XXX XXXX", Toast.LENGTH_LONG).show());

        notificationBell.setOnClickListener(v ->
                Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show());

        navHome.setOnClickListener(v ->
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show());

        navLocation.setOnClickListener(v ->
                startActivity(new Intent(driver_active_dashboard.this, available_pickup.class)));

        navQr.setOnClickListener(v ->
                startActivity(new Intent(driver_active_dashboard.this, QR_scan.class)));

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(driver_active_dashboard.this,
                        driver_profile.class)));
    }

    // ─────────────────────────────────────────────────────────────────
    //  LIFECYCLE
    // ─────────────────────────────────────────────────────────────────

    @Override
    protected void onResume() {
        super.onResume();
        loadRideStatus();
        loadActivityFeed();
        // Restart GPS if ride was already active
        if (isRideActive && !isTrackingLocation) {
            startLocationTracking();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Only stop UI updates, not Firestore location (ride may still be active)
        if (fusedLocationClient != null && locationCallback != null && !isRideActive) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}