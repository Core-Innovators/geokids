package com.coreinnovators.geokids;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class TrackLocation extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "TrackLocation";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 200;

    private GoogleMap googleMap;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration driverLocationListener;

    private Marker driverMarker;
    private boolean cameraMovedOnce = false;

    // UI
    private TextView tvDriverName, tvStatus, tvChildName, tvBack;
    private LinearLayout statusContainer;
    private ImageView btnBack;

    // Data passed from parent dashboard
    private String driverId;
    private String driverName;
    private String childName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_location);

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get extras from intent
        driverId   = getIntent().getStringExtra("driverId");
        driverName = getIntent().getStringExtra("driverName");
        childName  = getIntent().getStringExtra("childName");

        // If no driverId passed, try to find it automatically from child data
        if (driverId == null || driverId.isEmpty()) {
            loadDriverIdFromFirestore();
        }

        initViews();
        setupMap();
    }

    private void initViews() {
        tvDriverName  = findViewById(R.id.tv_driver_name);
        tvStatus      = findViewById(R.id.tv_status);
        tvChildName   = findViewById(R.id.tv_child_name);
        statusContainer = findViewById(R.id.status_container);
        btnBack       = findViewById(R.id.btn_back);

        if (driverName != null) tvDriverName.setText("Driver: " + driverName);
        if (childName  != null) tvChildName.setText("Tracking van for " + childName);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Enable my-location layer if permission granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        // Start listening for driver location
        if (driverId != null && !driverId.isEmpty()) {
            startListeningDriverLocation();
        }
    }

    /**
     * Fallback: find the assigned driver from the parent's child document in Firestore.
     */
    private void loadDriverIdFromFirestore() {
        if (auth.getCurrentUser() == null) return;
        String parentId = auth.getCurrentUser().getUid();

        db.collection("children")
                .whereEqualTo("parentId", parentId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) return;
                    QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                    childName = doc.getString("childName");

                    Object assignedDriverObj = doc.get("assignedDriver");
                    if (assignedDriverObj instanceof java.util.Map) {
                        java.util.Map<String, Object> ad = (java.util.Map<String, Object>) assignedDriverObj;
                        driverId   = (String) ad.get("driverId");
                        driverName = (String) ad.get("driverName");
                    }

                    runOnUiThread(() -> {
                        if (tvDriverName != null && driverName != null)
                            tvDriverName.setText("Driver: " + driverName);
                        if (tvChildName != null && childName != null)
                            tvChildName.setText("Tracking van for " + childName);
                    });

                    // Map might already be ready by now
                    if (googleMap != null && driverId != null) {
                        startListeningDriverLocation();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load child data: " + e.getMessage()));
    }

    /**
     * Real-time listener on drivers/{driverId} for latitude/longitude fields.
     */
    private void startListeningDriverLocation() {
        if (driverId == null || driverId.isEmpty()) {
            showOfflineStatus();
            return;
        }

        Log.d(TAG, "Starting real-time location listener for driver: " + driverId);

        driverLocationListener = db.collection("drivers")
                .document(driverId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed: " + error.getMessage());
                        showOfflineStatus();
                        return;
                    }

                    if (snapshot == null || !snapshot.exists()) {
                        showOfflineStatus();
                        return;
                    }

                    Boolean rideActive = snapshot.getBoolean("rideActive");
                    Double  lat        = snapshot.getDouble("latitude");
                    Double  lng        = snapshot.getDouble("longitude");

                    if (Boolean.TRUE.equals(rideActive) && lat != null && lng != null) {
                        showOnlineStatus();
                        updateDriverMarker(new LatLng(lat, lng));
                    } else {
                        showOfflineStatus();
                    }
                });
    }

    private void updateDriverMarker(LatLng position) {
        if (googleMap == null) return;

        if (driverMarker == null) {
            driverMarker = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(driverName != null ? driverName : "School Van")
                    .snippet("Live location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        } else {
            driverMarker.setPosition(position);
        }

        // Move camera to driver on first fix; after that just update marker smoothly
        if (!cameraMovedOnce) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f));
            cameraMovedOnce = true;
        } else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(position));
        }
    }

    private void showOnlineStatus() {
        runOnUiThread(() -> {
            if (tvStatus != null) {
                tvStatus.setText("● LIVE");
                tvStatus.setTextColor(Color.parseColor("#4CAF50"));
            }
        });
    }

    private void showOfflineStatus() {
        runOnUiThread(() -> {
            if (tvStatus != null) {
                tvStatus.setText("● Ride not active");
                tvStatus.setTextColor(Color.parseColor("#F44336"));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (driverLocationListener != null) {
            driverLocationListener.remove();
            driverLocationListener = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Keep listener alive so map updates even if screen briefly off;
        // remove only on destroy.
    }
}
