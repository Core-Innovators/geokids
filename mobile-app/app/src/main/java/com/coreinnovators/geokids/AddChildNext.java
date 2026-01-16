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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddChildNext extends AppCompatActivity {
    private static final String TAG = "AddChildNext";
    private static final String COLLECTION_NAME = "children";

    // UI Components
    private EditText childNameInput, parentNameInput, childAgeInput;
    private EditText childGradeInput, childSchoolInput;
    private ImageView childProfileImage;
    private Button submitButton;

    // Firebase
    private FirebaseFirestore firestore;

    // Data from previous activity
    private String childId;
    private String parentId;
    private String parentName;

    // Selected Image
    private Uri selectedImageUri;

    // Image Picker Launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child_next);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();

        // Get data from intent
        Intent intent = getIntent();
        childId = intent.getStringExtra("child_id");
        parentId = intent.getStringExtra("parent_id");
        parentName = intent.getStringExtra("parent_name");

        Log.d(TAG, "Child ID: " + childId);
        Log.d(TAG, "Parent ID: " + parentId);
        Log.d(TAG, "Parent Name: " + parentName);

        // Check if childId is null - if so, create a new child document
        if (childId == null || childId.isEmpty()) {
            Log.d(TAG, "Child ID not provided, creating new child document");
            childId = firestore.collection(COLLECTION_NAME).document().getId();
            Log.d(TAG, "Generated Child ID: " + childId);
        }

        // Initialize Views
        initializeViews();

        // Setup Image Picker
        setupImagePicker();

        // Setup Listeners
        setupListeners();

        // Pre-fill parent name if available
        if (parentName != null && !parentName.isEmpty()) {
            parentNameInput.setText(parentName);
        }
    }

    private void initializeViews() {
        childNameInput = findViewById(R.id.child_name);
        parentNameInput = findViewById(R.id.parent_name);
        childAgeInput = findViewById(R.id.child_age);
        childGradeInput = findViewById(R.id.child_grade);
        childSchoolInput = findViewById(R.id.child_school);
        childProfileImage = findViewById(R.id.child_profile_image);
        submitButton = findViewById(R.id.select_school);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (childProfileImage != null) {
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .circleCrop()
                                    .into(childProfileImage);
                        }
                        Toast.makeText(this, "Profile image selected!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupListeners() {
        // Child profile image click
        if (childProfileImage != null) {
            childProfileImage.setOnClickListener(v -> openImagePicker());
        }

        // Submit button - validate and submit form
        submitButton.setOnClickListener(v -> validateAndSubmit());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void validateAndSubmit() {
        // Get input values
        String childName = childNameInput.getText().toString().trim();
        String parentName = parentNameInput.getText().toString().trim();
        String childAge = childAgeInput.getText().toString().trim();
        String childGrade = childGradeInput.getText().toString().trim();
        String childSchool = childSchoolInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(childName)) {
            childNameInput.setError("Child name is required");
            childNameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(parentName)) {
            parentNameInput.setError("Parent name is required");
            parentNameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(childAge)) {
            childAgeInput.setError("Child age is required");
            childAgeInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(childGrade)) {
            childGradeInput.setError("Child grade is required");
            childGradeInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(childSchool)) {
            childSchoolInput.setError("Child school is required");
            childSchoolInput.requestFocus();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        // All validations passed
        updateChildData(childName, parentName, childAge, childGrade, childSchool);
    }

    private void updateChildData(String childName, String parentName, String childAge,
                                 String childGrade, String childSchool) {
        // Show loading
        showLoading(true);

        // Step 1: Upload image to Supabase
        SupabaseHelper.uploadImage(this, selectedImageUri)
                .thenAccept(imageUrl -> {
                    // Step 2: Update Firestore with all child data including image URL
                    runOnUiThread(() -> {
                        updateFirestore(childName, parentName, childAge, childGrade,
                                childSchool, imageUrl);
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Log.e(TAG, "Image upload failed: " + e.getMessage(), e);
                        Toast.makeText(AddChildNext.this, "Failed to upload image: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                    return null;
                });
    }

    private void updateFirestore(String childName, String parentName, String childAge,
                                 String childGrade, String childSchool, String imageUrl) {
        if (childId == null || childId.isEmpty()) {
            showLoading(false);
            Toast.makeText(this, "Error: Child ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create update map with all child information
        Map<String, Object> childData = new HashMap<>();
        childData.put("childName", childName);
        childData.put("parentName", parentName);
        childData.put("childAge", childAge);
        childData.put("childGrade", childGrade);
        childData.put("childSchool", childSchool);
        childData.put("childProfileImageUrl", imageUrl);
        childData.put("parentId", parentId);
        childData.put("createdAt", System.currentTimeMillis());
        childData.put("updatedAt", System.currentTimeMillis());

        Log.d(TAG, "Saving child document: " + childId);
        Log.d(TAG, "Child name: " + childName);
        Log.d(TAG, "Image URL: " + imageUrl);

        // Use set with merge to create or update the document
        firestore.collection(COLLECTION_NAME)
                .document(childId)
                .set(childData)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Log.d(TAG, "Child data saved successfully");
                    Toast.makeText(this, "Child profile completed successfully!",
                            Toast.LENGTH_SHORT).show();

                    // Navigate to Select Driver Activity
                    Intent intent = new Intent(AddChildNext.this, SelectDriverActivity.class);
                    intent.putExtra("child_id", childId);
                    intent.putExtra("parent_id", parentId);
                    intent.putExtra("child_school", childSchool);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error saving child: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to save child data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            submitButton.setEnabled(!show);
            submitButton.setText(show ? "Saving..." : "Submit");

            // Disable all inputs during upload
            childNameInput.setEnabled(!show);
            parentNameInput.setEnabled(!show);
            childAgeInput.setEnabled(!show);
            childGradeInput.setEnabled(!show);
            childSchoolInput.setEnabled(!show);
            if (childProfileImage != null) {
                childProfileImage.setEnabled(!show);
            }
        });
    }
}