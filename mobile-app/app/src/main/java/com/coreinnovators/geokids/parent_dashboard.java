package com.coreinnovators.geokids;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class parent_dashboard extends AppCompatActivity {

    private static final String TAG = "ParentDashboard";

    // UI Components
    private TextView helloText;
    private MaterialButton addChildButton;
    private TextView supportText;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String currentUserId;
    private String parentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Get current user
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // User not logged in, redirect to login
            Intent intent = new Intent(parent_dashboard.this, AddChild.class);
            startActivity(intent);
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        Log.d(TAG, "Current user ID: " + currentUserId);

        // Initialize Views
        initializeViews();

        // Load parent data
        loadParentData();

        // Setup Listeners
        setupListeners();
    }

    private void initializeViews() {
        helloText = findViewById(R.id.hello);
        addChildButton = findViewById(R.id.add_child);
        supportText = findViewById(R.id.support);
    }

    private void loadParentData() {
        // Load parent name from Firestore
        firestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get parent name
                        parentName = documentSnapshot.getString("username");
                        if (parentName == null || parentName.isEmpty()) {
                            parentName = documentSnapshot.getString("name");
                        }
                        if (parentName == null || parentName.isEmpty()) {
                            parentName = "Parent";
                        }

                        // Update hello text
                        helloText.setText("Hello, " + parentName + "!");
                        Log.d(TAG, "Parent name loaded: " + parentName);
                    } else {
                        Log.d(TAG, "Parent document does not exist");
                        helloText.setText("Hello, Parent!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load parent data: " + e.getMessage());
                    helloText.setText("Hello, Parent!");
                });
    }

    private void setupListeners() {
        // Add Child Button Click
        addChildButton.setOnClickListener(v -> {
            Log.d(TAG, "Add child button clicked");

            // Navigate to AddChild activity
            Intent intent = new Intent(parent_dashboard.this, AddChild.class);

            // Pass parent information if needed
            if (parentName != null) {
                intent.putExtra("parent_name", parentName);
            }
            intent.putExtra("parent_id", currentUserId);

            startActivity(intent);
        });

        // Support Text Click (optional)
        supportText.setOnClickListener(v -> {
            Log.d(TAG, "Support text clicked");
            // You can implement support functionality here
            // For example: open email app, chat support, etc.
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload parent data when returning to this activity
        loadParentData();
    }
}