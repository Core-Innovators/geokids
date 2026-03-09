package com.coreinnovators.geokids;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChildHistory extends AppCompatActivity {

    private static final String TAG = "ChildHistory";

    private RecyclerView historyRecycler;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyList = new ArrayList<>();
    private ImageView backButton;
    private TextView emptyView, titleText;
    private LinearLayout navHome, navLocation, navQr, navProfile;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Optional: filter by specific child
    private String filterChildId;
    private String filterChildName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_history);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if filtering by specific child
        if (getIntent() != null) {
            filterChildId = getIntent().getStringExtra("childId");
            filterChildName = getIntent().getStringExtra("childName");
        }

        initializeViews();
        setupRecyclerView();
        setupBottomNavigation();
        loadHistory();
    }

    private void initializeViews() {
        historyRecycler = findViewById(R.id.history_recycler);
        backButton = findViewById(R.id.back_button);
        emptyView = findViewById(R.id.empty_view);
        titleText = findViewById(R.id.title_text);

        // Bottom navigation
        navHome = findViewById(R.id.nav_home);
        navLocation = findViewById(R.id.nav_location);
        navQr = findViewById(R.id.nav_qr);
        navProfile = findViewById(R.id.nav_profile);

        backButton.setOnClickListener(v -> finish());

        // Update title if filtering by child
        if (filterChildName != null && !filterChildName.isEmpty()) {
            titleText.setText(filterChildName + "'s History");
        }
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter(historyList);
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyRecycler.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ChildHistory.this, ParentActiveDashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navLocation.setOnClickListener(v -> {
            Intent intent = new Intent(ChildHistory.this, TrackLocation.class);
            startActivity(intent);
        });

        navQr.setOnClickListener(v -> {
            // Already on history
            Toast.makeText(this, "Already on History", Toast.LENGTH_SHORT).show();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ChildHistory.this, parent_profile.class);
            startActivity(intent);
        });
    }

    private void loadHistory() {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "User not logged in");
            return;
        }

        String parentId = auth.getCurrentUser().getUid();

        // Load pickups
        loadPickups(parentId);
        // Load dropoffs
        loadDropoffs(parentId);
    }

    private void loadPickups(String parentId) {
        Query query = db.collection("pickups")
                .whereEqualTo("parentId", parentId);

        // Filter by child if specified
        if (filterChildId != null && !filterChildId.isEmpty()) {
            query = query.whereEqualTo("childId", filterChildId);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        HistoryItem item = new HistoryItem();
                        item.id = document.getId();
                        item.childName = document.getString("childName");
                        item.driverName = document.getString("driverName");
                        item.type = "pickup";

                        // Handle timestamp
                        Long timestamp = document.getLong("timestamp");
                        if (timestamp != null) {
                            item.timestamp = timestamp;
                        } else if (document.getLong("createdAt") != null) {
                            item.timestamp = document.getLong("createdAt");
                        }

                        historyList.add(item);
                    }

                    // Sort and update after both collections are loaded
                    sortAndUpdateList();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading pickups: " + e.getMessage());
                });
    }

    private void loadDropoffs(String parentId) {
        Query query = db.collection("dropoffs")
                .whereEqualTo("parentId", parentId);

        // Filter by child if specified
        if (filterChildId != null && !filterChildId.isEmpty()) {
            query = query.whereEqualTo("childId", filterChildId);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        HistoryItem item = new HistoryItem();
                        item.id = document.getId();
                        item.childName = document.getString("childName");
                        item.driverName = document.getString("driverName");
                        item.childSchool = document.getString("childSchool");
                        item.type = "dropoff";

                        // Handle timestamp
                        Long timestamp = document.getLong("timestamp");
                        if (timestamp != null) {
                            item.timestamp = timestamp;
                        } else if (document.getLong("createdAt") != null) {
                            item.timestamp = document.getLong("createdAt");
                        }

                        historyList.add(item);
                    }

                    // Sort and update after both collections are loaded
                    sortAndUpdateList();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading dropoffs: " + e.getMessage());
                });
    }

    private void sortAndUpdateList() {
        // Sort by timestamp descending (newest first)
        historyList.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

        if (historyList.isEmpty()) {
            showEmptyView();
        } else {
            hideEmptyView();
            adapter.notifyDataSetChanged();
        }
    }

    private void showEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        historyRecycler.setVisibility(View.GONE);
    }

    private void hideEmptyView() {
        emptyView.setVisibility(View.GONE);
        historyRecycler.setVisibility(View.VISIBLE);
    }

    // ─────────────────────────────────────────────────────────────────
    //  DATA MODEL
    // ─────────────────────────────────────────────────────────────────

    private static class HistoryItem {
        String id;
        String childName;
        String driverName;
        String childSchool;
        String type; // "pickup" or "dropoff"
        long timestamp;
    }

    // ─────────────────────────────────────────────────────────────────
    //  ADAPTER
    // ─────────────────────────────────────────────────────────────────

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

        private List<HistoryItem> items;
        private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        HistoryAdapter(List<HistoryItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new HistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
            HistoryItem item = items.get(position);

            // Show date header if it's the first item or different day from previous
            String currentDate = dateFormat.format(new Date(item.timestamp));
            if (position == 0) {
                holder.dateHeader.setVisibility(View.VISIBLE);
                holder.dateHeader.setText(getDateLabel(item.timestamp));
            } else {
                String previousDate = dateFormat.format(new Date(items.get(position - 1).timestamp));
                if (!currentDate.equals(previousDate)) {
                    holder.dateHeader.setVisibility(View.VISIBLE);
                    holder.dateHeader.setText(getDateLabel(item.timestamp));
                } else {
                    holder.dateHeader.setVisibility(View.GONE);
                }
            }

            // Set time
            holder.timeTv.setText(timeFormat.format(new Date(item.timestamp)));

            // Set icon and message based on type
            if ("pickup".equals(item.type)) {
                holder.statusDot.setBackgroundResource(R.drawable.dot_green);
                String message = (item.childName != null ? item.childName : "Child")
                        + " has been picked up by "
                        + (item.driverName != null ? item.driverName : "driver");
                holder.messageTv.setText(message);
                holder.typeLabelTv.setText("PICKUP");
                holder.typeLabelTv.setTextColor(ContextCompat.getColor(ChildHistory.this, R.color.green));
            } else {
                holder.statusDot.setBackgroundResource(R.drawable.dot_red);
                String message = (item.childName != null ? item.childName : "Child")
                        + " has been dropped off at "
                        + (item.childSchool != null ? item.childSchool : "school");
                holder.messageTv.setText(message);
                holder.typeLabelTv.setText("DROP-OFF");
                holder.typeLabelTv.setTextColor(ContextCompat.getColor(ChildHistory.this, R.color.orange));
            }
        }

        private String getDateLabel(long timestamp) {
            Calendar today = Calendar.getInstance();
            Calendar itemDate = Calendar.getInstance();
            itemDate.setTimeInMillis(timestamp);

            if (today.get(Calendar.YEAR) == itemDate.get(Calendar.YEAR)
                    && today.get(Calendar.DAY_OF_YEAR) == itemDate.get(Calendar.DAY_OF_YEAR)) {
                return "Today";
            }

            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            if (yesterday.get(Calendar.YEAR) == itemDate.get(Calendar.YEAR)
                    && yesterday.get(Calendar.DAY_OF_YEAR) == itemDate.get(Calendar.DAY_OF_YEAR)) {
                return "Yesterday";
            }

            return dateFormat.format(new Date(timestamp));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class HistoryViewHolder extends RecyclerView.ViewHolder {
            TextView dateHeader, timeTv, messageTv, typeLabelTv;
            View statusDot;

            HistoryViewHolder(@NonNull View itemView) {
                super(itemView);
                dateHeader = itemView.findViewById(R.id.date_header);
                timeTv = itemView.findViewById(R.id.history_time);
                messageTv = itemView.findViewById(R.id.history_message);
                typeLabelTv = itemView.findViewById(R.id.history_type_label);
                statusDot = itemView.findViewById(R.id.status_dot);
            }
        }
    }
}