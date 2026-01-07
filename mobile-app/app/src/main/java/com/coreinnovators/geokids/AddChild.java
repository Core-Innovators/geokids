package com.coreinnovators.geokids;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddChild extends AppCompatActivity {

    private static final String TAG = "AddChildActivity";
    private static final String COLLECTION_NAME = "children";

    // UI Components
    private EditText parentNameInput, parentNicInput, parentContact1Input, parentContact2Input;
    private EditText pickupLocationInput;
    private Button searchButton, nextButton;

    // Firebase
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String currentUserId;

    // Selected Image
    private Uri selectedImageUri;

    // Selected Route
    private RouteData selectedRouteData;

    // Image Picker Launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

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
        pickupLocationInput = findViewById(R.id.start_point);
        searchButton = findViewById(R.id.search);
        nextButton = findViewById(R.id.select_school);
    }


    private void setupListeners() {

        // Search button - for route selection
        searchButton.setOnClickListener(v -> searchRoute());

        // Next button - submit form
        nextButton.setOnClickListener(v -> validateAndSubmit());
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


    private void searchRoute() {
        String pickupLocation = pickupLocationInput.getText().toString().trim();

        Log.d(TAG, "Search button clicked!");
        Log.d(TAG, "Pickup location: " + pickupLocation);

        if (TextUtils.isEmpty(pickupLocation)) {
            Toast.makeText(this, "Please enter pickup location", Toast.LENGTH_SHORT).show();
            return;
        }

        // For now, you can implement route search later
        // This is a placeholder for route functionality
        Toast.makeText(this, "Route search will be implemented", Toast.LENGTH_SHORT).show();

        // If you have school location, you can implement geocoding like in next_form.java
        // geocodeAndShowRoutes(pickupLocation, schoolLocation);
    }

    private void validateAndSubmit() {
        // Get input values
        String parentName = parentNameInput.getText().toString().trim();
        String parentNic = parentNicInput.getText().toString().trim();
        String parentContact1 = parentContact1Input.getText().toString().trim();
        String parentContact2 = parentContact2Input.getText().toString().trim();
        String pickupLocation = pickupLocationInput.getText().toString().trim();

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

        if (TextUtils.isEmpty(pickupLocation)) {
            pickupLocationInput.setError("Pickup location is required");
            pickupLocationInput.requestFocus();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        // All validations passed, proceed with upload
        submitChildData(parentName, parentNic, parentContact1, parentContact2, pickupLocation);
    }

    private void submitChildData(String parentName, String parentNic, String parentContact1,
                                 String parentContact2, String pickupLocation) {
        // Show loading
        showLoading(true);

        // Step 1: Upload image to Supabase
        SupabaseHelper.uploadImage(this, selectedImageUri)
                .thenAccept(imageUrl -> {
                    // Step 2: Save data to Firebase Firestore
                    saveToFirestore(parentName, parentNic, parentContact1, parentContact2,
                            pickupLocation, imageUrl);
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Log.e(TAG, "Image upload failed: " + e.getMessage());
                        Toast.makeText(this, "Failed to upload image: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                    return null;
                });
    }

    private void saveToFirestore(String parentName, String parentNic, String parentContact1,
                                 String parentContact2, String pickupLocation, String imageUrl) {
        // Create child data map
        Map<String, Object> childData = new HashMap<>();
        childData.put("parentId", currentUserId);
        childData.put("parentName", parentName);
        childData.put("parentNic", parentNic);
        childData.put("parentContact1", parentContact1);
        childData.put("parentContact2", parentContact2);
        childData.put("pickupLocation", pickupLocation);
        childData.put("childProfileImageUrl", imageUrl);
        childData.put("createdAt", System.currentTimeMillis());
        childData.put("status", "active");

        // Add route data if available
        if (selectedRouteData != null) {
            childData.put("routeData", selectedRouteData.toMap());
        }

        Log.d(TAG, "Saving child data for parent: " + currentUserId);

        // Add child document to Firestore
        firestore.collection(COLLECTION_NAME)
                .add(childData)
                .addOnSuccessListener(documentReference -> {
                    showLoading(false);
                    String childId = documentReference.getId();
                    Log.d(TAG, "Child data saved with ID: " + childId);
                    Toast.makeText(this, "Child added successfully!",
                            Toast.LENGTH_SHORT).show();

                    // Navigate to next child form (AddChildNext activity)
                    Intent intent = new Intent(AddChild.this, AddChildNext.class);
                    intent.putExtra("child_id", childId);
                    intent.putExtra("parent_id", currentUserId);
                    intent.putExtra("parent_name", parentName);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error adding child: " + e.getMessage());
                    Toast.makeText(this, "Failed to save child data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            nextButton.setEnabled(!show);
            nextButton.setText(show ? "Saving..." : "Next");

            // Disable all inputs during upload
            parentNameInput.setEnabled(!show);
            parentNicInput.setEnabled(!show);
            parentContact1Input.setEnabled(!show);
            parentContact2Input.setEnabled(!show);
            pickupLocationInput.setEnabled(!show);
            searchButton.setEnabled(!show);
        });
    }
}