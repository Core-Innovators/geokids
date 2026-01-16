package com.coreinnovators.geokids;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelectDriverActivity extends AppCompatActivity implements DriverAdapter.OnDriverClickListener {

    private static final String TAG = "SelectDriverActivity";
    private static final String DRIVERS_COLLECTION = "drivers";

    // UI Components
    private TextView titleText;
    private Chip locationChip;
    private ImageView filterIcon;
    private RecyclerView driversRecyclerView;
    private Button continueButton;

    // Data
    private FirebaseFirestore firestore;
    private DriverAdapter driverAdapter;
    private List<Driver> allDrivers;
    private String childId;
    private String parentId;
    private String childSchool;

    // Filter variables
    private String selectedVehicleType = "";
    private String selectedAC = "";
    private String selectedPickupLocation = "";
    private String selectedDropoffLocation = "";
    private String selectedSchoolName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_driver);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();

        // Get intent data
        Intent intent = getIntent();
        childId = intent.getStringExtra("child_id");
        parentId = intent.getStringExtra("parent_id");
        childSchool = intent.getStringExtra("child_school");

        Log.d(TAG, "Child ID: " + childId);
        Log.d(TAG, "Parent ID: " + parentId);
        Log.d(TAG, "Child School: " + childSchool);

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup listeners
        setupListeners();

        // Load drivers
        loadDrivers();
    }

    private void initializeViews() {
        titleText = findViewById(R.id.title_text);
        locationChip = findViewById(R.id.location_chip);
        filterIcon = findViewById(R.id.filter_icon);
        driversRecyclerView = findViewById(R.id.drivers_recycler_view);
        continueButton = findViewById(R.id.continue_button);

        // Set location chip if available
        if (childSchool != null && !childSchool.isEmpty()) {
            locationChip.setText(childSchool);
        }
    }

    private void setupRecyclerView() {
        driverAdapter = new DriverAdapter(this, this);
        driversRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        driversRecyclerView.setAdapter(driverAdapter);
        allDrivers = new ArrayList<>();
    }

    private void setupListeners() {
        // Filter icon click
        filterIcon.setOnClickListener(v -> showFilterDialog());

        // Location chip close icon click
        locationChip.setOnCloseIconClickListener(v -> {
            locationChip.setText("");
            selectedSchoolName = "";
            applyFilters();
        });

        // Continue button click
        continueButton.setOnClickListener(v -> {
            Driver selectedDriver = driverAdapter.getSelectedDriver();
            if (selectedDriver != null) {
                // Navigate to driver profile or next screen
                Intent intent = new Intent(SelectDriverActivity.this, view_driver_profile.class);
                intent.putExtra("driver_id", selectedDriver.getDriverId());
                intent.putExtra("child_id", childId);
                intent.putExtra("parent_id", parentId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select a driver", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDrivers() {
        firestore.collection(DRIVERS_COLLECTION)
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allDrivers.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Driver driver = document.toObject(Driver.class);
                        driver.setId(document.getId());  // Set document ID as driver ID
                        allDrivers.add(driver);
                        Log.d(TAG, "Driver loaded: " + driver.getFullName());
                    }

                    Log.d(TAG, "Total drivers loaded: " + allDrivers.size());
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading drivers: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to load drivers", Toast.LENGTH_SHORT).show();
                });
    }

    private void showFilterDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View filterView = getLayoutInflater().inflate(R.layout.dialog_driver_filters, null);
        bottomSheetDialog.setContentView(filterView);

        // Find filter views
        ImageView closeButton = filterView.findViewById(R.id.close_button);

        // Vehicle Type Spinner
        // AC/NON AC Spinner
        // Pickup Location Spinner
        // Dropoff Location Spinner
        // School Name Spinner

        Button applyFiltersButton = filterView.findViewById(R.id.apply_filters_button);

        // Close button
        closeButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // Apply filters button
        applyFiltersButton.setOnClickListener(v -> {
            // Get selected filter values from spinners
            // For now, just apply filters and dismiss
            applyFilters();
            bottomSheetDialog.dismiss();
            Toast.makeText(this, "Filters applied", Toast.LENGTH_SHORT).show();
        });

        bottomSheetDialog.show();
    }

    private void applyFilters() {
        List<Driver> filteredDrivers = new ArrayList<>();

        for (Driver driver : allDrivers) {
            boolean matches = true;

            // Apply school filter if set
            if (!selectedSchoolName.isEmpty()) {
                // You can add school matching logic here
                // For now, we'll keep all drivers
            }

            // Apply other filters as needed
            // Vehicle type, AC, locations, etc.

            if (matches) {
                filteredDrivers.add(driver);
            }
        }

        Log.d(TAG, "Filtered drivers: " + filteredDrivers.size());
        driverAdapter.setDriverList(filteredDrivers);
    }

    @Override
    public void onDriverClick(Driver driver, int position) {
        Log.d(TAG, "Driver clicked: " + driver.getFullName());
        // Driver is already selected in the adapter
        // You can add additional logic here if needed
    }
}