package com.coreinnovators.geokids;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class view_request extends AppCompatActivity {

    private static final String TAG = "ViewRequest";

    private RecyclerView requestsRecyclerView;
    private RequestAdapter requestAdapter;
    private List<RideRequest> requestList;
    private ImageView backButton;
    private TextView noRequestsText;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Load requests
        loadAssignedRequests();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        requestsRecyclerView = findViewById(R.id.requests_recycler_view);
        noRequestsText = findViewById(R.id.no_requests_text);

        // Set up RecyclerView
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestList = new ArrayList<>();
        requestAdapter = new RequestAdapter(requestList, this::onAcceptRequest, this::onRejectRequest);
        requestsRecyclerView.setAdapter(requestAdapter);

        // Back button listener
        backButton.setOnClickListener(v -> finish());
    }

    private void loadAssignedRequests() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            showNoRequests();
            return;
        }

        String driverId = auth.getCurrentUser().getUid();
        Log.d(TAG, "===== STARTING REQUEST LOAD =====");
        Log.d(TAG, "Driver ID: " + driverId);

        // Query children where assignedDriver.driverId equals current driver ID
        // and assignedDriver.status equals "assigned"
        db.collection("children")
                .whereEqualTo("assignedDriver.driverId", driverId)
                .whereEqualTo("assignedDriver.status", "assigned")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    requestList.clear();

                    Log.d(TAG, "Query successful! Found " + queryDocumentSnapshots.size() + " documents");

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d(TAG, "--- Processing Document ---");
                        Log.d(TAG, "Document ID: " + document.getId());

                        // Get all data from document
                        Map<String, Object> data = document.getData();
                        Log.d(TAG, "All fields: " + data.keySet());

                        RideRequest request = new RideRequest();
                        request.setRequestId(document.getId());

                        // Set pickup and dropoff
                        String pickupAddress = document.getString("pickupAddress");
                        String dropoffAddress = document.getString("dropoffAddress");
                        Log.d(TAG, "Pickup: " + pickupAddress);
                        Log.d(TAG, "Dropoff: " + dropoffAddress);

                        request.setPickupAddress(pickupAddress);
                        request.setDropoffAddress(dropoffAddress);

                        // Set parent info
                        request.setParentName(document.getString("parentName"));
                        request.setParentId(document.getString("parentId"));
                        request.setParentContact1(document.getString("parentContact1"));
                        request.setParentContact2(document.getString("parentContact2"));
                        request.setParentNic(document.getString("parentNic"));

                        // Set child info
                        String childName = document.getString("childName");
                        Log.d(TAG, "Child Name: " + childName);

                        request.setChildName(childName);
                        request.setChildAge(document.getString("childAge"));
                        request.setChildGrade(document.getString("childGrade"));
                        request.setChildSchool(document.getString("childSchool"));
                        request.setChildProfileImageUrl(document.getString("childProfileImageUrl"));

                        // Set status (main status is "active", but we care about assignedDriver.status)
                        request.setStatus(document.getString("status"));

                        // Get driver info from assignedDriver map
                        Map<String, Object> assignedDriver = (Map<String, Object>) document.get("assignedDriver");
                        if (assignedDriver != null) {
                            Log.d(TAG, "AssignedDriver map: " + assignedDriver);
                            request.setDriverId((String) assignedDriver.get("driverId"));
                            request.setDriverName((String) assignedDriver.get("driverName"));
                        }

                        // Handle timestamps
                        Long createdAt = document.getLong("createdAt");
                        Long updatedAt = document.getLong("updatedAt");
                        if (createdAt != null) request.setCreatedAt(createdAt);
                        if (updatedAt != null) request.setUpdatedAt(updatedAt);

                        Log.d(TAG, "✓ Request added successfully!");
                        Log.d(TAG, "Route: " + request.getRouteText());

                        requestList.add(request);
                    }

                    Log.d(TAG, "===== TOTAL REQUESTS: " + requestList.size() + " =====");

                    requestAdapter.notifyDataSetChanged();

                    if (requestList.isEmpty()) {
                        showNoRequests();
                        Log.d(TAG, "Showing NO REQUESTS message");
                    } else {
                        showRequests();
                        Log.d(TAG, "Showing " + requestList.size() + " requests in UI");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "===== QUERY FAILED =====");
                    Log.e(TAG, "Error: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load requests: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    showNoRequests();
                });
    }

    private void showNoRequests() {
        noRequestsText.setVisibility(View.VISIBLE);
        requestsRecyclerView.setVisibility(View.GONE);
    }

    private void showRequests() {
        noRequestsText.setVisibility(View.GONE);
        requestsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void onAcceptRequest(RideRequest request, int position) {
        // When driver accepts, update status to "accepted"
        // Child remains assigned to this driver
        updateRequestStatus(request, "accepted", position, true);
    }

    private void onRejectRequest(RideRequest request, int position) {
        // When driver rejects, update status to "rejected"
        // Remove driver assignment so it can be reassigned
        updateRequestStatus(request, "rejected", position, false);
    }

    private void updateRequestStatus(RideRequest request, String newStatus, int position, boolean keepDriverAssignment) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String driverId = auth.getCurrentUser().getUid();
        String childId = request.getRequestId();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("updatedAt", System.currentTimeMillis());

        if (!keepDriverAssignment) {
            // When rejecting - Remove driver assignment
            childUpdates.put("assignedDriver", FieldValue.delete());

            // Also remove childId from driver's assignedChildren array
            db.collection("drivers").document(driverId)
                    .update("assignedChildren", FieldValue.arrayRemove(childId))
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Child removed from driver's assignedChildren array");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error removing child from driver array: " + e.getMessage());
                    });

        } else {
            // When accepting - Update assignedDriver status
            Map<String, Object> assignedDriver = new HashMap<>();
            assignedDriver.put("driverId", driverId);
            assignedDriver.put("driverName", request.getDriverName());
            assignedDriver.put("status", newStatus);
            assignedDriver.put("assignedAt", FieldValue.serverTimestamp());

            childUpdates.put("assignedDriver", assignedDriver);

            // Add childId to driver's assignedChildren array
            db.collection("drivers").document(driverId)
                    .update("assignedChildren", FieldValue.arrayUnion(childId))
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Child added to driver's assignedChildren array");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding child to driver array: " + e.getMessage());
                        // If the field doesn't exist, create it
                        Map<String, Object> driverData = new HashMap<>();
                        List<String> assignedChildren = new ArrayList<>();
                        assignedChildren.add(childId);
                        driverData.put("assignedChildren", assignedChildren);

                        db.collection("drivers").document(driverId)
                                .set(driverData, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener(aVoid2 -> {
                                    Log.d(TAG, "Created assignedChildren field and added child");
                                })
                                .addOnFailureListener(e2 -> {
                                    Log.e(TAG, "Error creating assignedChildren field: " + e2.getMessage());
                                });
                    });
        }

        // Update child document
        db.collection("children").document(childId)
                .update(childUpdates)
                .addOnSuccessListener(aVoid -> {
                    String message = newStatus.equals("accepted")
                            ? "Request accepted! Child assigned to you."
                            : "Request rejected";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                    // Remove the item from list
                    requestList.remove(position);
                    requestAdapter.notifyItemRemoved(position);

                    // Check if list is now empty
                    if (requestList.isEmpty()) {
                        showNoRequests();
                    }

                    Log.d(TAG, "Request " + childId + " updated to " + newStatus);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating request: " + e.getMessage());
                    Toast.makeText(this, "Failed to update request: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAssignedRequests();
    }
}