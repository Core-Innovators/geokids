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

    // UI Components
    private EditText childNameInput, childAgeInput;
    private EditText childSchoolInput, childGradeInput;
    private ImageView childProfileImage;
    private Button submitButton;

    // Firebase
    private FirebaseFirestore firestore;

    // Data from previous activity
    private String parentDocId;
    private String parentId;
    private String parentName;
    private String parentNic;
    private String parentContact1;
    private String parentContact2;

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

        // Get data from previous activity
        getIntentData();

        // Initialize Views
        initializeViews();

        // Setup Image Picker
        setupImagePicker();

        // Setup Listeners
        setupListeners();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        parentDocId = intent.getStringExtra("parent_doc_id");
        parentId = intent.getStringExtra("parent_id");
        parentName = intent.getStringExtra("parent_name");
        parentNic = intent.getStringExtra("parent_nic");
        parentContact1 = intent.getStringExtra("parent_contact1");
        parentContact2 = intent.getStringExtra("parent_contact2");

        Log.d(TAG, "Parent Doc ID: " + parentDocId);
        Log.d(TAG, "Parent ID: " + parentId);
        Log.d(TAG, "Parent Name: " + parentName);
    }

    private void initializeViews() {
        childNameInput = findViewById(R.id.child_name);
        childAgeInput = findViewById(R.id.child_age);
        childSchoolInput = findViewById(R.id.child_school);
        childGradeInput = findViewById(R.id.child_grade);
        childProfileImage = findViewById(R.id.child_profile_image);
        submitButton = findViewById(R.id.submit_button);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (childProfileImage != null && selectedImageUri != null) {
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .into(childProfileImage);
                            Toast.makeText(this, "Profile image selected!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void setupListeners() {
        // Profile image click to select image
        childProfileImage.setOnClickListener(v -> {
            Log.d(TAG, "Profile image clicked");
            openImagePicker();
        });

        // Submit button to save child data
        submitButton.setOnClickListener(v -> validateAndSubmit());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void validateAndSubmit() {
        // Get input values
        String childName = childNameInput.getText().toString().trim();
        String childAge = childAgeInput.getText().toString().trim();
        String childSchool = childSchoolInput.getText().toString().trim();
        String childGrade = childGradeInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(childName)) {
            childNameInput.setError("Child name is required");
            childNameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(childAge)) {
            childAgeInput.setError("Child age is required");
            childAgeInput.requestFocus();
            return;
        }

        // Validate age is a valid number
        try {
            int age = Integer.parseInt(childAge);
            if (age <= 0 || age > 18) {
                childAgeInput.setError("Please enter a valid age (1-18)");
                childAgeInput.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            childAgeInput.setError("Please enter a valid number");
            childAgeInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(childSchool)) {
            childSchoolInput.setError("Child school is required");
            childSchoolInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(childGrade)) {
            childGradeInput.setError("Child grade is required");
            childGradeInput.requestFocus();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a profile image",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // All validations passed
        submitChildData(childName, childAge, childSchool, childGrade);
    }

    private void submitChildData(String childName, String childAge,
                                 String childSchool, String childGrade) {
        // Show loading
        showLoading(true);

        // Save data with local image URI
        saveToFirestore(childName, childAge, childSchool, childGrade);
    }

    private void saveToFirestore(String childName, String childAge,
                                 String childSchool, String childGrade) {
        // Use the local image URI (stored in device)
        String imageUrl = selectedImageUri.toString();

        // Create child data map
        Map<String, Object> childData = new HashMap<>();
        childData.put("parentId", parentId);
        childData.put("parentDocId", parentDocId);
        childData.put("childName", childName);
        childData.put("parentName", parentName);
        childData.put("childAge", childAge);
        childData.put("childSchool", childSchool);
        childData.put("parentNic", parentNic);
        childData.put("parentContact1", parentContact1);
        childData.put("parentContact2", parentContact2);
        childData.put("childGrade", childGrade);
        childData.put("childProfileImageUri", imageUrl); // Local URI
        childData.put("createdAt", System.currentTimeMillis());
        childData.put("updatedAt", System.currentTimeMillis());
        childData.put("status", "active");

        Log.d(TAG, "Saving child data to Firestore");
        Log.d(TAG, "Child Name: " + childName);
        Log.d(TAG, "Child Age: " + childAge);
        Log.d(TAG, "Child School: " + childSchool);
        Log.d(TAG, "Child Grade: " + childGrade);

        // Save to Firestore
        firestore.collection("children")
                .add(childData)
                .addOnSuccessListener(documentReference -> {
                    showLoading(false);
                    String childId = documentReference.getId();
                    Log.d(TAG, "Child saved with ID: " + childId);

                    Toast.makeText(this, "Child profile added successfully!",
                            Toast.LENGTH_LONG).show();

                    // Clear the form for adding another child
                    clearForm();

                    // Show dialog to add another child or go to dashboard
                    showAddAnotherChildDialog();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error saving child: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            submitButton.setEnabled(!show);
            submitButton.setText(show ? "Saving..." : "Submit");

            // Disable all inputs during save
            childNameInput.setEnabled(!show);
            childAgeInput.setEnabled(!show);
            childSchoolInput.setEnabled(!show);
            childGradeInput.setEnabled(!show);
            childProfileImage.setEnabled(!show);
        });
    }

    private void clearForm() {
        // Clear all child-specific fields
        childNameInput.setText("");
        childAgeInput.setText("");
        childSchoolInput.setText("");
        childGradeInput.setText("");

        // Reset image to default avatar
        childProfileImage.setImageResource(R.drawable.avatar);
        selectedImageUri = null;
    }

    private void showAddAnotherChildDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("Child profile added successfully! Would you like to add another child?")
                .setPositiveButton("Add Another Child", (dialog, which) -> {
                    // Form is already cleared, just dismiss dialog
                    dialog.dismiss();
                })
                .setNegativeButton("Go to Dashboard", (dialog, which) -> {
                    // Navigate to parent dashboard
                    Intent intent = new Intent(AddChildNext.this, parent_dashboard.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}