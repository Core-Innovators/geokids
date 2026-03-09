package com.coreinnovators.geokids;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectDriverActivity extends AppCompatActivity implements DriverAdapter.OnDriverClickListener {

    private static final String TAG = "SelectDriverActivity";
    private static final String DRIVERS_COLLECTION = "drivers";

    // UI Components
    private TextView titleText;
    private Chip locationChip;
    private LinearLayout filterIcon;
    private RecyclerView driversRecyclerView;
    private Button continueButton;
    private LinearLayout selectedDriverPreview;
    private CircleImageView selectedDriverAvatar;
    private TextView selectedDriverName;
    private TextView driverCountLabel;

    // Data
    private FirebaseFirestore firestore;
    private DriverAdapter driverAdapter;
    private List<Driver> allDrivers;
    private String childId;
    private String parentId;
    private String childSchool;
    private String selectedSchoolName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_driver);

        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        childId = intent.getStringExtra("child_id");
        parentId = intent.getStringExtra("parent_id");
        childSchool = intent.getStringExtra("child_school");

        Log.d(TAG, "Child ID: " + childId);
        Log.d(TAG, "Parent ID: " + parentId);

        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadDrivers();
    }

    private void initializeViews() {
        titleText = findViewById(R.id.title_text);
        locationChip = findViewById(R.id.location_chip);
        filterIcon = findViewById(R.id.filter_icon);
        driversRecyclerView = findViewById(R.id.drivers_recycler_view);
        continueButton = findViewById(R.id.continue_button);
        selectedDriverPreview = findViewById(R.id.selected_driver_preview);
        selectedDriverAvatar = findViewById(R.id.selected_driver_avatar);
        selectedDriverName = findViewById(R.id.selected_driver_name);
        driverCountLabel = findViewById(R.id.driver_count_label);

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
        filterIcon.setOnClickListener(v -> showFilterDialog());

        locationChip.setOnCloseIconClickListener(v -> {
            locationChip.setText("");
            selectedSchoolName = "";
            applyFilters();
        });

        continueButton.setOnClickListener(v -> {
            Driver selectedDriver = driverAdapter.getSelectedDriver();
            if (selectedDriver == null) {
                Toast.makeText(this, "Please select a driver first", Toast.LENGTH_SHORT).show();
                driversRecyclerView.animate()
                        .translationX(-12f).setDuration(60).withEndAction(() ->
                                driversRecyclerView.animate()
                                        .translationX(12f).setDuration(60).withEndAction(() ->
                                                driversRecyclerView.animate()
                                                        .translationX(0f).setDuration(60).start()
                                        ).start()
                        ).start();
                return;
            }

            v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction(() ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).withEndAction(() -> {
                        Log.d(TAG, "Navigating to profile for: " + selectedDriver.getFullName());
                        Intent intent = new Intent(SelectDriverActivity.this, view_driver_profile.class);
                        intent.putExtra("driver_id", selectedDriver.getId());
                        intent.putExtra("child_id", childId);
                        intent.putExtra("parent_id", parentId);
                        startActivity(intent);
                        finish();
                    }).start()
            ).start();
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
                        driver.setId(document.getId());
                        allDrivers.add(driver);
                        Log.d(TAG, "Loaded: " + driver.getFullName() + " | " + driver.getId());
                    }
                    Log.d(TAG, "Total: " + allDrivers.size());

                    if (driverCountLabel != null) {
                        driverCountLabel.setText(allDrivers.size() + " Available Drivers");
                    }

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

        // ✅ Correct IDs matching dialog_driver_filters.xml
        ImageView closeButton = filterView.findViewById(R.id.close_btn);
        Button applyFiltersButton = filterView.findViewById(R.id.btnApplyFilters);
        Button resetFiltersButton = filterView.findViewById(R.id.btnResetFilters);

        closeButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        resetFiltersButton.setOnClickListener(v -> {
            // Clear chip selections
            com.google.android.material.chip.ChipGroup vehicleGroup =
                    filterView.findViewById(R.id.chipGroupVehicleType);
            com.google.android.material.chip.ChipGroup acGroup =
                    filterView.findViewById(R.id.chipGroupAC);
            if (vehicleGroup != null) vehicleGroup.clearCheck();
            if (acGroup != null) acGroup.clearCheck();
        });

        applyFiltersButton.setOnClickListener(v -> {
            applyFilters();
            bottomSheetDialog.dismiss();
            Toast.makeText(this, "Filters applied", Toast.LENGTH_SHORT).show();
        });

        bottomSheetDialog.show();
    }

    private void applyFilters() {
        List<Driver> filteredDrivers = new ArrayList<>(allDrivers);
        Log.d(TAG, "Filtered drivers: " + filteredDrivers.size());
        driverAdapter.setDriverList(filteredDrivers);
    }

    @Override
    public void onDriverClick(Driver driver, int position) {
        Log.d(TAG, "Driver selected: " + driver.getFullName());

        if (selectedDriverPreview != null) {
            selectedDriverName.setText(driver.getFullName());

            String imageUrl = driver.getProfileImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .circleCrop()
                        .into(selectedDriverAvatar);
            }

            if (selectedDriverPreview.getVisibility() == View.GONE) {
                selectedDriverPreview.setVisibility(View.VISIBLE);
                selectedDriverPreview.setAlpha(0f);
                selectedDriverPreview.setTranslationY(20f);
                selectedDriverPreview.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(250)
                        .start();
            }

            continueButton.setText("Continue with " + driver.getFullName().split(" ")[0]);
        }
    }
}