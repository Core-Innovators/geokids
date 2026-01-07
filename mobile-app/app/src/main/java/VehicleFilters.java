import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.coreinnovators.geokids.R;

public class VehicleFilters extends AppCompatActivity {

    private Spinner spinVehicleType, spinAcType, spinPickUp, spinDropOff, spinSchool;
    private Button btnApply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_filters);

        // Initialize UI elements
        spinVehicleType = findViewById(R.id.spinVehicleType);
        spinAcType = findViewById(R.id.spinAcType);
        spinPickUp = findViewById(R.id.spinPickUp);
        spinDropOff = findViewById(R.id.spinDropOff);
        spinSchool = findViewById(R.id.spinSchool);
        btnApply = findViewById(R.id.btnApply);

        // Populate Spinners with data
        setupSpinner(spinVehicleType, new String[]{"Vehicle Type", "Van", "Car", "Bus"});
        setupSpinner(spinAcType, new String[]{"AC/NON AC", "AC", "Non-AC"});
        setupSpinner(spinPickUp, new String[]{"Pick up Location", "Colombo", "Kandy", "Galle"});
        setupSpinner(spinDropOff, new String[]{"Drop off Location", "School A", "School B"});
        setupSpinner(spinSchool, new String[]{"School Name", "Royal College", "Ananda College"});

        // Button Logic
        btnApply.setOnClickListener(v -> {
            String selected = spinVehicleType.getSelectedItem().toString();
            if (selected.equals("Vehicle Type")) {
                Toast.makeText(this, "Please select a vehicle type", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Filters Applied Successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinner(Spinner spinner, String[] data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, data);
        spinner.setAdapter(adapter);
    }
}