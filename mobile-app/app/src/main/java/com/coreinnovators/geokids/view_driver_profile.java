package com.coreinnovators.geokids;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class view_driver_profile extends AppCompatActivity {

    private static final String TAG = "DriverProfileActivity";

    private CircleImageView profileImage;
    private TextView driverName;
    private TextView driverDescription;
    private TextView driverAge;
    private TextView vehicleNumber;
    private RecyclerView vehicleImagesRecycler;
    private RecyclerView reviewsRecycler;
    private Button continueButton;
    private TextView noReviewsText;
    private View loadingOverlay;

    private FirebaseFirestore firestore;
    private String driverId;
    private String childId;
    private String parentId;
    private Driver currentDriver;
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_driver_profile);

        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        driverId = intent.getStringExtra("driver_id");
        childId  = intent.getStringExtra("child_id");
        parentId = intent.getStringExtra("parent_id");

        Log.d(TAG, "Driver ID: " + driverId);
        Log.d(TAG, "Child ID: "  + childId);
        Log.d(TAG, "Parent ID: " + parentId);

        initializeViews();
        setupRecyclerViews();
        loadDriverData();
        loadReviews();
        setupListeners();
    }

    private void initializeViews() {
        profileImage          = findViewById(R.id.profile_image);
        driverName            = findViewById(R.id.driver_name);
        driverDescription     = findViewById(R.id.driver_description);
        driverAge             = findViewById(R.id.driver_age);
        vehicleNumber         = findViewById(R.id.vehicle_number);
        vehicleImagesRecycler = findViewById(R.id.vehicle_images_recycler);
        reviewsRecycler       = findViewById(R.id.reviews_recycler);
        continueButton        = findViewById(R.id.continue_button);
        noReviewsText         = findViewById(R.id.no_reviews_text);
        loadingOverlay        = findViewById(R.id.loading_overlay);
    }

    private void setupRecyclerViews() {
        reviewAdapter = new ReviewAdapter();
        reviewsRecycler.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecycler.setAdapter(reviewAdapter);
        reviewsRecycler.setNestedScrollingEnabled(true);

        vehicleImagesRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void loadDriverData() {
        showLoading(true);
        firestore.collection("drivers").document(driverId)
                .get()
                .addOnSuccessListener(doc -> {
                    showLoading(false);
                    if (doc.exists()) {
                        currentDriver = doc.toObject(Driver.class);
                        if (currentDriver != null) {
                            currentDriver.setId(doc.getId());
                            updateUI();
                        }
                    } else {
                        Toast.makeText(this, "Driver not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error loading driver: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to load driver data", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null)
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateUI() {
        driverName.setText(currentDriver.getFullName());
        driverName.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));

        driverDescription.setText("Professional driver with " + calculateExperience() + " years of experience");

        int age = calculateAge(currentDriver.getBirthday());
        driverAge.setText("~" + age + " years old");

        String vehicleNum = currentDriver.getVehicleNumber();
        vehicleNumber.setText(vehicleNum != null && !vehicleNum.isEmpty() ? "~" + vehicleNum : "~N/A");

        String profileUrl = currentDriver.getProfileImageUrl();
        if (profileUrl != null && !profileUrl.isEmpty()) {
            Glide.with(this)
                    .load(profileUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(profileImage);
        }

        loadVehicleImages();
    }

    private void loadVehicleImages() {
        List<String> vehicleImages = currentDriver.getVehicleImageUrls();
        if (vehicleImages == null || vehicleImages.isEmpty()) {
            vehicleImagesRecycler.setVisibility(View.GONE);
        } else {
            vehicleImagesRecycler.setVisibility(View.VISIBLE);
            VehicleImageAdapter imageAdapter = new VehicleImageAdapter(this, vehicleImages);
            vehicleImagesRecycler.setAdapter(imageAdapter);
        }
    }

    private void loadReviews() {
        firestore.collection("reviews")
                .whereEqualTo("driverId", driverId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Review review = doc.toObject(Review.class);
                        review.setReviewId(doc.getId());
                        reviews.add(review);
                    }
                    if (reviews.isEmpty()) {
                        reviewsRecycler.setVisibility(View.GONE);
                        noReviewsText.setVisibility(View.VISIBLE);
                    } else {
                        reviewsRecycler.setVisibility(View.VISIBLE);
                        noReviewsText.setVisibility(View.GONE);
                        reviewAdapter.setReviews(reviews);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading reviews: " + e.getMessage(), e);
                    reviewsRecycler.setVisibility(View.GONE);
                    noReviewsText.setVisibility(View.VISIBLE);
                });
    }

    private void setupListeners() {
        continueButton.setOnClickListener(v ->
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() ->
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100)
                                .withEndAction(this::assignDriverAndNavigate).start()
                ).start()
        );
    }

    private void assignDriverAndNavigate() {
        if (childId == null || childId.isEmpty()) {
            Toast.makeText(this, "Child ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentDriver == null) {
            Toast.makeText(this, "Driver data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        continueButton.setEnabled(false);

        Map<String, Object> assignedDriverMap = new HashMap<>();
        assignedDriverMap.put("driverId",    driverId);
        assignedDriverMap.put("driverName",  currentDriver.getFullName());
        assignedDriverMap.put("status",      "assigned");
        assignedDriverMap.put("assignedAt",  FieldValue.serverTimestamp());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("assignedDriver", assignedDriverMap);
        childUpdates.put("updatedAt", System.currentTimeMillis());

        firestore.collection("children").document(childId)
                .update(childUpdates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "assignedDriver written to child: " + childId);
                    showLoading(false);
                    // ── Navigate directly to ParentPendingDashboard ──────────
                    // Do NOT call updateParentRecord() first — the parents
                    // collection uses auto-generated document IDs (via .add()),
                    // NOT the Auth UID, so .document(parentId) would create a
                    // wrong/empty doc and the update would silently fail, which
                    // previously caused the activity stack to behave unexpectedly.
                    navigateToPendingDashboard();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    continueButton.setEnabled(true);
                    Log.e(TAG, "Error assigning driver: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to assign driver. Please try again.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToPendingDashboard() {
        Toast.makeText(this, "Driver selected! Awaiting confirmation.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, ParentPendingDashboard.class);
        // Clear the entire back stack — user cannot go back to driver selection
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private int calculateAge(String birthday) {
        if (birthday == null || birthday.isEmpty()) return 35;
        try {
            String[] parts = birthday.split("/");
            if (parts.length >= 3) {
                int birthYear  = Integer.parseInt(parts[2]);
                int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                return currentYear - birthYear;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating age: " + e.getMessage());
        }
        return 35;
    }

    private int calculateExperience() {
        return 5;
    }
}