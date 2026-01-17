package com.coreinnovators.geokids;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    // UI Components
    private CircleImageView profileImage;
    private TextView driverName;
    private TextView driverDescription;
    private TextView driverAge;
    private TextView vehicleNumber;
    private RecyclerView vehicleImagesRecycler;
    private RecyclerView reviewsRecycler;
    private Button continueButton;
    private TextView noReviewsText;

    // Data
    private FirebaseFirestore firestore;
    private String driverId;
    private String childId;
    private String parentId;
    private Driver currentDriver;
    private ReviewAdapter reviewAdapter;
    private List<String> vehicleImageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_driver_profile);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();

        // Get intent data
        Intent intent = getIntent();
        driverId = intent.getStringExtra("driver_id");
        childId = intent.getStringExtra("child_id");
        parentId = intent.getStringExtra("parent_id");

        Log.d(TAG, "Driver ID: " + driverId);
        Log.d(TAG, "Child ID: " + childId);
        Log.d(TAG, "Parent ID: " + parentId);

        // Initialize views
        initializeViews();

        // Setup RecyclerViews
        setupRecyclerViews();

        // Load driver data
        loadDriverData();

        // Load reviews
        loadReviews();

        // Setup listeners
        setupListeners();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profile_image);
        driverName = findViewById(R.id.driver_name);
        driverDescription = findViewById(R.id.driver_description);
        driverAge = findViewById(R.id.driver_age);
        vehicleNumber = findViewById(R.id.vehicle_number);
        vehicleImagesRecycler = findViewById(R.id.vehicle_images_recycler);
        reviewsRecycler = findViewById(R.id.reviews_recycler);
        continueButton = findViewById(R.id.continue_button);
        noReviewsText = findViewById(R.id.no_reviews_text);
    }

    private void setupRecyclerViews() {
        // Reviews RecyclerView
        reviewAdapter = new ReviewAdapter();
        reviewsRecycler.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecycler.setAdapter(reviewAdapter);
        reviewsRecycler.setNestedScrollingEnabled(true);

        // Vehicle Images RecyclerView (horizontal)
        vehicleImagesRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        vehicleImageUrls = new ArrayList<>();
    }

    private void loadDriverData() {
        firestore.collection("drivers")
                .document(driverId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentDriver = documentSnapshot.toObject(Driver.class);
                        if (currentDriver != null) {
                            currentDriver.setId(documentSnapshot.getId());
                            updateUI();
                        }
                    } else {
                        Toast.makeText(this, "Driver not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading driver: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to load driver data", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateUI() {
        // Set driver name
        driverName.setText(currentDriver.getFullName());

        // Set description
        driverDescription.setText("Professional driver with " +
                calculateExperience() + " years of experience");

        // Set age
        int age = calculateAge(currentDriver.getBirthday());
        driverAge.setText("~" + age + " years old");

        // Set vehicle number
        String vehicleNum = currentDriver.getVehicleNumber();
        vehicleNumber.setText(vehicleNum != null ? "~" + vehicleNum : "~N/A");

        // Load profile image
        if (currentDriver.getProfileImageUrl() != null &&
                !currentDriver.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentDriver.getProfileImageUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(profileImage);
        }

        // Load vehicle images
        loadVehicleImages();
    }

    private void loadVehicleImages() {
        // Collect all vehicle image URLs
        vehicleImageUrls.clear();

        if (currentDriver.getFrontLicenseUrl() != null &&
                !currentDriver.getFrontLicenseUrl().isEmpty()) {
            vehicleImageUrls.add(currentDriver.getFrontLicenseUrl());
        }
        if (currentDriver.getBackLicenseUrl() != null &&
                !currentDriver.getBackLicenseUrl().isEmpty()) {
            vehicleImageUrls.add(currentDriver.getBackLicenseUrl());
        }

        // Set adapter
        VehicleImageAdapter imageAdapter = new VehicleImageAdapter(this, vehicleImageUrls);
        vehicleImagesRecycler.setAdapter(imageAdapter);
    }

    private void loadReviews() {
        firestore.collection("reviews")
                .whereEqualTo("driverId", driverId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Review review = document.toObject(Review.class);
                        review.setReviewId(document.getId());
                        reviews.add(review);
                    }

                    if (reviews.isEmpty()) {
                        // Show "no reviews" message
                        reviewsRecycler.setVisibility(View.GONE);
                        noReviewsText.setVisibility(View.VISIBLE);
                    } else {
                        // Show reviews
                        reviewsRecycler.setVisibility(View.VISIBLE);
                        noReviewsText.setVisibility(View.GONE);
                        reviewAdapter.setReviews(reviews);
                    }

                    Log.d(TAG, "Loaded " + reviews.size() + " reviews");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading reviews: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to load reviews", Toast.LENGTH_SHORT).show();
                    // Show no reviews on error
                    reviewsRecycler.setVisibility(View.GONE);
                    noReviewsText.setVisibility(View.VISIBLE);
                });
    }

    private void setupListeners() {
        continueButton.setOnClickListener(v -> assignDriverToChild());
    }

    private void assignDriverToChild() {
        if (childId == null || childId.isEmpty()) {
            Toast.makeText(this, "Child ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create assignment data
        Map<String, Object> assignmentData = new HashMap<>();
        assignmentData.put("driverId", driverId);
        assignmentData.put("driverName", currentDriver.getFullName());
        assignmentData.put("assignedAt", FieldValue.serverTimestamp());
        assignmentData.put("status", "assigned");

        // Update child document with driver assignment
        firestore.collection("children")
                .document(childId)
                .update("assignedDriver", assignmentData)
                .addOnSuccessListener(aVoid -> {
                    // Also update parent's document to track the assignment
                    updateParentRecord();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error assigning driver: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to assign driver", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateParentRecord() {
        if (parentId == null || parentId.isEmpty()) {
            // If no parent ID, just show success and finish
            showSuccessAndNavigate();
            return;
        }

        // Create assignment record in parent's document
        Map<String, Object> assignmentInfo = new HashMap<>();
        assignmentInfo.put("childId", childId);
        assignmentInfo.put("driverId", driverId);
        assignmentInfo.put("driverName", currentDriver.getFullName());
        assignmentInfo.put("assignedAt", FieldValue.serverTimestamp());

        firestore.collection("parents")
                .document(parentId)
                .update("driverAssignments", FieldValue.arrayUnion(assignmentInfo))
                .addOnSuccessListener(aVoid -> {
                    showSuccessAndNavigate();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating parent record: " + e.getMessage(), e);
                    // Still show success since child was updated
                    showSuccessAndNavigate();
                });
    }

    private void showSuccessAndNavigate() {
        Toast.makeText(this, "Driver assigned successfully!", Toast.LENGTH_LONG).show();

        // Navigate back to parent dashboard
        Intent intent = new Intent(this, ParentActiveDashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private int calculateAge(String birthday) {
        if (birthday == null || birthday.isEmpty()) {
            return 35; // Default age
        }

        try {
            // Assuming birthday format is "7/1/2026" or similar
            String[] parts = birthday.split("/");
            if (parts.length >= 3) {
                int birthYear = Integer.parseInt(parts[2]);
                int currentYear = 2026;
                return currentYear - birthYear;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating age: " + e.getMessage());
        }
        return 35; // Default age
    }

    private int calculateExperience() {
        // You can add experience field to Driver model
        // For now, returning a default value
        return 5;
    }
}