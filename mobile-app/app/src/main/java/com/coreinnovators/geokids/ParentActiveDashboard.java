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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParentActiveDashboard extends AppCompatActivity {

    private static final String TAG = "ParentActiveDashboard";

    private ImageView childProfileImage;
    private TextView childNameTv, childGradeTv, childSchoolTv;
    private ImageView notificationBell, dropdownIcon;
    private LinearLayout childNameContainer;
    private RecyclerView activityRecycler;
    private ActivityAdapter activityAdapter;
    private List<ActivityItem> activityList = new ArrayList<>();

    private LinearLayout trackLocationBtn, tripHistoryBtn, contactDriverBtn;
    private LinearLayout navHome, navLocation, navQr, navProfile;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private List<ChildData> childrenList = new ArrayList<>();
    private int currentChildIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_active_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupRecyclerView();
        loadChildren();
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

        trackLocationBtn = findViewById(R.id.track_location_btn);
        tripHistoryBtn = findViewById(R.id.trip_history_btn);
        contactDriverBtn = findViewById(R.id.contact_driver_btn);

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
                        navigateToParentDashboard();
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

                        // Check driver assignment status
                        if (document.contains("assignedDriver")) {
                            Object assignedDriverObj = document.get("assignedDriver");
                            if (assignedDriverObj instanceof Map) {
                                Map<String, Object> assignedDriver = (Map<String, Object>) assignedDriverObj;
                                String status = (String) assignedDriver.get("status");

                                if ("accepted".equals(status)) {
                                    child.hasActiveDriver = true;
                                    child.driverId = (String) assignedDriver.get("driverId");
                                    child.driverName = (String) assignedDriver.get("driverName");
                                }
                            }
                        }

                        childrenList.add(child);
                        Log.d(TAG, "Child: " + child.name + " - Active Driver: " + child.hasActiveDriver);
                    }

                    // Check if we need to navigate away
                    boolean hasAtLeastOneActive = false;
                    for (ChildData child : childrenList) {
                        if (child.hasActiveDriver) {
                            hasAtLeastOneActive = true;
                            break;
                        }
                    }

                    if (!hasAtLeastOneActive) {
                        // No children with active drivers - navigate to pending dashboard
                        navigateToParentPendingDashboard();
                        return;
                    }

                    // Find first child with active driver and display
                    for (int i = 0; i < childrenList.size(); i++) {
                        if (childrenList.get(i).hasActiveDriver) {
                            displayChild(i);
                            break;
                        }
                    }

                    // Show dropdown only if there are multiple children with active drivers
                    int activeChildrenCount = 0;
                    for (ChildData child : childrenList) {
                        if (child.hasActiveDriver) activeChildrenCount++;
                    }

                    if (dropdownIcon != null) {
                        dropdownIcon.setVisibility(activeChildrenCount > 1 ? View.VISIBLE : View.GONE);
                    }

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

        if (child.name != null) {
            childNameTv.setText(child.name);
        }

        if (child.grade != null) {
            childGradeTv.setText(child.grade);
        } else {
            childGradeTv.setText("");
        }

        if (child.school != null) {
            childSchoolTv.setText(child.school);
        } else {
            childSchoolTv.setText("");
        }

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
        // Filter only children with active drivers
        List<ChildData> activeChildren = new ArrayList<>();
        List<Integer> activeIndices = new ArrayList<>();

        for (int i = 0; i < childrenList.size(); i++) {
            if (childrenList.get(i).hasActiveDriver) {
                activeChildren.add(childrenList.get(i));
                activeIndices.add(i);
            }
        }

        if (activeChildren.size() <= 1) return;

        PopupMenu popupMenu = new PopupMenu(this, childNameContainer);

        for (int i = 0; i < activeChildren.size(); i++) {
            popupMenu.getMenu().add(0, i, i, activeChildren.get(i).name);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int selectedIndex = activeIndices.get(item.getItemId());
            ChildData selectedChild = childrenList.get(selectedIndex);

            // Check if selected child still has active driver
            if (!selectedChild.hasActiveDriver) {
                Toast.makeText(this, "This child is pending driver assignment",
                        Toast.LENGTH_SHORT).show();
                // Reload children to refresh status
                loadChildren();
                return true;
            }

            displayChild(selectedIndex);
            loadActivities();
            return true;
        });

        popupMenu.show();
    }

    private void loadActivities() {
        activityList.clear();

        if (!childrenList.isEmpty() && currentChildIndex < childrenList.size()) {
            ChildData currentChild = childrenList.get(currentChildIndex);

            if (currentChild.hasActiveDriver) {
                String childName = currentChild.name;
                activityList.add(new ActivityItem("07:15 A.M.",
                        childName + " has been dropped off at school"));
                activityList.add(new ActivityItem("06:45 A.M.",
                        childName + " has been picked up at home"));
                activityList.add(new ActivityItem("06:30 A.M.",
                        "Driver has started the trip. Get ready!"));
            }
        }

        activityAdapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        childNameContainer.setOnClickListener(v -> showChildSelectionMenu());

        trackLocationBtn.setOnClickListener(v -> {
            ChildData currentChild = childrenList.get(currentChildIndex);
            if (currentChild.hasActiveDriver) {
                Toast.makeText(this, "Tracking " + currentChild.name, Toast.LENGTH_SHORT).show();
                // Navigate to tracking map
            } else {
                Toast.makeText(this, "Driver not assigned yet", Toast.LENGTH_SHORT).show();
            }
        });

        tripHistoryBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Trip History", Toast.LENGTH_SHORT).show();
        });

        contactDriverBtn.setOnClickListener(v -> {
            ChildData currentChild = childrenList.get(currentChildIndex);
            if (currentChild.hasActiveDriver && currentChild.driverName != null) {
                Toast.makeText(this, "Contact Driver: " + currentChild.driverName,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Driver not assigned yet", Toast.LENGTH_SHORT).show();
            }
        });

        notificationBell.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation
        navHome.setOnClickListener(v -> {
            // Already on home - do nothing or refresh
            Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show();
        });

        navLocation.setOnClickListener(v -> {
            Intent intent = new Intent(ParentActiveDashboard.this, ChildHistory.class);
            startActivity(intent);
        });

        navQr.setOnClickListener(v -> {
            Intent intent = new Intent(ParentActiveDashboard.this, QR_Code.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ParentActiveDashboard.this, parent_profile.class);
            startActivity(intent);
        });
    }

    private void navigateToParentDashboard() {
        Intent intent = new Intent(this, parent_dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToParentPendingDashboard() {
        Intent intent = new Intent(this, ParentPendingDashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChildren();
    }

    private static class ChildData {
        String id;
        String name;
        String imageUrl;
        String age;
        String grade;
        String school;
        boolean hasActiveDriver = false;
        String driverId;
        String driverName;
    }

    private static class ActivityItem {
        String time;
        String message;

        ActivityItem(String time, String message) {
            this.time = time;
            this.message = message;
        }
    }

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