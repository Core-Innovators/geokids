package com.coreinnovators.geokids;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TrackChildActivity extends AppCompatActivity {

    private Button btnContactDriver, btnEmergencyContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_child);

        btnContactDriver = findViewById(R.id.btnContactDriver);
        btnEmergencyContact = findViewById(R.id.btnEmergencyContact);

        // Logic for Contacting Driver [1]
        btnContactDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Example: Open dialer or chat
                Toast.makeText(TrackChildActivity.this, "Connecting to Driver...", Toast.LENGTH_SHORT).show();
            }
        });

        // Logic for Emergency Contact [1]
        btnEmergencyContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Example: Trigger immediate emergency call
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:911")); // Replace with actual emergency number
                startActivity(intent);
            }
        });
    }
}