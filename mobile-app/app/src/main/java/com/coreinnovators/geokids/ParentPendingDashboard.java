package com.coreinnovators.geokids;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ParentPendingDashboard extends AppCompatActivity {

    private static final String TAG = "ParentPendingDashboard";

    private TextView helloTv;
    private Button contactAdmin;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_pending_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        helloTv = findViewById(R.id.hello);
        contactAdmin = findViewById(R.id.contact_admin);

        loadParentName();

        contactAdmin.setOnClickListener(v -> {
            // Navigate to support/contact screen
            // You can implement email, phone, or chat support here
        });
    }

    private void loadParentName() {
        if (auth.getCurrentUser() == null) return;

        String parentId = auth.getCurrentUser().getUid();

        db.collection("users").document(parentId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null) {
                            helloTv.setText("Hello, " + name);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading parent name: " + e.getMessage());
                });
    }
}