package com.coreinnovators.geokids;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QR_scan extends AppCompatActivity {

    private static final String TAG = "QRScan";
    private static final int CAMERA_PERMISSION_CODE = 100;

    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isScanning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        previewView = findViewById(R.id.previewView);

        // Initialize barcode scanner
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Check camera permission
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage());
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview(ProcessCameraProvider cameraProvider) {
        // Preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image analysis for QR scanning
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                processImageProxy(imageProxy);
            }
        });

        // Camera selector
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        try {
            cameraProvider.unbindAll();
            Camera camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Camera binding failed: " + e.getMessage());
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImageProxy(ImageProxy imageProxy) {
        @androidx.camera.core.ExperimentalGetImage
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null && isScanning) {
            InputImage image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.getImageInfo().getRotationDegrees()
            );

            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();
                            if (rawValue != null) {
                                isScanning = false; // Stop scanning
                                handleQRCode(rawValue);
                                break;
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Barcode scanning failed: " + e.getMessage());
                    })
                    .addOnCompleteListener(task -> {
                        imageProxy.close();
                    });
        } else {
            imageProxy.close();
        }
    }

    private void handleQRCode(String qrData) {
        try {
            JSONObject jsonData = new JSONObject(qrData);

            // Check if it's a valid GEOKIDS QR code
            String type = jsonData.optString("type", "");
            if (!type.equals("GEOKIDS_CHILD")) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_SHORT).show();
                    isScanning = true;
                });
                return;
            }

            String childId = jsonData.getString("childId");
            String driverId = jsonData.optString("driverId", "");

            // Verify if this child is assigned to the current driver
            verifyChildAssignment(childId, driverId);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing QR code: " + e.getMessage());
            runOnUiThread(() -> {
                Toast.makeText(this, "Invalid QR code data", Toast.LENGTH_SHORT).show();
                isScanning = true;
            });
        }
    }

    private void verifyChildAssignment(String childId, String qrDriverId) {
        String currentDriverId = auth.getCurrentUser().getUid();

        // First, check the driver's document to get assigned children
        db.collection("drivers").document(currentDriverId)
                .get()
                .addOnSuccessListener(driverDoc -> {
                    if (driverDoc.exists()) {
                        List<String> assignedChildren = (List<String>) driverDoc.get("assignedChildren");

                        if (assignedChildren != null && assignedChildren.contains(childId)) {
                            // Child is assigned to this driver, proceed to load child details
                            loadChildDetails(childId);
                        } else {
                            // Child is not assigned to this driver
                            runOnUiThread(() -> {
                                Toast.makeText(this,
                                        "This child is not assigned to you!",
                                        Toast.LENGTH_LONG).show();
                                isScanning = true;
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Driver data not found", Toast.LENGTH_SHORT).show();
                            isScanning = true;
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error verifying child assignment: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error verifying assignment", Toast.LENGTH_SHORT).show();
                        isScanning = true;
                    });
                });
    }

    private void loadChildDetails(String childId) {
        db.collection("children").document(childId)
                .get()
                .addOnSuccessListener(childDoc -> {
                    if (childDoc.exists()) {
                        // Navigate to confirmation screen with child data
                        Intent intent = new Intent(QR_scan.this, confirm_child.class);
                        intent.putExtra("childId", childId);
                        intent.putExtra("childName", childDoc.getString("childName"));
                        intent.putExtra("childAge", childDoc.getString("childAge"));
                        intent.putExtra("childGrade", childDoc.getString("childGrade"));
                        intent.putExtra("childSchool", childDoc.getString("childSchool"));
                        intent.putExtra("childProfileImageUrl", childDoc.getString("childProfileImageUrl"));
                        intent.putExtra("parentName", childDoc.getString("parentName"));
                        intent.putExtra("parentId", childDoc.getString("parentId"));
                        startActivity(intent);
                        finish();
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Child data not found", Toast.LENGTH_SHORT).show();
                            isScanning = true;
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading child details: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error loading child data", Toast.LENGTH_SHORT).show();
                        isScanning = true;
                    });
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isScanning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isScanning = true;
    }
}