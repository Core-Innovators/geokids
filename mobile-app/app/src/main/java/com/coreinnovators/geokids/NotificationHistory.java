package com.coreinnovators.geokids;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationHistory extends AppCompatActivity {

    private static final String TAG = "NotificationHistory";

    private RecyclerView notificationRecycler;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList = new ArrayList<>();
    private ImageView backButton;
    private TextView emptyView;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_history);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupRecyclerView();
        loadNotifications();
    }

    private void initializeViews() {
        notificationRecycler = findViewById(R.id.notification_recycler);
        backButton = findViewById(R.id.back_button);
        emptyView = findViewById(R.id.empty_view);

        backButton.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notificationList);
        notificationRecycler.setLayoutManager(new LinearLayoutManager(this));
        notificationRecycler.setAdapter(adapter);
    }

    private void loadNotifications() {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "User not logged in");
            return;
        }

        String parentId = auth.getCurrentUser().getUid();

        db.collection("notifications")
                .whereEqualTo("parentId", parentId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyView();
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        NotificationItem item = new NotificationItem();
                        item.id = document.getId();
                        item.title = document.getString("title");
                        item.message = document.getString("message");
                        item.type = document.getString("type");
                        item.childName = document.getString("childName");
                        item.driverName = document.getString("driverName");
                        item.read = document.getBoolean("read") != null &&
                                document.getBoolean("read");

                        // Handle timestamp
                        if (document.contains("createdAt")) {
                            Long timestamp = document.getLong("createdAt");
                            if (timestamp != null) {
                                item.timestamp = timestamp;
                            }
                        }

                        notificationList.add(item);
                    }

                    if (notificationList.isEmpty()) {
                        showEmptyView();
                    } else {
                        hideEmptyView();
                        adapter.notifyDataSetChanged();

                        // Mark all as read
                        markAllAsRead();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading notifications: " + e.getMessage());
                    Toast.makeText(this, "Failed to load notifications",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void markAllAsRead() {
        String parentId = auth.getCurrentUser().getUid();

        db.collection("notifications")
                .whereEqualTo("parentId", parentId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().update("read", true);
                    }
                });
    }

    private void showEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        notificationRecycler.setVisibility(View.GONE);
    }

    private void hideEmptyView() {
        emptyView.setVisibility(View.GONE);
        notificationRecycler.setVisibility(View.VISIBLE);
    }

    private static class NotificationItem {
        String id;
        String title;
        String message;
        String type;
        String childName;
        String driverName;
        long timestamp;
        boolean read;
    }

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

        private List<NotificationItem> notifications;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());

        NotificationAdapter(List<NotificationItem> notifications) {
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            NotificationItem item = notifications.get(position);

            holder.titleTv.setText(item.title);
            holder.messageTv.setText(item.message);
            holder.timeTv.setText(dateFormat.format(new Date(item.timestamp)));

            // Set icon based on type
            if ("pickup".equals(item.type)) {
                holder.iconIv.setImageResource(R.drawable.home);
                holder.iconIv.setColorFilter(getResources().getColor(R.color.orange));
            } else if ("dropoff".equals(item.type)) {
                holder.iconIv.setImageResource(R.drawable.location);
                holder.iconIv.setColorFilter(getResources().getColor(R.color.orange));
            }

            // Show unread indicator
            holder.unreadIndicator.setVisibility(item.read ? View.GONE : View.VISIBLE);

            // Background color for unread
            if (!item.read) {
                holder.itemView.setBackgroundColor(getResources().getColor(R.color.light_gray));
            } else {
                holder.itemView.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        class NotificationViewHolder extends RecyclerView.ViewHolder {
            TextView titleTv, messageTv, timeTv;
            ImageView iconIv;
            View unreadIndicator;

            NotificationViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTv = itemView.findViewById(R.id.notification_title);
                messageTv = itemView.findViewById(R.id.notification_message);
                timeTv = itemView.findViewById(R.id.notification_time);
                iconIv = itemView.findViewById(R.id.notification_icon);
                unreadIndicator = itemView.findViewById(R.id.unread_indicator);
            }
        }
    }
}