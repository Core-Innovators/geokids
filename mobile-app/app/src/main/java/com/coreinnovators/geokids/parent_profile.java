package com.coreinnovators.geokids;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;

public class parent_profile extends AppCompatActivity {

    private CardView btnAddChild, btnViewQr, btnChooseDriver, btnUpdateLocation,
            btnUpdateChild, btnInformAbsence, btnContactAdmin, btnDeleteAccount;

    private LinearLayout navHome, navLocation, navQr, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_profile);

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupMenuClickListeners();
        setupBottomNavigationListeners();
    }

    private void initializeViews() {
        // Menu buttons
        btnAddChild = findViewById(R.id.btn_add_child);
        btnViewQr = findViewById(R.id.btn_view_qr);
        btnChooseDriver = findViewById(R.id.btn_choose_driver);
        btnUpdateLocation = findViewById(R.id.btn_update_location);
        btnUpdateChild = findViewById(R.id.btn_update_child);
        btnInformAbsence = findViewById(R.id.btn_inform_absence);
        btnContactAdmin = findViewById(R.id.btn_contact_admin);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);

        // Bottom navigation
        navHome = findViewById(R.id.nav_home);
        navLocation = findViewById(R.id.nav_location);
        navQr = findViewById(R.id.nav_qr);
        navProfile = findViewById(R.id.nav_profile);
    }

    private void setupMenuClickListeners() {
        // Add New Child Profile
        btnAddChild.setOnClickListener(v -> {
            Intent intent = new Intent(parent_profile.this, AddChild.class);
            startActivity(intent);
        });

        // View QR Code - Navigate to separate screen
        btnViewQr.setOnClickListener(v -> {
            Intent intent = new Intent(parent_profile.this, QR_Code.class);
            startActivity(intent);
        });

        // Choose New Driver
        btnChooseDriver.setOnClickListener(v -> {
            Intent intent = new Intent(parent_profile.this, SelectDriverActivity.class);
            startActivity(intent);
        });

        // Update Pickup Location
        btnUpdateLocation.setOnClickListener(v -> {
            Intent intent = new Intent(parent_profile.this, UpdateLocationChild.class);
            startActivity(intent);
        });

        // Update Child Details
        btnUpdateChild.setOnClickListener(v -> {
            Intent intent = new Intent(parent_profile.this, UpdateChildDetails.class);
            startActivity(intent);
        });

        // Inform Child Absence
        btnInformAbsence.setOnClickListener(v -> {
            Intent intent = new Intent(parent_profile.this, InformAbsence.class);
            startActivity(intent);
        });

        // Contact Admin/Inquiries - Make phone call
        btnContactAdmin.setOnClickListener(v -> {
            showContactAdminDialog();
        });

        // Delete Account - Show confirmation dialog
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void setupBottomNavigationListeners() {
        // Home navigation
        navHome.setOnClickListener(v -> {
            // Navigate to home or do nothing if already on home
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        });

        // Location navigation
        navLocation.setOnClickListener(v -> {
            Intent intent = new Intent(parent_profile.this, ChildHistory.class);
            startActivity(intent);
        });

        // History navigation
        navQr.setOnClickListener(v -> {
            Intent intent = new Intent(parent_profile.this, ChildHistory.class);
            startActivity(intent);
        });

        // Profile navigation - Already on profile
        navProfile.setOnClickListener(v -> {
            // Already on profile page
            Toast.makeText(this, "Already on Profile", Toast.LENGTH_SHORT).show();
        });
    }

    private void showContactAdminDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Contact Admin")
                .setMessage("Would you like to call admin support?")
                .setPositiveButton("Call", (dialog, which) -> {
                    makePhoneCall();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_menu_call)
                .show();
    }

    private void makePhoneCall() {
        String adminPhoneNumber = "+94771234567"; // Replace with your actual admin phone number

        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(android.net.Uri.parse("tel:" + adminPhoneNumber));

            Toast.makeText(this, "Calling Admin Support...", Toast.LENGTH_SHORT).show();
            startActivity(callIntent);

        } catch (Exception e) {
            Toast.makeText(this, "Unable to make call. Please try again.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Handle account deletion
                    performAccountDeletion();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performAccountDeletion() {
        // TODO: Implement actual account deletion logic
        Toast.makeText(this, "Account deletion initiated", Toast.LENGTH_SHORT).show();

        // Example: Navigate to login screen after deletion
        // Intent intent = new Intent(parent_profile.this, LoginActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // startActivity(intent);
        // finish();
    }
}