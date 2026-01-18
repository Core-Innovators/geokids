package com.coreinnovators.geokids;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class login extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText emailEt, passwordEt;
    private Button loginBtn;
    private TextView registerTv;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEt = findViewById(R.id.email);
        passwordEt = findViewById(R.id.password);
        loginBtn = findViewById(R.id.btnLogin);
        registerTv = findViewById(R.id.register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerTv.setOnClickListener(v -> startActivity(new Intent(login.this, signup.class)));

        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Logging in...");

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            checkUserRoleAndNavigate(user.getUid());
                        }
                    } else {
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Login");
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
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
                            loginBtn.setEnabled(true);
                            loginBtn.setText("Login");
                            Toast.makeText(this, "Role not assigned", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Login");
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Login");
                    Log.e(TAG, "Error fetching user data: " + e.getMessage());
                    Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkParentChildrenStatusAndNavigate(String parentId) {
        Log.d(TAG, "Checking parent children status for ID: " + parentId);

        db.collection("children")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No children added - navigate to parent dashboard (add child screen)
                        Log.d(TAG, "No children found - navigating to parent_dashboard");
                        navigateToParentDashboard();
                    } else {
                        // Parent has children - check their status
                        List<ChildStatus> childrenStatus = new ArrayList<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            ChildStatus childStatus = new ChildStatus();
                            childStatus.childId = document.getId();
                            childStatus.childName = document.getString("childName");

                            // Check if child has assignedDriver field
                            if (document.contains("assignedDriver")) {
                                Object assignedDriverObj = document.get("assignedDriver");
                                if (assignedDriverObj instanceof java.util.Map) {
                                    java.util.Map<String, Object> assignedDriver =
                                            (java.util.Map<String, Object>) assignedDriverObj;
                                    String status = (String) assignedDriver.get("status");

                                    if ("accepted".equals(status)) {
                                        childStatus.hasActiveDriver = true;
                                    }
                                }
                            }

                            childrenStatus.add(childStatus);
                            Log.d(TAG, "Child: " + childStatus.childName +
                                    " - Active Driver: " + childStatus.hasActiveDriver);
                        }

                        // Determine which dashboard to navigate to
                        boolean hasAtLeastOneActive = false;
                        boolean hasAtLeastOnePending = false;

                        for (ChildStatus status : childrenStatus) {
                            if (status.hasActiveDriver) {
                                hasAtLeastOneActive = true;
                            } else {
                                hasAtLeastOnePending = true;
                            }
                        }

                        if (hasAtLeastOneActive) {
                            // At least one child has an active driver
                            Log.d(TAG, "Parent has active children - navigating to ParentActiveDashboard");
                            navigateToParentActiveDashboard();
                        } else {
                            // All children are pending driver assignment
                            Log.d(TAG, "All children pending - navigating to ParentPendingDashboard");
                            navigateToParentPendingDashboard();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Login");
                    Log.e(TAG, "Error checking children status: " + e.getMessage());
                    Toast.makeText(this, "Error loading children data", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkDriverStatusAndNavigate(String driverId, String driverName) {
        Log.d(TAG, "Checking driver status for ID: " + driverId);

        db.collection("drivers").document(driverId)
                .get()
                .addOnSuccessListener(driverDoc -> {
                    Log.d(TAG, "Driver document exists: " + driverDoc.exists());

                    if (driverDoc.exists()) {
                        String status = driverDoc.getString("status");
                        String rejectionReason = driverDoc.getString("rejection_reason");
                        Log.d(TAG, "Driver status from Firestore: " + status);

                        if (status == null) {
                            Log.d(TAG, "Status is null - navigating to driver dashboard");
                            navigateToDriverDashboard();
                        } else if ("pending".equalsIgnoreCase(status.trim())) {
                            Log.d(TAG, "Status is pending - navigating to pending dashboard");
                            navigateToPendingDashboard(driverId, driverName);
                        } else if ("approved".equalsIgnoreCase(status.trim()) ||
                                "active".equalsIgnoreCase(status.trim())) {
                            Log.d(TAG, "Status is approved/active - navigating to active dashboard");
                            navigateToActiveDriverDashboard(driverId, driverName);
                        } else if ("rejected".equalsIgnoreCase(status.trim())) {
                            Log.d(TAG, "Status is rejected - navigating to rejection screen");
                            navigateToRejectionScreen(driverId, driverName, rejectionReason);
                        } else {
                            Log.d(TAG, "Unknown status: " + status + " - navigating to driver dashboard");
                            navigateToDriverDashboard();
                        }
                    } else {
                        Log.d(TAG, "Driver document not found - navigating to driver dashboard");
                        navigateToDriverDashboard();
                    }
                })
                .addOnFailureListener(e -> {
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Login");
                    Log.e(TAG, "Error checking driver status: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Error loading driver data", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToParentDashboard() {
        Intent intent = new Intent(login.this, parent_dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToParentActiveDashboard() {
        Intent intent = new Intent(login.this, ParentActiveDashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToParentPendingDashboard() {
        Intent intent = new Intent(login.this, ParentPendingDashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToDriverDashboard() {
        Intent intent = new Intent(login.this, driver_dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToPendingDashboard(String driverId, String driverName) {
        Intent intent = new Intent(login.this, driver_pending_dashboard.class);
        intent.putExtra("driver_id", driverId);
        intent.putExtra("driver_name", driverName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToActiveDriverDashboard(String driverId, String driverName) {
        Intent intent = new Intent(login.this, driver_active_dashboard.class);
        intent.putExtra("driver_id", driverId);
        intent.putExtra("driver_name", driverName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToRejectionScreen(String driverId, String driverName, String rejectionReason) {
        Intent intent = new Intent(login.this, driver_rejection_screen.class);
        intent.putExtra("driver_id", driverId);
        intent.putExtra("driver_name", driverName);
        intent.putExtra("rejection_reason", rejectionReason);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Inner class to track child status
    private static class ChildStatus {
        String childId;
        String childName;
        boolean hasActiveDriver = false;
    }
}