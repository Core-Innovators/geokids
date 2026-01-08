package com.coreinnovators.geokids;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParentActiveDashboard extends AppCompatActivity {

    private static final String TAG = "ParentActiveDashboard";

    private ImageView childProfileImage;
    private TextView childNameTv, childGradeTv, childSchoolTv;
    private ImageView notificationBell, dropdownIcon;
    private LinearLayout childNameContainer;
    private RecyclerView activityRecycler;
    private ActivityAdapter activityAdapter;
    private List<ActivityItem> activityList = new ArrayList<>();

    // Action Buttons
    private LinearLayout trackLocationBtn, tripHistoryBtn, contactDriverBtn;

    // Bottom Navigation
    private LinearLayout navHome, navLocation, navQr, navProfile;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private List<ChildData> childrenList = new ArrayList<>();
    private int currentChildIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_active_dashboard);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load children
        loadChildren();

        // Set up listeners
        setupClickListeners();
    }

    private void initializeViews() {
        childProfileImage = findViewById(R.id.child_profile_image);
        childNameTv = findViewById(R.id.child_name_tv);
        childGradeTv = findViewById(R.id.child_grade_tv);
        childSchoolTv = findViewById(R.id.child_school_tv);
        notificationBell = findViewById(R.id.notification_bell);
        dropdownIcon = findViewById(R.id.dropdown_icon);
        childNameContainer = findViewById(R.id.child_name_container);
        activityRecycler = findViewById(R.id.activity_recycler);

        // Action buttons
        trackLocationBtn = findViewById(R.id.track_location_btn);
        tripHistoryBtn = findViewById(R.id.trip_history_btn);
        contactDriverBtn = findViewById(R.id.contact_driver_btn);

        // Bottom navigation
        navHome = findViewById(R.id.nav_home);
        navLocation = findViewById(R.id.nav_location);
        navQr = findViewById(R.id.nav_qr);
        navProfile = findViewById(R.id.nav_profile);
    }

    private void setupRecyclerView() {
        activityAdapter = new ActivityAdapter(activityList);
        activityRecycler.setLayoutManager(new LinearLayoutManager(this));
        activityRecycler.setAdapter(activityAdapter);
    }

    private void loadChildren() {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "Current user is null!");
            return;
        }

        String parentId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Loading children for parent ID: " + parentId);

        db.collection("children")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    childrenList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No children found for this parent");
                        showNoChildrenState();
                        return;
                    }

                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " children");

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ChildData child = new ChildData();
                        child.id = document.getId();
                        child.name = document.getString("childName");
                        child.imageUrl = document.getString("childProfileImageUrl");
                        child.age = document.getString("childAge");
                        child.grade = document.getString("childGrade");
                        child.school = document.getString("childSchool");

                        childrenList.add(child);
                        Log.d(TAG, "Child: " + child.name);
                    }

                    // Display first child
                    if (!childrenList.isEmpty()) {
                        displayChild(0);

                        // Show/hide dropdown based on number of children
                        if (dropdownIcon != null) {
                            dropdownIcon.setVisibility(childrenList.size() > 1 ? View.VISIBLE : View.GONE);
                        }
                    }

                    // Load activities for current child
                    loadActivities();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading children: " + e.getMessage());
                    Toast.makeText(this, "Failed to load children", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayChild(int index) {
        if (index < 0 || index >= childrenList.size()) return;

        currentChildIndex = index;
        ChildData child = childrenList.get(index);

        // Set child name
        if (child.name != null) {
            childNameTv.setText(child.name);
        }

        // Set child grade
        if (child.grade != null) {
            childGradeTv.setText(child.grade);
        } else {
            childGradeTv.setText("");
        }

        // Set child school
        if (child.school != null) {
            childSchoolTv.setText(child.school);
        } else {
            childSchoolTv.setText("");
        }

        // Load child image from Supabase
        if (child.imageUrl != null && !child.imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(child.imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.avatar)
                    .error(R.drawable.avatar)
                    .into(childProfileImage);
            Log.d(TAG, "Loading image for " + child.name + ": " + child.imageUrl);
        } else {
            childProfileImage.setImageResource(R.drawable.avatar);
        }
    }

    private void showChildSelectionMenu() {
        if (childrenList.size() <= 1) return;

        PopupMenu popupMenu = new PopupMenu(this, childNameContainer);

        for (int i = 0; i < childrenList.size(); i++) {
            popupMenu.getMenu().add(0, i, i, childrenList.get(i).name);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            displayChild(item.getItemId());
            loadActivities();
            return true;
        });

        popupMenu.show();
    }

    private void loadActivities() {
        activityList.clear();

        // Sample activities - replace with real data from Firestore
        if (!childrenList.isEmpty()) {
            String childName = childrenList.get(currentChildIndex).name;
            activityList.add(new ActivityItem("07:15 A.M.", childName + " has been drop off at school"));
            activityList.add(new ActivityItem("06:45 A.M.", childName + " has been pickup at home"));
            activityList.add(new ActivityItem("06:30 A.M.", "Driver has started the trip. Get ready!"));
        }

        activityAdapter.notifyDataSetChanged();
    }

    private void showNoChildrenState() {
        childNameTv.setText("No Children Added");
        childGradeTv.setText("");
        childSchoolTv.setText("Tap below to add your first child");
        childProfileImage.setImageResource(R.drawable.avatar);
        if (dropdownIcon != null) {
            dropdownIcon.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // Child name dropdown
        childNameContainer.setOnClickListener(v -> showChildSelectionMenu());

        // Action buttons
        trackLocationBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Track Location", Toast.LENGTH_SHORT).show();
            // Navigate to tracking map
        });

        tripHistoryBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Trip History", Toast.LENGTH_SHORT).show();
            // Navigate to trip history
        });

        contactDriverBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Contact Driver: +94 XX XXX XXXX", Toast.LENGTH_LONG).show();
            // Open dialer or chat with driver
        });

        // Notification bell
        notificationBell.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation
        navHome.setOnClickListener(v -> {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        });

        navLocation.setOnClickListener(v -> {
            Toast.makeText(this, "Location tracking coming soon", Toast.LENGTH_SHORT).show();
        });

        navQr.setOnClickListener(v -> {
            Toast.makeText(this, "QR codes coming soon", Toast.LENGTH_SHORT).show();
        });

        navProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Profile coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChildren();
    }

    // Inner class to hold child data
    private static class ChildData {
        String id;
        String name;
        String imageUrl;
        String age;
        String grade;
        String school;
    }

    // Inner class for activity items
    private static class ActivityItem {
        String time;
        String message;

        ActivityItem(String time, String message) {
            this.time = time;
            this.message = message;
        }
    }

    // RecyclerView Adapter
    private class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

        private List<ActivityItem> activities;

        ActivityAdapter(List<ActivityItem> activities) {
            this.activities = activities;
        }

        @NonNull
        @Override
        public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_activity, parent, false);
            return new ActivityViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
            ActivityItem activity = activities.get(position);
            holder.timeTv.setText(activity.time);
            holder.messageTv.setText(activity.message);
        }

        @Override
        public int getItemCount() {
            return activities.size();
        }

        class ActivityViewHolder extends RecyclerView.ViewHolder {
            TextView timeTv, messageTv;

            ActivityViewHolder(@NonNull View itemView) {
                super(itemView);
                timeTv = itemView.findViewById(R.id.activity_time);
                messageTv = itemView.findViewById(R.id.activity_message);
            }
        }
    }
}