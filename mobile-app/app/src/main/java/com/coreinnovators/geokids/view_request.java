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
        loadPendingRequests();
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

    private void loadPendingRequests() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            showNoRequests();
            return;
        }

        String driverId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Loading requests for driver: " + driverId);

        // Query ride requests where status is "pending" and assigned to this driver
        db.collection("rideRequests")
                .whereEqualTo("status", "pending")
                .whereEqualTo("driverId", driverId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    requestList.clear();

                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " requests");

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        RideRequest request = new RideRequest();
                        request.setRequestId(document.getId());
                        request.setPickupLocation(document.getString("pickupLocation"));
                        request.setDropoffLocation(document.getString("dropoffLocation"));
                        request.setParentName(document.getString("parentName"));
                        request.setChildName(document.getString("childName"));
                        request.setStatus(document.getString("status"));

                        Log.d(TAG, "Request: " + request.getPickupLocation() + " to " +
                                request.getDropoffLocation() + " for " + request.getChildName());

                        requestList.add(request);
                    }

                    requestAdapter.notifyDataSetChanged();

                    if (requestList.isEmpty()) {
                        showNoRequests();
                    } else {
                        showRequests();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading requests: " + e.getMessage());
                    Toast.makeText(this, "Failed to load requests", Toast.LENGTH_SHORT).show();
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
        updateRequestStatus(request.getRequestId(), "accepted", position);
    }

    private void onRejectRequest(RideRequest request, int position) {
        updateRequestStatus(request.getRequestId(), "rejected", position);
    }

    private void updateRequestStatus(String requestId, String status, int position) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection("rideRequests").document(requestId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    String message = status.equals("accepted") ? "Request accepted" : "Request rejected";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                    // Remove the item from list
                    requestList.remove(position);
                    requestAdapter.notifyItemRemoved(position);

                    // Check if list is now empty
                    if (requestList.isEmpty()) {
                        showNoRequests();
                    }

                    Log.d(TAG, "Request " + requestId + " updated to " + status);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating request: " + e.getMessage());
                    Toast.makeText(this, "Failed to update request", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingRequests();
    }
}