package com.coreinnovators.geokids;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ParentProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_profile);

        // Initialize buttons based on Source [1]
        setupButton(R.id.btnAddChild, "Navigate to Add Child");
        setupButton(R.id.btnChooseDriver, "Navigate to Driver Selection");
        setupButton(R.id.btnUpdatePickup, "Navigate to Pickup Update");
        setupButton(R.id.btnUpdateDetails, "Navigate to Details Update");
        setupButton(R.id.btnInformAbsence, "Navigate to Absence Reporting");
        setupButton(R.id.btnContactAdmin, "Navigate to Support");

        // Special handling for Delete Account
        Button btnDelete = findViewById(R.id.btnDeleteAccount);
        btnDelete.setOnClickListener(v -> {
            // Logic for account deletion
            Toast.makeText(this, "Delete Account clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupButton(int id, final String message) {
        findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // In a real app, you would use:
                // startActivity(new Intent(ParentProfileActivity.this, TargetActivity.class));
                Toast.makeText(ParentProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}