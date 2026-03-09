package com.coreinnovators.geokids;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class driver_profile extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // Views
    private TextView tvGreeting;
    private CardView btnViewChildren;
    private CardView btnUpdateProfile;
    private CardView btnContactParent;
    private CardView btnViewRequests;
    private CardView btnContactAdmin;
    private CardView btnDeleteAccount;
    private CardView btnLogout;

    // Bottom Navigation
    private View navHome;
    private View navLocation;
    private View navQr;
    private View navProfile;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentDriverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deriver_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentDriverId = currentUser.getUid();
        }

        initViews();
        setupClickListeners();
        setupBottomNavigation();
        loadDriverName();
    }

    private void initViews() {
        // Main buttons
        tvGreeting = findViewById(R.id.tvGreeting);
        btnViewChildren = findViewById(R.id.btnViewChildren);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnContactParent = findViewById(R.id.btnContactParent);
        btnViewRequests = findViewById(R.id.btnViewRequests);
        btnContactAdmin = findViewById(R.id.btnContactAdmin);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnLogout = findViewById(R.id.btnLogout);

        // Bottom navigation
        navHome = findViewById(R.id.nav_home);
        navLocation = findViewById(R.id.nav_location);
        navQr = findViewById(R.id.nav_qr);
        navProfile = findViewById(R.id.nav_profile);
    }

    private void loadDriverName() {
        if (currentDriverId == null) {
            tvGreeting.setText("Hello Driver");
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the driver's data in Firebase
        DatabaseReference driverRef = mDatabase.child("drivers").child(currentDriverId);

        driverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get fullName from Firebase
                    String fullName = snapshot.child("fullName").getValue(String.class);

                    if (fullName != null && !fullName.isEmpty()) {
                        tvGreeting.setText("Hello " + fullName);
                        Log.d(TAG, "Driver name loaded: " + fullName);
                    } else {
                        tvGreeting.setText("Hello Driver");
                        Log.w(TAG, "fullName field is empty");
                    }
                } else {
                    tvGreeting.setText("Hello Driver");
                    Log.w(TAG, "Driver data not found in Firebase");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvGreeting.setText("Hello Driver");
                Log.e(TAG, "Failed to load driver name: " + error.getMessage());
                Toast.makeText(driver_profile.this,
                        "Failed to load profile data",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        btnViewChildren.setOnClickListener(v -> {
            Intent intent = new Intent(driver_profile.this, available_pickup.class);
            startActivity(intent);
        });

        btnUpdateProfile.setOnClickListener(v -> {
            Intent intent = new Intent(driver_profile.this, DriverFormActivity.class);
            startActivity(intent);
        });

        btnContactParent.setOnClickListener(v -> {
            showToast("Open a child's profile to contact their parent");
        });

        btnViewRequests.setOnClickListener(v -> {
            Intent intent = new Intent(driver_profile.this, view_request.class);
            startActivity(intent);
        });

        btnContactAdmin.setOnClickListener(v -> {
            showContactAdminDialog();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            showDeleteAccountDialog();
        });

        // Logout
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void showLogoutConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        mAuth.signOut();
        Intent intent = new Intent(driver_profile.this, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(driver_profile.this, driver_active_dashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navLocation.setOnClickListener(v -> {
            Intent intent = new Intent(driver_profile.this, available_pickup.class);
            startActivity(intent);
        });

        navQr.setOnClickListener(v -> {
            Intent intent = new Intent(driver_profile.this, QR_scan.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            // Already on profile screen
        });
    }

    private void showDeleteAccountDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteAccount();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        if (currentDriverId == null) {
            showToast("No user logged in");
            return;
        }

        // Delete from Realtime Database
        mDatabase.child("drivers").child(currentDriverId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Delete Firebase Auth account
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        user.delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    showToast("Account deleted successfully");
                                    Intent intent = new Intent(driver_profile.this, login.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to delete auth account: " + e.getMessage());
                                    showToast("Failed to delete account: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete database record: " + e.getMessage());
                    showToast("Failed to delete account data");
                });
    }

    private void showContactAdminDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Contact Admin")
                .setMessage("Would you like to call admin support?")
                .setPositiveButton("Call", (dialog, which) -> {
                    String adminPhoneNumber = "+94771234567";
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(android.net.Uri.parse("tel:" + adminPhoneNumber));
                        showToast("Calling Admin Support...");
                        startActivity(callIntent);
                    } catch (Exception e) {
                        showToast("Unable to make call. Please try again.");
                        e.printStackTrace();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh driver name when returning to this screen
        loadDriverName();
    }
}