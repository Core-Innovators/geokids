package com.coreinnovators.geokids;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class SelectDriverActivity extends AppCompatActivity {

    private Spinner driverSpinner;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_driver);

        driverSpinner = findViewById(R.id.driverSpinner);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Mock data for drivers - replace this with data from your database/API
        List<String> drivers = new ArrayList<>();
        drivers.add("Select a Driver...");
        drivers.add("John Smith - License: XYZ-123");
        drivers.add("Sarah Jenkins - License: ABC-789");
        drivers.add("Michael Brown - License: LMN-456");

        // Set up the adapter for the Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, drivers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        driverSpinner.setAdapter(adapter);

        // Set up the button click listener
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedDriver = driverSpinner.getSelectedItem().toString();

                if (driverSpinner.getSelectedItemPosition() == 0) {
                    Toast.makeText(SelectDriverActivity.this,
                            "Please select a valid driver", Toast.LENGTH_SHORT).show();
                } else {
                    // Logic to save the selection or proceed to the next screen
                    Toast.makeText(SelectDriverActivity.this,
                            "Driver Selected: " + selectedDriver, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
