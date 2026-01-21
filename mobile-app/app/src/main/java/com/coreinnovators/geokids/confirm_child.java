package com.coreinnovators.geokids;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class confirm_child extends AppCompatActivity {

    private static final String TAG = "ConfirmChildActivity";

    private ImageView childProfileImage, closeButton;
    private TextView childNameTv, childGradeTv, childSchoolTv;
    private Button confirmPickupBtn, confirmDropOffBtn, contactParentBtn, contactAdminBtn;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String childId;
    private String childName;
    private String childAge;
    private String childGrade;
    private String childSchool;
    private String childProfileImageUrl;
    private String parentName;
    private String parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_child);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();

        // Get data from intent
        getDataFromIntent();

        // Display child data
        displayChildData();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        childProfileImage = findViewById(R.id.child_profile_image);
        closeButton = findViewById(R.id.close_button);
        childNameTv = findViewById(R.id.child_name);
        childGradeTv = findViewById(R.id.child_grade);
        childSchoolTv = findViewById(R.id.child_school);
        confirmPickupBtn = findViewById(R.id.confirm_pickup_btn);
        confirmDropOffBtn = findViewById(R.id.confirm_dropoff_btn);
        contactParentBtn = findViewById(R.id.contact_parent_btn);
        contactAdminBtn = findViewById(R.id.contact_admin_btn);
    }

    private void getDataFromIntent() {
        childId = getIntent().getStringExtra("childId");
        childName = getIntent().getStringExtra("childName");
        childAge = getIntent().getStringExtra("childAge");
        childGrade = getIntent().getStringExtra("childGrade");
        childSchool = getIntent().getStringExtra("childSchool");
        childProfileImageUrl = getIntent().getStringExtra("childProfileImageUrl");
        parentName = getIntent().getStringExtra("parentName");
        parentId = getIntent().getStringExtra("parentId");
    }

    private void displayChildData() {
        // Set child name
        childNameTv.setText("Name : = " + (childName != null ? childName : "N/A"));

        // Set child grade
        childGradeTv.setText("Grade : = " + (childGrade != null ? childGrade : "N/A"));

        // Set child school
        childSchoolTv.setText("School : = " + (childSchool != null ? childSchool : "N/A"));

        // Load profile image
        if (childProfileImageUrl != null && !childProfileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(childProfileImageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(childProfileImage);
        }
    }

    private void setupClickListeners() {
        closeButton.setOnClickListener(v -> finish());

        confirmPickupBtn.setOnClickListener(v -> {
            confirmAction("pickup");
        });

        confirmDropOffBtn.setOnClickListener(v -> {
            confirmAction("dropoff");
        });

        contactParentBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Contact Parent: " + parentName, Toast.LENGTH_SHORT).show();
            // TODO: Implement contact parent functionality
        });

        contactAdminBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Contact Admin", Toast.LENGTH_SHORT).show();
            // TODO: Implement contact admin functionality
        });
    }

    private void confirmAction(String actionType) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable buttons to prevent multiple clicks
        confirmPickupBtn.setEnabled(false);
        confirmDropOffBtn.setEnabled(false);

        String driverId = auth.getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();

        // First, create a "QR scanned" activity
        createQRScannedActivity(driverId, timestamp);

        // Get driver name from Firestore
        db.collection("drivers").document(driverId)
                .get()
                .addOnSuccessListener(driverDoc -> {
                    String driverName = driverDoc.getString("fullName");
                    if (driverName == null || driverName.isEmpty()) {
                        driverName = driverDoc.getString("name");
                    }
                    if (driverName == null || driverName.isEmpty()) {
                        driverName = "Driver";
                    }

                    // Create pickup/dropoff record
                    Map<String, Object> actionData = new HashMap<>();
                    actionData.put("childId", childId);
                    actionData.put("childName", childName);
                    actionData.put("childGrade", childGrade);
                    actionData.put("childSchool", childSchool);
                    actionData.put("driverId", driverId);
                    actionData.put("driverName", driverName);
                    actionData.put("parentId", parentId);
                    actionData.put("parentName", parentName);
                    actionData.put("actionType", actionType);
                    actionData.put("status", "completed");
                    actionData.put("timestamp", timestamp);
                    actionData.put("createdAt", timestamp);

                    // Save to appropriate collection
                    String collectionName = actionType.equals("pickup") ? "pickups" : "dropoffs";

                    db.collection(collectionName)
                            .add(actionData)
                            .addOnSuccessListener(documentReference -> {
                                Log.d(TAG, actionType + " confirmed with ID: " + documentReference.getId());

                                // Update child document with latest action
                                Map<String, Object> childUpdate = new HashMap<>();
                                childUpdate.put("lastAction", actionType);
                                childUpdate.put("lastActionTimestamp", timestamp);
                                childUpdate.put("lastActionBy", driverId);

                                db.collection("children").document(childId)
                                        .update(childUpdate)
                                        .addOnSuccessListener(aVoid -> {
                                            String message = actionType.equals("pickup")
                                                    ? "Pickup confirmed successfully!"
                                                    : "Drop-off confirmed successfully!";
                                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error updating child document: " + e.getMessage());
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error confirming " + actionType + ": " + e.getMessage());
                                Toast.makeText(this, "Failed to confirm " + actionType,
                                        Toast.LENGTH_SHORT).show();
                                confirmPickupBtn.setEnabled(true);
                                confirmDropOffBtn.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading driver data: " + e.getMessage());
                    Toast.makeText(this, "Error loading driver data", Toast.LENGTH_SHORT).show();
                    confirmPickupBtn.setEnabled(true);
                    confirmDropOffBtn.setEnabled(true);
                });
    }

    private void createQRScannedActivity(String driverId, long timestamp) {
        // Create QR scanned activity entry
        Map<String, Object> qrActivity = new HashMap<>();
        qrActivity.put("childId", childId);
        qrActivity.put("childName", childName);
        qrActivity.put("driverId", driverId);
        qrActivity.put("actionType", "qr_scanned");
        qrActivity.put("timestamp", timestamp);
        qrActivity.put("createdAt", timestamp);

        db.collection("activities")
                .add(qrActivity)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "QR scanned activity created: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating QR scanned activity: " + e.getMessage());
                });
    }
}