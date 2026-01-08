package com.coreinnovators.geokids;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class driver_rejection_screen extends AppCompatActivity {

    private static final String TAG = "RejectionScreen";
    private static final String COLLECTION_NAME = "drivers";

    private TextView helloText;
    private TextView rejectionReason;
    private Button reapplyButton;
    private Button contactAdminButton;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String driverId;
    private String driverName;
    private String reason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_rejection_screen);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get driver data from intent
        driverId = getIntent().getStringExtra("driver_id");
        driverName = getIntent().getStringExtra("driver_name");
        reason = getIntent().getStringExtra("rejection_reason");

        initializeViews();
        loadDriverData();
        setupListeners();
        setupBackPressHandler();
    }

    private void setupBackPressHandler() {
        // Modern way to handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showLogoutDialog();
            }
        });
    }

    private void initializeViews() {
        helloText = findViewById(R.id.hello);
        rejectionReason = findViewById(R.id.rejection_reason);
        reapplyButton = findViewById(R.id.reapplybutton);
        contactAdminButton = findViewById(R.id.contact_admin);

        // Set driver name if available
        if (driverName != null && !driverName.isEmpty()) {
            helloText.setText("Hello, " + driverName);
        }

        // Set rejection reason if available
        if (reason != null && !reason.isEmpty()) {
            rejectionReason.setText("Reason: " + reason);
        } else {
            rejectionReason.setText("Reason: Please contact support for more details.");
        }
    }

    private void loadDriverData() {
        if (driverId == null) {
            // If driver ID not passed, try to get from current user
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                driverId = currentUser.getUid();
            } else {
                Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Load driver data from Firestore
        firestore.collection(COLLECTION_NAME)
                .document(driverId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String status = documentSnapshot.getString("status");
                        String rejectionReason = documentSnapshot.getString("rejection_reason");

                        if (name != null && !name.isEmpty()) {
                            helloText.setText("Hello, " + name);
                        }

                        if (rejectionReason != null && !rejectionReason.isEmpty()) {
                            this.rejectionReason.setText("Reason: " + rejectionReason);
                        }

                        Log.d(TAG, "Driver status: " + status);

                        // Check if status changed
                        if ("approved".equals(status)) {
                            // Navigate to main driver screen
                            navigateToDriverHome();
                        } else if ("pending".equals(status)) {
                            // Navigate back to pending screen
                            navigateToPendingScreen();
                        }
                    } else {
                        Log.e(TAG, "Driver document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading driver data: " + e.getMessage());
                    Toast.makeText(this, "Failed to load driver data", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupListeners() {
        reapplyButton.setOnClickListener(v -> {
            showReapplyDialog();
        });

        contactAdminButton.setOnClickListener(v -> {
            contactSupport();
        });
    }

    private void showReapplyDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Reapply")
                .setMessage("Are you sure you want to submit a new application?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    reapplyForDriver();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void reapplyForDriver() {
        if (driverId == null) {
            Toast.makeText(this, "Error: Driver ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update status to pending in Firestore
        firestore.collection(COLLECTION_NAME)
                .document(driverId)
                .update("status", "pending", "rejection_reason", "")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Application resubmitted successfully", Toast.LENGTH_SHORT).show();
                    navigateToPendingScreen();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error reapplying: " + e.getMessage());
                    Toast.makeText(this, "Failed to reapply. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void contactSupport() {
        // Open email client
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:support@geokids.com")); // Replace with your support email
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Driver Application Rejection - " + driverId);
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                "Hello,\n\nI would like to inquire about my rejected driver application.\n\n" +
                        "Driver ID: " + driverId +
                        "\nRejection Reason: " + (reason != null ? reason : "Not specified") +
                        "\n\nAdditional Information: ");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email app installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToDriverHome() {
        Toast.makeText(this, "Your application has been approved!", Toast.LENGTH_LONG).show();
        // Uncomment and implement navigation to driver home
        // Intent intent = new Intent(this, DriverHomeActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // startActivity(intent);
        // finish();
    }

    private void navigateToPendingScreen() {
        Intent intent = new Intent(this, driver_pending_dashboard.class);
        intent.putExtra("driver_id", driverId);
        intent.putExtra("driver_name", driverName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Show logout dialog instead of going back
        showLogoutDialog();
        // Don't call super.onBackPressed() to prevent default back behavior
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Do you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    auth.signOut();
                    // Navigate to login screen
                    // Intent intent = new Intent(this, LoginActivity.class);
                    // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    // startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}