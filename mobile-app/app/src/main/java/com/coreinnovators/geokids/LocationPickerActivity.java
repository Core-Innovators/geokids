package com.coreinnovators.geokids;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "LocationPickerActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLocation;
    private String selectedAddress = "";

    // UI Components
    private EditText searchLocationInput;
    private Button searchButton, confirmButton, currentLocationButton;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        // Initialize location client and geocoder
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        // Initialize views
        searchLocationInput = findViewById(R.id.search_location);
        searchButton = findViewById(R.id.search_button);
        confirmButton = findViewById(R.id.confirm_location_btn);
        currentLocationButton = findViewById(R.id.current_location_btn);

        // Setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Setup listeners
        setupListeners();

        // Check if location was passed
        double latitude = getIntent().getDoubleExtra("latitude", 0);
        double longitude = getIntent().getDoubleExtra("longitude", 0);
        if (latitude != 0 && longitude != 0) {
            selectedLocation = new LatLng(latitude, longitude);
        }
    }

    private void setupListeners() {
        // Search button click
        searchButton.setOnClickListener(v -> searchLocation());

        // Search on keyboard enter
        searchLocationInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                            event.getAction() == KeyEvent.ACTION_DOWN)) {
                searchLocation();
                return true;
            }
            return false;
        });

        // Current location button
        currentLocationButton.setOnClickListener(v -> moveToCurrentLocation());

        // Confirm button
        confirmButton.setOnClickListener(v -> confirmLocation());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false); // We have custom button

        // Set map click listener
        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            updateMapMarker(latLng);
            getAddressFromLocation(latLng);
        });

        // Check location permission and move to current location or default
        if (checkLocationPermission()) {
            if (selectedLocation != null) {
                // Move to previously selected location
                updateMapMarker(selectedLocation);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));
            } else {
                // Move to current location
                moveToCurrentLocation();
            }
        } else {
            requestLocationPermission();
            // Default location (Colombo, Sri Lanka)
            LatLng defaultLocation = new LatLng(6.9271, 79.8612);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
        }
    }

    private void searchLocation() {
        String searchQuery = searchLocationInput.getText().toString().trim();

        if (searchQuery.isEmpty()) {
            Toast.makeText(this, "Please enter a location to search",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Hide keyboard
        hideKeyboard();

        // Show loading message
        Toast.makeText(this, "Searching for: " + searchQuery, Toast.LENGTH_SHORT).show();

        // Search for location using Geocoder
        try {
            List<Address> addresses = geocoder.getFromLocationName(searchQuery, 5);

            if (addresses != null && !addresses.isEmpty()) {
                // Get first result
                Address address = addresses.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());

                // Update map and selected location
                selectedLocation = location;
                updateMapMarker(location);
                getAddressFromLocation(location);

                Toast.makeText(this, "Location found!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location not found. Please try a different search.",
                        Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding error: " + e.getMessage());
            Toast.makeText(this, "Error searching location. Please try again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void updateMapMarker(LatLng location) {
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Pickup Location")
                    .snippet("Tap confirm to select this location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
    }

    private void getAddressFromLocation(LatLng location) {
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.latitude, location.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressBuilder = new StringBuilder();

                // Build address string
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressBuilder.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        addressBuilder.append(", ");
                    }
                }

                selectedAddress = addressBuilder.toString();
                Log.d(TAG, "Address: " + selectedAddress);

                // Update search box with found address
                searchLocationInput.setText(selectedAddress);
            } else {
                selectedAddress = String.format("Lat: %.6f, Lng: %.6f",
                        location.latitude, location.longitude);
                searchLocationInput.setText(selectedAddress);
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder error: " + e.getMessage());
            selectedAddress = String.format("Lat: %.6f, Lng: %.6f",
                    location.latitude, location.longitude);
            searchLocationInput.setText(selectedAddress);
        }
    }

    private void moveToCurrentLocation() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude());
                            selectedLocation = currentLocation;
                            updateMapMarker(currentLocation);
                            getAddressFromLocation(currentLocation);
                            Toast.makeText(this, "Moved to current location",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Unable to get current location. Please enable GPS.",
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting location: " + e.getMessage());
                        Toast.makeText(this, "Error getting location: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void confirmLocation() {
        if (selectedLocation == null) {
            Toast.makeText(this, "Please select a location on the map",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Return result
        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitude", selectedLocation.latitude);
        resultIntent.putExtra("longitude", selectedLocation.longitude);
        resultIntent.putExtra("address", selectedAddress);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Location selected!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    moveToCurrentLocation();
                }
            } else {
                Toast.makeText(this, "Location permission denied. You can still search or tap on the map.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}