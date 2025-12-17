package com.coreinnovators.geokids;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
<<<<<<< HEAD
=======
import android.util.Log;
>>>>>>> 327c339 (Implement Driver Application Status Management and Conditional Navigation(refs #16))
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
<<<<<<< HEAD
=======
import com.google.firebase.firestore.DocumentSnapshot;
>>>>>>> 327c339 (Implement Driver Application Status Management and Conditional Navigation(refs #16))
import com.google.firebase.firestore.FirebaseFirestore;

public class login extends AppCompatActivity {

<<<<<<< HEAD
=======
    private static final String TAG = "LoginActivity";

>>>>>>> 327c339 (Implement Driver Application Status Management and Conditional Navigation(refs #16))
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

<<<<<<< HEAD
=======
        // Disable login button during authentication
        loginBtn.setEnabled(false);
        loginBtn.setText("Logging in...");

>>>>>>> 327c339 (Implement Driver Application Status Management and Conditional Navigation(refs #16))
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
<<<<<<< HEAD
                            db.collection("users").document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(doc -> {
                                        if (doc.exists()) {
                                            String role = doc.getString("role");
                                            if ("Parent".equals(role)) {
                                                startActivity(new Intent(login.this, parent_dashboard.class));
                                            } else if ("Driver".equals(role)) {
                                                startActivity(new Intent(login.this, driver_dashboard.class));
                                            } else {
                                                Toast.makeText(this, "Role not assigned", Toast.LENGTH_SHORT).show();
                                            }
                                            finish();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
=======
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
                            navigateToParentDashboard();
                        } else if ("Driver".equals(role)) {
                            // Check driver status in drivers collection
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

    private void checkDriverStatusAndNavigate(String driverId, String driverName) {
        Log.d(TAG, "Checking driver status for ID: " + driverId);

        db.collection("drivers").document(driverId)
                .get()
                .addOnSuccessListener(driverDoc -> {
                    Log.d(TAG, "Driver document exists: " + driverDoc.exists());

                    if (driverDoc.exists()) {
                        // Driver document exists - check status
                        String status = driverDoc.getString("status");
                        Log.d(TAG, "Driver status from Firestore: " + status);

                        // Log all fields for debugging
                        Log.d(TAG, "All driver data: " + driverDoc.getData());

                        if (status == null) {
                            // Status field doesn't exist - treat as new driver
                            Log.d(TAG, "Status is null - navigating to driver dashboard");
                            navigateToDriverDashboard();
                        } else if ("pending".equalsIgnoreCase(status.trim())) {
                            // Navigate to pending dashboard
                            Log.d(TAG, "Status is pending - navigating to pending dashboard");
                            navigateToPendingDashboard(driverId, driverName);
                        } else if ("approved".equalsIgnoreCase(status.trim()) ||
                                "active".equalsIgnoreCase(status.trim())) {
                            // Navigate to active driver dashboard
                            Log.d(TAG, "Status is approved/active - navigating to active dashboard");
                            navigateToActiveDriverDashboard(driverId, driverName);
                        } else if ("rejected".equalsIgnoreCase(status.trim())) {
                            // Navigate to rejection screen or show message
                            Log.d(TAG, "Status is rejected");
                            loginBtn.setEnabled(true);
                            loginBtn.setText("Login");
                            Toast.makeText(this, "Your application was rejected. Please contact support.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // Unknown status - navigate to normal driver dashboard
                            Log.d(TAG, "Unknown status: " + status + " - navigating to driver dashboard");
                            navigateToDriverDashboard();
                        }
                    } else {
                        // Driver document doesn't exist - first time or hasn't submitted form
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

    private void navigateToDriverDashboard() {
        // Normal driver dashboard (for new drivers who haven't submitted form yet)
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
        // Active/Approved driver dashboard
        Intent intent = new Intent(login.this, driver_active_dashboard.class);
        intent.putExtra("driver_id", driverId);
        intent.putExtra("driver_name", driverName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToRejectionScreen(String driverId, String driverName) {
        // Optional: Create a rejection screen to show why application was rejected
        Intent intent = new Intent(login.this, driver_rejection_screen.class);
        intent.putExtra("driver_id", driverId);
        intent.putExtra("driver_name", driverName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
>>>>>>> 327c339 (Implement Driver Application Status Management and Conditional Navigation(refs #16))
}
