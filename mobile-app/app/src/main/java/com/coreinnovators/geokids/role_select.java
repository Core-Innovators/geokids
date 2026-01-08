package com.coreinnovators.geokids;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class role_select extends AppCompatActivity {

    private ImageButton parentBtn, driverBtn;
    private Button continueBtn;
    private FirebaseFirestore db;
    private String selectedRole = null;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_select);

        db = FirebaseFirestore.getInstance();

        // Get UID from Intent
        uid = getIntent().getStringExtra("uid");
        if (uid == null) {
            Toast.makeText(this, "UID missing!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        parentBtn = findViewById(R.id.parent);
        driverBtn = findViewById(R.id.driver);
        continueBtn = findViewById(R.id.conti);

        parentBtn.setOnClickListener(v -> {
            selectedRole = "Parent";
            parentBtn.setAlpha(1f);
            driverBtn.setAlpha(0.5f);
        });

        driverBtn.setOnClickListener(v -> {
            selectedRole = "Driver";
            driverBtn.setAlpha(1f);
            parentBtn.setAlpha(0.5f);
        });

        continueBtn.setOnClickListener(v -> {
            if (selectedRole == null) {
                Toast.makeText(this, "Please select a role!", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> roleMap = new HashMap<>();
            roleMap.put("role", selectedRole);

            db.collection("users").document(uid)
                    .update(roleMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(role_select.this, "Role saved: " + selectedRole, Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(role_select.this, login.class));

                        // Navigate based on role
                        Intent intent;
                        if (selectedRole.equals("Parent")) {
                            intent = new Intent(role_select.this, parent_dashboard.class);
                        } else {
                            // Navigate to driver dashboard or login
                            intent = new Intent(role_select.this, login.class);
                        }
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(role_select.this, "Error saving role: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

}

}
