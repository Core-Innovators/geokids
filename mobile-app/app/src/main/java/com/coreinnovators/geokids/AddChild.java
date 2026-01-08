package com.coreinnovators.geokids;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddChild extends AppCompatActivity {
    private static final String TAG = "AddChildActivity";

    // UI Components
    private EditText parentNameInput, parentNicInput, parentContact1Input, parentContact2Input;
    private Button submitButton;

    // Firebase
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get current user ID
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        Log.d(TAG, "Current user ID: " + currentUserId);

        // Initialize Views
        initializeViews();

        // Setup Listeners
        setupListeners();

        // Load parent data
        loadParentData();
    }

    private void initializeViews() {
        parentNameInput = findViewById(R.id.parent_name);
        parentNicInput = findViewById(R.id.parent_nic);
        parentContact1Input = findViewById(R.id.parent_contact);
        parentContact2Input = findViewById(R.id.parent_contact2);
        submitButton = findViewById(R.id.select_school);
    }

    private void setupListeners() {
        submitButton.setOnClickListener(v -> validateAndSubmit());
    }

    private void loadParentData() {
        // Load parent data from users collection
        firestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Pre-fill parent name if available
                        String username = documentSnapshot.getString("username");
                        if (username == null || username.isEmpty()) {
                            username = documentSnapshot.getString("name");
                        }
                        if (username != null && !username.isEmpty()) {
                            parentNameInput.setText(username);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load parent data: " + e.getMessage());
                });
    }

    private void validateAndSubmit() {
        // Get input values
        String parentName = parentNameInput.getText().toString().trim();
        String parentNic = parentNicInput.getText().toString().trim();
        String parentContact1 = parentContact1Input.getText().toString().trim();
        String parentContact2 = parentContact2Input.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(parentName)) {
            parentNameInput.setError("Parent name is required");
            parentNameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(parentNic)) {
            parentNicInput.setError("Parent NIC is required");
            parentNicInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(parentContact1)) {
            parentContact1Input.setError("Contact number is required");
            parentContact1Input.requestFocus();
            return;
        }

        if (parentContact1.length() < 10) {
            parentContact1Input.setError("Invalid contact number");
            parentContact1Input.requestFocus();
            return;
        }

        // All validations passed, proceed with saving
        submitParentData(parentName, parentNic, parentContact1, parentContact2);
    }

    private void submitParentData(String parentName, String parentNic,
                                  String parentContact1, String parentContact2) {
        // Show loading
        showLoading(true);

        // Create parent data map
        Map<String, Object> parentData = new HashMap<>();
        parentData.put("parentId", currentUserId);
        parentData.put("parentName", parentName);
        parentData.put("parentNic", parentNic);
        parentData.put("parentContact1", parentContact1);
        parentData.put("parentContact2", parentContact2);
        parentData.put("createdAt", System.currentTimeMillis());
        parentData.put("updatedAt", System.currentTimeMillis());
        parentData.put("status", "active");

        Log.d(TAG, "Saving parent data for: " + currentUserId);

        // Add parent document to Firestore
        firestore.collection("parents")
                .add(parentData)
                .addOnSuccessListener(documentReference -> {
                    showLoading(false);
                    String parentDocId = documentReference.getId();
                    Log.d(TAG, "Parent data saved with ID: " + parentDocId);

                    Toast.makeText(this, "Parent information saved successfully!",
                            Toast.LENGTH_SHORT).show();

                    // Navigate to AddChildNext activity
                    try {
                        Intent intent = new Intent(AddChild.this, AddChildNext.class);
                        intent.putExtra("parent_doc_id", parentDocId);
                        intent.putExtra("parent_id", currentUserId);
                        intent.putExtra("parent_name", parentName);
                        intent.putExtra("parent_nic", parentNic);
                        intent.putExtra("parent_contact1", parentContact1);
                        intent.putExtra("parent_contact2", parentContact2);

                        Log.d(TAG, "Starting AddChildNext activity");
                        Log.d(TAG, "Intent extras - parent_doc_id: " + parentDocId);

                        startActivity(intent);

                        // Don't call finish() immediately - let the activity start first
                        // Only finish after a short delay or don't finish at all
                        // This allows users to go back if needed

                    } catch (Exception e) {
                        Log.e(TAG, "Error starting AddChildNext: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(this, "Error opening next page: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error saving parent data: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            submitButton.setEnabled(!show);
            submitButton.setText(show ? "Saving..." : "Submit");
            parentNameInput.setEnabled(!show);
            parentNicInput.setEnabled(!show);
            parentContact1Input.setEnabled(!show);
            parentContact2Input.setEnabled(!show);
        });
    }
}