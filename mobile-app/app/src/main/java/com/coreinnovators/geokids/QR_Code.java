package com.coreinnovators.geokids;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QR_Code extends AppCompatActivity {

    private static final String TAG = "QR_Code";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private ImageView qrCodeImage, closeButton;
    private TextView childNameTv, statusMessageTv;
    private Button downloadButton, selectChildButton;
    private LinearLayout qrContainer, noQrContainer;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private List<ChildQRData> childrenWithQR = new ArrayList<>();
    private int currentChildIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupClickListeners();
        loadChildrenQRCodes();
    }

    private void initializeViews() {
        qrCodeImage = findViewById(R.id.qr_code_image);
        closeButton = findViewById(R.id.close_button);
        childNameTv = findViewById(R.id.child_name_tv);
        statusMessageTv = findViewById(R.id.status_message_tv);
        downloadButton = findViewById(R.id.download_button);
        selectChildButton = findViewById(R.id.select_child_button);
        qrContainer = findViewById(R.id.qr_container);
        noQrContainer = findViewById(R.id.no_qr_container);
    }

    private void setupClickListeners() {
        closeButton.setOnClickListener(v -> finish());

        downloadButton.setOnClickListener(v -> {
            if (checkPermission()) {
                downloadQRCode();
            } else {
                requestPermission();
            }
        });

        selectChildButton.setOnClickListener(v -> showChildSelectionDialog());
    }

    private void loadChildrenQRCodes() {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "User not logged in");
            showNoQRState("Please log in to view QR codes");
            return;
        }

        String parentId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Loading QR codes for parent: " + parentId);

        db.collection("children")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    childrenWithQR.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No children found");
                        showNoQRState("No children added yet");
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String qrCodeUrl = document.getString("qrCodeUrl");

                        // Only add children who have QR codes generated
                        if (qrCodeUrl != null && !qrCodeUrl.isEmpty()) {
                            ChildQRData child = new ChildQRData();
                            child.id = document.getId();
                            child.name = document.getString("childName");
                            child.qrCodeUrl = qrCodeUrl;
                            child.grade = document.getString("childGrade");
                            child.school = document.getString("childSchool");

                            childrenWithQR.add(child);
                            Log.d(TAG, "Child with QR: " + child.name + " - " + child.qrCodeUrl);
                        }
                    }

                    if (childrenWithQR.isEmpty()) {
                        showNoQRState("No QR codes generated yet.\nPlease wait for driver assignment.");
                    } else {
                        displayQRCode(0);

                        // Show select child button only if there are multiple children
                        if (selectChildButton != null) {
                            selectChildButton.setVisibility(
                                    childrenWithQR.size() > 1 ? View.VISIBLE : View.GONE
                            );
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading children: " + e.getMessage());
                    showNoQRState("Error loading QR codes");
                    Toast.makeText(this, "Failed to load QR codes", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayQRCode(int index) {
        if (index < 0 || index >= childrenWithQR.size()) return;

        currentChildIndex = index;
        ChildQRData child = childrenWithQR.get(index);

        // Show QR container, hide no QR container
        qrContainer.setVisibility(View.VISIBLE);
        noQrContainer.setVisibility(View.GONE);

        // Set child name
        if (childNameTv != null) {
            String displayName = child.name;
            if (child.grade != null) {
                displayName += " - " + child.grade;
            }
            childNameTv.setText(displayName);
        }

        // Set status message
        if (statusMessageTv != null) {
            statusMessageTv.setText("Your request for driver is success. You can now download your child QR code");
        }

        // Load QR code image from URL
        if (child.qrCodeUrl != null && !child.qrCodeUrl.isEmpty()) {
            Log.d(TAG, "Loading QR code from: " + child.qrCodeUrl);

            Glide.with(this)
                    .load(child.qrCodeUrl)
                    .placeholder(R.drawable.ic_qr_placeholder)
                    .error(R.drawable.ic_qr_placeholder)
                    .into(qrCodeImage);
        } else {
            qrCodeImage.setImageResource(R.drawable.ic_qr_placeholder);
        }
    }

    private void showChildSelectionDialog() {
        if (childrenWithQR.size() <= 1) return;

        String[] childNames = new String[childrenWithQR.size()];
        for (int i = 0; i < childrenWithQR.size(); i++) {
            childNames[i] = childrenWithQR.get(i).name;
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Child")
                .setItems(childNames, (dialog, which) -> {
                    displayQRCode(which);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showNoQRState(String message) {
        qrContainer.setVisibility(View.GONE);
        noQrContainer.setVisibility(View.VISIBLE);

        TextView noQrMessage = findViewById(R.id.no_qr_message);
        if (noQrMessage != null) {
            noQrMessage.setText(message);
        }
    }

    private void downloadQRCode() {
        if (childrenWithQR.isEmpty() || currentChildIndex >= childrenWithQR.size()) {
            Toast.makeText(this, "No QR code to download", Toast.LENGTH_SHORT).show();
            return;
        }

        ChildQRData currentChild = childrenWithQR.get(currentChildIndex);

        Toast.makeText(this, "Downloading QR code...", Toast.LENGTH_SHORT).show();

        Glide.with(this)
                .asBitmap()
                .load(currentChild.qrCodeUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource,
                                                @Nullable Transition<? super Bitmap> transition) {
                        saveImage(resource, currentChild.name);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Do nothing
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        Toast.makeText(QR_Code.this,
                                "Failed to download QR code", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveImage(Bitmap bitmap, String childName) {
        try {
            // Create a file name
            String fileName = "QR_" + childName.replaceAll("\\s+", "_") +
                    "_" + System.currentTimeMillis() + ".png";

            // Save to Downloads folder
            File downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File imageFile = new File(downloadsDir, fileName);

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Toast.makeText(this, "QR code saved to Downloads/" + fileName,
                    Toast.LENGTH_LONG).show();

            Log.d(TAG, "QR code saved: " + imageFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "Error saving QR code: " + e.getMessage());
            Toast.makeText(this, "Failed to save QR code", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10 and above - no permission needed for Downloads
            return true;
        } else {
            int result = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadQRCode();
            } else {
                Toast.makeText(this, "Permission denied. Cannot download QR code.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class ChildQRData {
        String id;
        String name;
        String qrCodeUrl;
        String grade;
        String school;
    }
}