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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class next_form extends AppCompatActivity {

    private static final String TAG = "NextFormActivity";
    private static final String COLLECTION_NAME = "drivers";
    private static final int MAX_VEHICLE_IMAGES = 10;

    // UI Components
    private ImageView frontLicenseImage, backLicenseImage;
    private Button uploadImagesButton, searchButton, submitButton;
    private EditText startPointInput, endPointInput;
    private TextView vehicleImagesNameText;

    // Firebase
    private FirebaseFirestore firestore;

    // Selected Images
    private Uri frontLicenseUri;
    private Uri backLicenseUri;
    private List<Uri> vehicleImageUris = new ArrayList<>();
    private RouteData selectedRouteData;  // Store selected route

    // Driver ID from previous screen
    private String driverId;

    // Image Picker Launchers
    private ActivityResultLauncher<Intent> frontLicenseLauncher;
    private ActivityResultLauncher<Intent> backLicenseLauncher;
    private ActivityResultLauncher<Intent> vehicleImagesLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_form);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();

        // Get driver ID from intent
        driverId = getIntent().getStringExtra("driver_id");

        // Initialize Views
        initializeViews();

        // Setup Image Pickers
        setupImagePickers();

        // Setup Listeners
        setupListeners();
    }

    private void initializeViews() {
        frontLicenseImage = findViewById(R.id.front_image);
        backLicenseImage = findViewById(R.id.back_image);
        uploadImagesButton = findViewById(R.id.image_add);
        searchButton = findViewById(R.id.search);
        submitButton = findViewById(R.id.add);
        startPointInput = findViewById(R.id.start_point);
        endPointInput = findViewById(R.id.end_point);
        vehicleImagesNameText = findViewById(R.id.vehicle_images_name);
    }

    private void setupImagePickers() {
        // Front License Picker
        frontLicenseLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        frontLicenseUri = result.getData().getData();
                        Glide.with(this)
                                .load(frontLicenseUri)
                                .into(frontLicenseImage);
                        Toast.makeText(this, "Front license selected!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Back License Picker
        backLicenseLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        backLicenseUri = result.getData().getData();
                        Glide.with(this)
                                .load(backLicenseUri)
                                .into(backLicenseImage);
                        Toast.makeText(this, "Back license selected!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Vehicle Images Picker (Multiple Selection)
        vehicleImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Handle multiple images
                        if (result.getData().getClipData() != null) {
                            int count = result.getData().getClipData().getItemCount();
                            vehicleImageUris.clear();

                            for (int i = 0; i < count && i < MAX_VEHICLE_IMAGES; i++) {
                                Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                                vehicleImageUris.add(imageUri);
                            }

                            updateVehicleImagesText();
                            Toast.makeText(this, count + " vehicle images selected", Toast.LENGTH_SHORT).show();
                        }
                        // Handle single image
                        else if (result.getData().getData() != null) {
                            vehicleImageUris.clear();
                            vehicleImageUris.add(result.getData().getData());
                            updateVehicleImagesText();
                            Toast.makeText(this, "1 vehicle image selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void setupListeners() {
        // Front license image click
        frontLicenseImage.setOnClickListener(v -> {
            Log.d(TAG, "Front license image clicked");
            openFrontLicensePicker();
        });

        // Back license image click
        backLicenseImage.setOnClickListener(v -> {
            Log.d(TAG, "Back license image clicked");
            openBackLicensePicker();
        });

        // Upload vehicle images button
        uploadImagesButton.setOnClickListener(v -> {
            Log.d(TAG, "Upload images button clicked");
            openVehicleImagesPicker();
        });

        // Search button - FREE OpenStreetMap routing
        searchButton.setOnClickListener(v -> {
            Log.d(TAG, "Search button clicked!");
            searchRoute();
        });

        // Submit button
        submitButton.setOnClickListener(v -> {
            Log.d(TAG, "Submit button clicked");
            validateAndSubmit();
        });
    }

    private void openFrontLicensePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        frontLicenseLauncher.launch(intent);
    }

    private void openBackLicensePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        backLicenseLauncher.launch(intent);
    }

    private void openVehicleImagesPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        vehicleImagesLauncher.launch(intent);
    }

    private void updateVehicleImagesText() {
        if (vehicleImageUris.isEmpty()) {
            vehicleImagesNameText.setText("No images yet");
        } else {
            vehicleImagesNameText.setText(vehicleImageUris.size() + " images selected");
        }
    }

    private void searchRoute() {
        String startPoint = startPointInput.getText().toString().trim();
        String endPoint = endPointInput.getText().toString().trim();

        Log.d(TAG, "Search button clicked!");
        Log.d(TAG, "Start point: " + startPoint);
        Log.d(TAG, "End point: " + endPoint);

        if (TextUtils.isEmpty(startPoint) || TextUtils.isEmpty(endPoint)) {
            Toast.makeText(this, "Please enter both start and end points", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Searching route...", Toast.LENGTH_SHORT).show();

        // Use FREE OpenStreetMap geocoding and routing
        geocodeAndShowRoutes(startPoint, endPoint);
    }

    private void geocodeAndShowRoutes(String startLocation, String endLocation) {
        Log.d(TAG, "Starting FREE geocoding for: " + startLocation + " -> " + endLocation);

        // Show loading
        searchButton.setEnabled(false);
        searchButton.setText("Searching...");

        // Use FREE OpenStreetMap geocoding (Nominatim) - NO API KEY NEEDED
        OSRMRouteHelper.geocodeLocation(startLocation, startLatLng -> {
            Log.d(TAG, "Start location geocoded: " + (startLatLng != null ? startLatLng.toString() : "null"));

            if (startLatLng == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Could not find start location: " + startLocation, Toast.LENGTH_LONG).show();
                    searchButton.setEnabled(true);
                    searchButton.setText("Search");
                });
                return;
            }

            // Geocode end location
            OSRMRouteHelper.geocodeLocation(endLocation, endLatLng -> {
                Log.d(TAG, "End location geocoded: " + (endLatLng != null ? endLatLng.toString() : "null"));

                if (endLatLng == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Could not find end location: " + endLocation, Toast.LENGTH_LONG).show();
                        searchButton.setEnabled(true);
                        searchButton.setText("Search");
                    });
                    return;
                }

                // Both locations geocoded successfully, fetch routes using FREE OSRM
                Log.d(TAG, "Both locations geocoded! Fetching routes with OSRM (FREE)...");
                OSRMRouteHelper.fetchRoute(startLatLng, endLatLng, new OSRMRouteHelper.RouteCallback() {
                    @Override
                    public void onRouteFetched(List<RouteData> routes) {
                        Log.d(TAG, "Routes fetched: " + routes.size() + " routes");
                        runOnUiThread(() -> {
                            searchButton.setEnabled(true);
                            searchButton.setText("Search");
                            showRouteSelectionDialogWithRoutes(routes);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Route fetch error: " + error);
                        runOnUiThread(() -> {
                            Toast.makeText(next_form.this, "Error fetching routes: " + error,
                                    Toast.LENGTH_LONG).show();
                            searchButton.setEnabled(true);
                            searchButton.setText("Search");
                        });
                    }
                });
            });
        });
    }

    private void showRouteSelectionDialogWithRoutes(List<RouteData> routes) {
        if (routes.isEmpty()) {
            Toast.makeText(this, "No routes found", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Showing dialog with " + routes.size() + " routes");

        ChooseMapRouteDialog dialog = ChooseMapRouteDialog.newInstanceWithRoutes(routes);
        dialog.setRouteSelectionListener(selectedRoute -> {
            // Store the selected route
            this.selectedRouteData = selectedRoute;
            Toast.makeText(this, "Route selected: " + selectedRoute.getSummary() +
                            " (" + selectedRoute.getDistance() + ")",
                    Toast.LENGTH_SHORT).show();
        });
        dialog.show(getSupportFragmentManager(), "ChooseMapRouteDialog");
    }

    private void validateAndSubmit() {
        // Validate license images
        if (frontLicenseUri == null) {
            Toast.makeText(this, "Please upload front side of license", Toast.LENGTH_SHORT).show();
            return;
        }

        if (backLicenseUri == null) {
            Toast.makeText(this, "Please upload back side of license", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate vehicle images
        if (vehicleImageUris.isEmpty()) {
            Toast.makeText(this, "Please upload at least one vehicle image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate route selection
        if (selectedRouteData == null) {
            Toast.makeText(this, "Please search and select a route", Toast.LENGTH_SHORT).show();
            return;
        }

        // All validations passed
        submitData();
    }

    private void submitData() {
        showLoading(true);

        // Upload all images to Supabase
        List<String> uploadedUrls = new ArrayList<>();

        // Step 1: Upload front license
        SupabaseHelper.uploadImage(this, frontLicenseUri)
                .thenCompose(frontUrl -> {
                    uploadedUrls.add(frontUrl);
                    Log.d(TAG, "Front license uploaded: " + frontUrl);
                    // Step 2: Upload back license
                    return SupabaseHelper.uploadImage(this, backLicenseUri);
                })
                .thenCompose(backUrl -> {
                    uploadedUrls.add(backUrl);
                    Log.d(TAG, "Back license uploaded: " + backUrl);
                    // Step 3: Upload vehicle images
                    return uploadVehicleImages();
                })
                .thenAccept(vehicleUrls -> {
                    Log.d(TAG, "All images uploaded! Updating Firestore...");
                    // All images uploaded successfully
                    updateFirestore(uploadedUrls.get(0), uploadedUrls.get(1), vehicleUrls);
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Log.e(TAG, "Image upload failed: " + e.getMessage());
                        Toast.makeText(this, "Failed to upload images: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                    return null;
                });
    }

    private java.util.concurrent.CompletableFuture<List<String>> uploadVehicleImages() {
        java.util.concurrent.CompletableFuture<List<String>> future =
                new java.util.concurrent.CompletableFuture<>();

        List<String> vehicleUrls = new ArrayList<>();
        uploadVehicleImageRecursive(0, vehicleUrls, future);

        return future;
    }

    private void uploadVehicleImageRecursive(int index, List<String> vehicleUrls,
                                             java.util.concurrent.CompletableFuture<List<String>> future) {
        if (index >= vehicleImageUris.size()) {
            future.complete(vehicleUrls);
            return;
        }

        SupabaseHelper.uploadImage(this, vehicleImageUris.get(index))
                .thenAccept(url -> {
                    vehicleUrls.add(url);
                    Log.d(TAG, "Vehicle image " + (index + 1) + " uploaded: " + url);
                    uploadVehicleImageRecursive(index + 1, vehicleUrls, future);
                })
                .exceptionally(e -> {
                    future.completeExceptionally(e);
                    return null;
                });
    }

    private void updateFirestore(String frontLicenseUrl, String backLicenseUrl,
                                 List<String> vehicleImageUrls) {
        if (driverId == null) {
            showLoading(false);
            Toast.makeText(this, "Error: Driver ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

<<<<<<< HEAD
        // Create update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("frontLicenseUrl", frontLicenseUrl);
        updates.put("backLicenseUrl", backLicenseUrl);
        updates.put("vehicleImageUrls", vehicleImageUrls);
        updates.put("routeData", selectedRouteData.toMap());  // Store complete route with coordinates
        updates.put("updatedAt", System.currentTimeMillis());

        Log.d(TAG, "Updating Firestore for driver: " + driverId);

        // Update Firestore document
        firestore.collection(COLLECTION_NAME)
                .document(driverId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Log.d(TAG, "Driver data updated successfully!");
                    Toast.makeText(this, "Registration completed successfully!",
                            Toast.LENGTH_SHORT).show();

                    // Navigate to success screen or home
                    finish();
                    // Or navigate to another activity:
                    // Intent intent = new Intent(next_form.this, HomeActivity.class);
                    // startActivity(intent);
                    // finish();
=======
        // Create complete driver data map
        Map<String, Object> driverData = new HashMap<>();
        driverData.put("frontLicenseUrl", frontLicenseUrl);
        driverData.put("backLicenseUrl", backLicenseUrl);
        driverData.put("vehicleImageUrls", vehicleImageUrls);
        driverData.put("routeData", selectedRouteData.toMap());
        driverData.put("status", "pending");  // Set status to pending
        driverData.put("updatedAt", System.currentTimeMillis());
        driverData.put("submittedAt", System.currentTimeMillis());

        Log.d(TAG, "Updating Firestore for driver: " + driverId);
        Log.d(TAG, "Setting status to: pending");

        // Use SET with merge to update existing document
        firestore.collection(COLLECTION_NAME)
                .document(driverId)
                .set(driverData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Log.d(TAG, "Driver data updated successfully!");
                    Log.d(TAG, "Status set to pending in Firestore");

                    Toast.makeText(this, "Registration completed successfully!",
                            Toast.LENGTH_SHORT).show();

                    // Navigate to pending dashboard
                    String driverName = getIntent().getStringExtra("driver_name");
                    Intent intent = new Intent(next_form.this, driver_pending_dashboard.class);
                    intent.putExtra("driver_id", driverId);
                    if (driverName != null) {
                        intent.putExtra("driver_name", driverName);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
>>>>>>> 327c339 (Implement Driver Application Status Management and Conditional Navigation(refs #16))
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error updating driver data: " + e.getMessage());
<<<<<<< HEAD
=======
                    e.printStackTrace();
>>>>>>> 327c339 (Implement Driver Application Status Management and Conditional Navigation(refs #16))
                    Toast.makeText(this, "Failed to save data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            submitButton.setEnabled(!show);
            submitButton.setText(show ? "Uploading..." : "Submit");

            // Disable all inputs during upload
            frontLicenseImage.setEnabled(!show);
            backLicenseImage.setEnabled(!show);
            uploadImagesButton.setEnabled(!show);
            searchButton.setEnabled(!show);
            startPointInput.setEnabled(!show);
            endPointInput.setEnabled(!show);
        });
    }
}