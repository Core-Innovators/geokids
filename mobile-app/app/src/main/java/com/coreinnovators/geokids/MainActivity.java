package com.coreinnovators.geokids;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final long SPLASH_DURATION_MS = 2500; // 2.5 seconds
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progressBar);
        animateProgressBar();

        // After splash, check if user is already logged in
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                // User is already signed in — navigate directly to correct screen
                Log.d(TAG, "User already signed in: " + currentUser.getUid());
                checkUserRoleAndNavigate(currentUser.getUid());
            } else {
                // No active session — go to login
                Log.d(TAG, "No active session, navigating to login");
                startActivity(new Intent(MainActivity.this, login.class));
                finish();
            }
        }, SPLASH_DURATION_MS);
    }

    private void animateProgressBar() {
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        animation.setDuration(SPLASH_DURATION_MS);
        animation.start();
    }

    private void checkUserRoleAndNavigate(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        String name = doc.getString("name");

                        if ("Parent".equals(role)) {
                            checkParentChildrenStatusAndNavigate(userId);
                        } else if ("Driver".equals(role)) {
                            checkDriverStatusAndNavigate(userId, name);
                        } else {
                            // Role not assigned — go to login
                            goToLogin();
                        }
                    } else {
                        goToLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user data on splash: " + e.getMessage());
                    goToLogin();
                });
    }

    private void checkParentChildrenStatusAndNavigate(String parentId) {
        db.collection("children")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        navigateTo(parent_dashboard.class);
                    } else {
                        boolean hasAtLeastOneActive = false;

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (document.contains("assignedDriver")) {
                                Object assignedDriverObj = document.get("assignedDriver");
                                if (assignedDriverObj instanceof java.util.Map) {
                                    java.util.Map<String, Object> assignedDriver =
                                            (java.util.Map<String, Object>) assignedDriverObj;
                                    String status = (String) assignedDriver.get("status");
                                    if ("accepted".equals(status)) {
                                        hasAtLeastOneActive = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (hasAtLeastOneActive) {
                            navigateTo(ParentActiveDashboard.class);
                        } else {
                            navigateTo(ParentPendingDashboard.class);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking children on splash: " + e.getMessage());
                    goToLogin();
                });
    }

    private void checkDriverStatusAndNavigate(String driverId, String driverName) {
        db.collection("drivers").document(driverId)
                .get()
                .addOnSuccessListener(driverDoc -> {
                    if (driverDoc.exists()) {
                        String status = driverDoc.getString("status");
                        String rejectionReason = driverDoc.getString("rejection_reason");

                        if (status == null) {
                            navigateTo(driver_dashboard.class);
                        } else if ("pending".equalsIgnoreCase(status.trim())) {
                            Intent intent = new Intent(MainActivity.this, driver_pending_dashboard.class);
                            intent.putExtra("driver_id", driverId);
                            intent.putExtra("driver_name", driverName);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else if ("approved".equalsIgnoreCase(status.trim()) ||
                                "active".equalsIgnoreCase(status.trim())) {
                            Intent intent = new Intent(MainActivity.this, driver_active_dashboard.class);
                            intent.putExtra("driver_id", driverId);
                            intent.putExtra("driver_name", driverName);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else if ("rejected".equalsIgnoreCase(status.trim())) {
                            Intent intent = new Intent(MainActivity.this, driver_rejection_screen.class);
                            intent.putExtra("driver_id", driverId);
                            intent.putExtra("driver_name", driverName);
                            intent.putExtra("rejection_reason", rejectionReason);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            navigateTo(driver_dashboard.class);
                        }
                    } else {
                        navigateTo(driver_dashboard.class);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking driver on splash: " + e.getMessage());
                    goToLogin();
                });
    }

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(MainActivity.this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        startActivity(new Intent(MainActivity.this, login.class));
        finish();
    }
}
