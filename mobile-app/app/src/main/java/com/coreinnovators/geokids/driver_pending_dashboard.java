package com.coreinnovators.geokids;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class driver_pending_dashboard extends AppCompatActivity {

    private static final String TAG = "PendingDashboard";
    private static final String COLLECTION_NAME = "drivers";

    private TextView helloText;
    private Button contactAdminButton;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String driverId;
    private String driverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_pending_dashboard);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get driver data from intent
        driverId = getIntent().getStringExtra("driver_id");
        driverName = getIntent().getStringExtra("driver_name");

        initializeViews();
        loadDriverData();
        setupListeners();
    }

    private void initializeViews() {
        helloText = findViewById(R.id.hello);
        contactAdminButton = findViewById(R.id.contact_admin);

        // Set driver name if available
        if (driverName != null && !driverName.isEmpty()) {
            helloText.setText("Hello, " + driverName);
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

                        if (name != null && !name.isEmpty()) {
                            helloText.setText("Hello, " + name);
                        }

                        Log.d(TAG, "Driver status: " + status);

                        // Optional: Check if status changed to "approved"
                        if ("approved".equals(status)) {
                            // Navigate to main driver screen
                            navigateToDriverHome();
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
        contactAdminButton.setOnClickListener(v -> {
            contactSupport();
        });
    }

    private void contactSupport() {
        // Option 1: Open email client
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:support@geokids.com")); // Replace with your support email
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Driver Application Support - " + driverId);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello,\n\nI need help with my driver application.\n\nDriver ID: " + driverId + "\n\nIssue: ");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email app installed", Toast.LENGTH_SHORT).show();
        }

        // Option 2: Open phone dialer (uncomment if you want this instead)
        /*
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:+1234567890")); // Replace with your support number
        startActivity(dialIntent);
        */

        // Option 3: Open WhatsApp (uncomment if you want this instead)
        /*
        try {
            String phoneNumber = "+1234567890"; // Replace with your WhatsApp number
            String message = "Hello, I need help with my driver application. Driver ID: " + driverId;

            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
            whatsappIntent.setData(Uri.parse("https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message)));
            startActivity(whatsappIntent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
        */
    }

    private void navigateToDriverHome() {
        // Navigate to the main driver dashboard if approved
        // Intent intent = new Intent(this, DriverHomeActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // startActivity(intent);
        // finish();

        Toast.makeText(this, "Your application has been approved!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        // Disable back button to prevent going back to registration
        super.onBackPressed();
        Toast.makeText(this, "Please wait for approval", Toast.LENGTH_SHORT).show();
        // Or you can allow logout:
        // showLogoutDialog();
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