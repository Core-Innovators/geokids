package com.coreinnovators.geokids;

import android.app.DatePickerDialog;
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

import java.util.Calendar;

public class DriverFormActivity extends AppCompatActivity {

    private static final String TAG = "DriverFormActivity";
    private static final String COLLECTION_NAME = "drivers";

    // UI Components
    private EditText nameInput, addressInput, nicInput, birthdayInput, contactInput;
    private ImageView profileImageView;
    private Button nextButton;

    // Firebase
    private FirebaseFirestore firestore;

    // Selected Image
    private Uri selectedImageUri;

    // Image Picker Launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driverformactivity);



        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();

        // Initialize Views
        initializeViews();

        // Setup Image Picker
        setupImagePicker();

        // Setup Listeners
        setupListeners();
    }

    private void initializeViews() {
        nameInput = findViewById(R.id.name);
        addressInput = findViewById(R.id.address);
        nicInput = findViewById(R.id.nic);
        birthdayInput = findViewById(R.id.birthday);
        contactInput = findViewById(R.id.drivercontact);
        profileImageView = findViewById(R.id.imageView);
        nextButton = findViewById(R.id.add);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();

                        // Display selected image using Glide
                        Glide.with(this)
                                .load(selectedImageUri)
                                .circleCrop()
                                .into(profileImageView);

                        Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupListeners() {
        // Image selection
        profileImageView.setOnClickListener(v -> openImagePicker());

        // Birthday picker
        birthdayInput.setOnClickListener(v -> showDatePicker());
        birthdayInput.setFocusable(false);
        birthdayInput.setClickable(true);

        // Submit button
        nextButton.setOnClickListener(v -> validateAndSubmit());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    birthdayInput.setText(date);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void validateAndSubmit() {
        // Get input values
        String name = nameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String nic = nicInput.getText().toString().trim();
        String birthday = birthdayInput.getText().toString().trim();
        String contact = contactInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(address)) {
            addressInput.setError("Address is required");
            addressInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(nic)) {
            nicInput.setError("NIC is required");
            nicInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(birthday)) {
            birthdayInput.setError("Birthday is required");
            birthdayInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(contact)) {
            contactInput.setError("Contact number is required");
            contactInput.requestFocus();
            return;
        }

        if (contact.length() < 10) {
            contactInput.setError("Invalid contact number");
            contactInput.requestFocus();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        // All validations passed, proceed with upload
        submitDriverData(name, address, nic, birthday, contact);
    }

    private void submitDriverData(String name, String address, String nic,
                                  String birthday, String contact) {
        // Show loading
        showLoading(true);

        // Step 1: Upload image to Supabase
        SupabaseHelper.uploadImage(this, selectedImageUri)
                .thenAccept(imageUrl -> {
                    // Step 2: Save data to Firebase Firestore
                    saveToFirestore(name, address, nic, birthday, contact, imageUrl);
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

    private void saveToFirestore(String name, String address, String nic,
                                 String birthday, String contact, String imageUrl) {
        // Create driver object
        Driver driver = new Driver(name, address, nic, birthday, contact, imageUrl);

        // Save to Firestore
        firestore.collection(COLLECTION_NAME)
                .add(driver.toMap())
                .addOnSuccessListener(documentReference -> {
                    showLoading(false);
                    Log.d(TAG, "Driver added with ID: " + documentReference.getId());
                    Toast.makeText(this, "Driver registered successfully!",
                            Toast.LENGTH_SHORT).show();

                    // Clear form
                    clearForm();

                    // Optional: Navigate to next activity
                    // Intent intent = new Intent(DriverFormActivity.this, NextActivity.class);
                    // intent.putExtra("driver_id", documentReference.getId());
                    // startActivity(intent);
                    // finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error adding driver: " + e.getMessage());
                    Toast.makeText(this, "Failed to save driver data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            nextButton.setEnabled(!show);
            nextButton.setText(show ? "Uploading..." : "Next");

            // Disable all inputs during upload
            nameInput.setEnabled(!show);
            addressInput.setEnabled(!show);
            nicInput.setEnabled(!show);
            birthdayInput.setEnabled(!show);
            contactInput.setEnabled(!show);
            profileImageView.setEnabled(!show);
        });
    }

    private void clearForm() {
        nameInput.setText("");
        addressInput.setText("");
        nicInput.setText("");
        birthdayInput.setText("");
        contactInput.setText("");
        profileImageView.setImageResource(R.drawable.avatar);
        selectedImageUri = null;
    }
}