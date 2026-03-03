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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        previewView = findViewById(R.id.previewView);

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);

        cameraExecutor = Executors.newSingleThreadExecutor();

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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this,
                        "Camera permission is required to scan QR codes",
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
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor,
                imageProxy -> processImageProxy(imageProxy));

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Camera binding failed: " + e.getMessage());
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImageProxy(ImageProxy imageProxy) {
        @ExperimentalGetImage
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null && isScanning) {
            InputImage image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.getImageInfo().getRotationDegrees());

            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();
                            if (rawValue != null) {
                                isScanning = false;
                                handleQRCode(rawValue);
                                break;
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Barcode scanning failed: " + e.getMessage()))
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  QR HANDLING
    // ─────────────────────────────────────────────────────────────────

    private void handleQRCode(String qrData) {
        try {
            JSONObject jsonData = new JSONObject(qrData);

            String type = jsonData.optString("type", "");
            if (!type.equals("GEOKIDS_CHILD")) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_SHORT).show();
                    isScanning = true;
                });
                return;
            }

            String childId  = jsonData.getString("childId");
            String driverId = jsonData.optString("driverId", "");

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

        db.collection("drivers").document(currentDriverId)
                .get()
                .addOnSuccessListener(driverDoc -> {
                    if (driverDoc.exists()) {
                        List<String> assignedChildren =
                                (List<String>) driverDoc.get("assignedChildren");

                        if (assignedChildren != null
                                && assignedChildren.contains(childId)) {
                            loadChildDetails(childId, currentDriverId);
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(this,
                                        "This child is not assigned to you!",
                                        Toast.LENGTH_LONG).show();
                                isScanning = true;
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this,
                                    "Driver data not found",
                                    Toast.LENGTH_SHORT).show();
                            isScanning = true;
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error verifying assignment: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(this,
                                "Error verifying assignment",
                                Toast.LENGTH_SHORT).show();
                        isScanning = true;
                    });
                });
    }

    private void loadChildDetails(String childId, String driverId) {
        db.collection("children").document(childId)
                .get()
                .addOnSuccessListener(childDoc -> {
                    if (childDoc.exists()) {
                        // Mark child as picked up in the active trip
                        markChildPickedUpInTrip(childId, driverId, childDoc);
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this,
                                    "Child data not found",
                                    Toast.LENGTH_SHORT).show();
                            isScanning = true;
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading child: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(this,
                                "Error loading child data",
                                Toast.LENGTH_SHORT).show();
                        isScanning = true;
                    });
                });
    }

    /**
     * Adds the childId to the active trip's pickedUpChildren array
     * and removes it from pendingChildren.
     * Then proceeds to confirm_child screen.
     */
    private void markChildPickedUpInTrip(String childId, String driverId,
                                         DocumentSnapshot childDoc) {
        db.collection("trips")
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("status", "active")
                .limit(1)
                .get()
                .addOnSuccessListener(tripQuery -> {
                    if (!tripQuery.isEmpty()) {
                        String tripDocId = tripQuery.getDocuments().get(0).getId();

                        Map<String, Object> update = new HashMap<>();
                        // Add to pickedUpChildren (arrayUnion prevents duplicates)
                        db.collection("trips").document(tripDocId)
                                .update(
                                        "pickedUpChildren",
                                        FieldValue.arrayUnion(childId),
                                        "pendingChildren",
                                        FieldValue.arrayRemove(childId))
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Child marked picked up in trip: "
                                            + childId);
                                    navigateToConfirmChild(childId, childDoc);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to update trip: " + e.getMessage());
                                    // Still navigate, just log the error
                                    navigateToConfirmChild(childId, childDoc);
                                });
                    } else {
                        // No active trip – navigate anyway
                        Log.w(TAG, "No active trip found; skipping trip update.");
                        navigateToConfirmChild(childId, childDoc);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Trip query failed: " + e.getMessage());
                    navigateToConfirmChild(childId, childDoc);
                });
    }

    private void navigateToConfirmChild(String childId, DocumentSnapshot childDoc) {
        Intent intent = new Intent(QR_scan.this, confirm_child.class);
        intent.putExtra("childId",              childId);
        intent.putExtra("childName",            childDoc.getString("childName"));
        intent.putExtra("childAge",             childDoc.getString("childAge"));
        intent.putExtra("childGrade",           childDoc.getString("childGrade"));
        intent.putExtra("childSchool",          childDoc.getString("childSchool"));
        intent.putExtra("childProfileImageUrl", childDoc.getString("childProfileImageUrl"));
        intent.putExtra("parentName",           childDoc.getString("parentName"));
        intent.putExtra("parentId",             childDoc.getString("parentId"));
        startActivity(intent);
        finish();
    }

    // ─────────────────────────────────────────────────────────────────
    //  LIFECYCLE
    // ─────────────────────────────────────────────────────────────────

    @Override
    protected void onPause()  { super.onPause();  isScanning = false; }

    @Override
    protected void onResume() { super.onResume(); isScanning = true;  }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor  != null) cameraExecutor.shutdown();
        if (barcodeScanner  != null) barcodeScanner.close();
    }
}