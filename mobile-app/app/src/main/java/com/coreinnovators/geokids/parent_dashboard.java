package com.coreinnovators.geokids;

<<<<<<< HEAD
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
=======
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
>>>>>>> 5670744 (Parent functions)

public class parent_dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);
<<<<<<< HEAD
=======

        // Find the button by its ID
        MaterialButton addChildButton = findViewById(R.id.add_child);

        // Set an OnClickListener
        addChildButton.setOnClickListener(v -> {
            // Navigate to AddChild activity
            Intent intent = new Intent(parent_dashboard.this, AddChild.class);
            startActivity(intent);
        });
>>>>>>> 5670744 (Parent functions)
    }
}