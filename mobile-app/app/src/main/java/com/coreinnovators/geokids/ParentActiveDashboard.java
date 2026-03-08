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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParentActiveDashboard extends AppCompatActivity {

    private static final String TAG = "ParentActiveDashboard";

    private ImageView childProfileImage;
    private TextView childNameTv, childGradeTv, childSchoolTv;
    private ImageView notificationBell;
    private TextView notificationBadge;
    private ImageView dropdownIcon;
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

        // Initialize FCM and get token
        initializeFCM();

        loadChildren();
        setupClickListeners();
    }

    private void initializeViews() {
        childProfileImage = findViewById(R.id.child_profile_image);
        childNameTv = findViewById(R.id.child_name_tv);
        childGradeTv = findViewById(R.id.child_grade_tv);
        childSchoolTv = findViewById(R.id.child_school_tv);
        notificationBell = findViewById(R.id.notification_bell);
        notificationBadge = findViewById(R.id.notification_badge);
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

    private void initializeFCM() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM token failed", task.getException());
                            return;
                        }

                        // Get FCM token
                        String token = task.getResult();
                        Log.d(TAG, "FCM Token: " + token);

                        // Save token to Firestore
                        saveFCMTokenToFirestore(token);
                    }
                });
    }

    private void saveFCMTokenToFirestore(String token) {
        if (auth.getCurrentUser() == null) return;

        String parentId = auth.getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", token);
        updates.put("tokenUpdatedAt", System.currentTimeMillis());

        // Try to update by document ID first
        db.collection("parents").document(parentId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ FCM token saved successfully (by doc ID)");
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Doc not found by ID, trying query by parentId field...");
                    // Fallback: Query by parentId field
                    db.collection("parents")
                            .whereEqualTo("parentId", parentId)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    String docId = querySnapshot.getDocuments().get(0).getId();
                                    db.collection("parents").document(docId)
                                            .update(updates)
                                            .addOnSuccessListener(aVoid2 -> {
                                                Log.d(TAG, "✅ FCM token saved successfully (by query)");
                                            })
                                            .addOnFailureListener(err -> {
                                                Log.e(TAG, "❌ Failed to save FCM token: " + err.getMessage());
                                            });
                                } else {
                                    Log.w(TAG, "⚠️ Parent document not found for ID: " + parentId);
                                }
                            })
                            .addOnFailureListener(queryError -> {
                                Log.e(TAG, "❌ Failed to query parent: " + queryError.getMessage());
                            });
                });
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
                    loadUnreadNotificationCount();
                    
                    // Start listening for real-time pickup/dropoff notifications
                    startRealtimeNotificationListeners();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading children: " + e.getMessage());
                    Toast.makeText(this, "Failed to load children", Toast.LENGTH_SHORT).show();
                });
    }

    // Real-time listeners for pickup/dropoff notifications
    private com.google.firebase.firestore.ListenerRegistration pickupListener;
    private com.google.firebase.firestore.ListenerRegistration dropoffListener;
    private long lastNotificationTime = 0;
    private boolean isFirstPickupLoad = true;
    private boolean isFirstDropoffLoad = true;

    private void startRealtimeNotificationListeners() {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "❌ Cannot start listeners - no user logged in");
            return;
        }
        
        String parentId = auth.getCurrentUser().getUid();
        Log.d(TAG, "🔔 Starting real-time notification listeners for parent: " + parentId);
        
        // Reset first load flags
        isFirstPickupLoad = true;
        isFirstDropoffLoad = true;
        
        // Listen for new pickups
        pickupListener = db.collection("pickups")
                .whereEqualTo("parentId", parentId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "❌ Pickup listener error: " + e.getMessage());
                        return;
                    }
                    
                    Log.d(TAG, "📥 Pickup listener triggered. isFirstLoad: " + isFirstPickupLoad);
                    
                    if (snapshots != null) {
                        Log.d(TAG, "📊 Total pickup documents: " + snapshots.size());
                        
                        // Skip first load to avoid showing old notifications
                        if (isFirstPickupLoad) {
                            isFirstPickupLoad = false;
                            Log.d(TAG, "⏭️ Skipping first pickup load (existing records)");
                            return;
                        }
                        
                        for (com.google.firebase.firestore.DocumentChange dc : snapshots.getDocumentChanges()) {
                            Log.d(TAG, "📝 Document change type: " + dc.getType());
                            
                            if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                Map<String, Object> data = dc.getDocument().getData();
                                String childName = (String) data.get("childName");
                                String childId = (String) data.get("childId");
                                String driverName = (String) data.get("driverName");
                                Object timestampObj = data.get("timestamp");
                                
                                Log.d(TAG, "🚌 NEW PICKUP DETECTED!");
                                Log.d(TAG, "   Child: " + childName);
                                Log.d(TAG, "   Driver: " + driverName);
                                Log.d(TAG, "   Timestamp: " + timestampObj);
                                
                                String title = "🚌 Child Picked Up";
                                String message = childName + " has been picked up by " + driverName;
                                
                                showLocalNotification(title, message, "pickup");
                                
                                // Save to notifications collection for history
                                saveNotificationToFirestore(parentId, childId, childName, driverName, 
                                        null, "pickup", title, message, timestampObj);
                                
                                // Refresh activities
                                runOnUiThread(() -> {
                                    loadActivities();
                                    loadUnreadNotificationCount();
                                });
                            }
                        }
                    }
                });
        
        // Listen for new dropoffs
        dropoffListener = db.collection("dropoffs")
                .whereEqualTo("parentId", parentId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "❌ Dropoff listener error: " + e.getMessage());
                        return;
                    }
                    
                    Log.d(TAG, "📥 Dropoff listener triggered. isFirstLoad: " + isFirstDropoffLoad);
                    
                    if (snapshots != null) {
                        Log.d(TAG, "📊 Total dropoff documents: " + snapshots.size());
                        
                        // Skip first load to avoid showing old notifications
                        if (isFirstDropoffLoad) {
                            isFirstDropoffLoad = false;
                            Log.d(TAG, "⏭️ Skipping first dropoff load (existing records)");
                            return;
                        }
                        
                        for (com.google.firebase.firestore.DocumentChange dc : snapshots.getDocumentChanges()) {
                            Log.d(TAG, "📝 Document change type: " + dc.getType());
                            
                            if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                Map<String, Object> data = dc.getDocument().getData();
                                String childName = (String) data.get("childName");
                                String childId = (String) data.get("childId");
                                String driverName = (String) data.get("driverName");
                                String school = (String) data.get("childSchool");
                                Object timestampObj = data.get("timestamp");
                                
                                Log.d(TAG, "🏫 NEW DROPOFF DETECTED!");
                                Log.d(TAG, "   Child: " + childName);
                                Log.d(TAG, "   School: " + school);
                                Log.d(TAG, "   Timestamp: " + timestampObj);
                                
                                String title = "🏫 Child Dropped Off";
                                String message = childName + " has arrived at " + (school != null ? school : "school");
                                
                                showLocalNotification(title, message, "dropoff");
                                
                                // Save to notifications collection for history
                                saveNotificationToFirestore(parentId, childId, childName, driverName, 
                                        school, "dropoff", title, message, timestampObj);
                                
                                // Refresh activities
                                runOnUiThread(() -> {
                                    loadActivities();
                                    loadUnreadNotificationCount();
                                });
                            }
                        }
                    }
                });
    }
    
    /**
     * Save notification to Firestore for history
     */
    private void saveNotificationToFirestore(String parentId, String childId, String childName, 
                                              String driverName, String school, String type, 
                                              String title, String message, Object timestampObj) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("parentId", parentId);
        notification.put("childId", childId != null ? childId : "");
        notification.put("childName", childName != null ? childName : "");
        notification.put("driverName", driverName != null ? driverName : "");
        if (school != null) {
            notification.put("childSchool", school);
        }
        notification.put("type", type);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("read", false);
        notification.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
        
        // Store original timestamp if available
        if (timestampObj != null) {
            notification.put("createdAt", timestampObj);
        }
        
        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "✅ Notification saved to Firestore: " + docRef.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to save notification: " + e.getMessage());
                });
    }

    private void showLocalNotification(String title, String message, String type) {
        // Prevent duplicate notifications (within 5 seconds)
        if (System.currentTimeMillis() - lastNotificationTime < 5000) {
            return;
        }
        lastNotificationTime = System.currentTimeMillis();
        
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
        String channelId = "geokids_notifications";
        
        // Create notification channel for Android O+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    channelId,
                    "GeoKids Notifications",
                    android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for child pickup and dropoff");
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
        
        // Intent to open this activity when notification is clicked
        Intent intent = new Intent(this, ParentActiveDashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("type", type);
        
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                this, 0, intent,
                android.app.PendingIntent.FLAG_ONE_SHOT | android.app.PendingIntent.FLAG_IMMUTABLE
        );
        
        // Build notification
        androidx.core.app.NotificationCompat.Builder builder =
                new androidx.core.app.NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setStyle(new androidx.core.app.NotificationCompat.BigTextStyle().bigText(message));
        
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        Log.d(TAG, "✅ Local notification shown: " + title);
    }

    private void stopRealtimeNotificationListeners() {
        if (pickupListener != null) {
            pickupListener.remove();
            pickupListener = null;
        }
        if (dropoffListener != null) {
            dropoffListener.remove();
            dropoffListener = null;
        }
        Log.d(TAG, "🔕 Real-time notification listeners stopped");
    }

    private void loadUnreadNotificationCount() {
        if (auth.getCurrentUser() == null) return;

        String parentId = auth.getCurrentUser().getUid();

        db.collection("notifications")
                .whereEqualTo("parentId", parentId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int unreadCount = queryDocumentSnapshots.size();

                    if (notificationBadge != null) {
                        if (unreadCount > 0) {
                            notificationBadge.setVisibility(View.VISIBLE);
                            notificationBadge.setText(String.valueOf(unreadCount > 99 ? "99+" : unreadCount));
                        } else {
                            notificationBadge.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading notification count: " + e.getMessage());
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

            if (!selectedChild.hasActiveDriver) {
                Toast.makeText(this, "This child is pending driver assignment",
                        Toast.LENGTH_SHORT).show();
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
                openTrackLocationScreen(currentChild);
            } else {
                Toast.makeText(this, "Driver not assigned yet", Toast.LENGTH_SHORT).show();
            }
        });

        tripHistoryBtn.setOnClickListener(v -> {
            if (!childrenList.isEmpty() && currentChildIndex < childrenList.size()) {
                ChildData currentChild = childrenList.get(currentChildIndex);
                Intent histIntent = new Intent(ParentActiveDashboard.this, ChildHistory.class);
                histIntent.putExtra("childId", currentChild.id);
                histIntent.putExtra("childName", currentChild.name);
                startActivity(histIntent);
            } else {
                startActivity(new Intent(ParentActiveDashboard.this, ChildHistory.class));
            }
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
            Intent intent = new Intent(ParentActiveDashboard.this, NotificationHistory.class);
            startActivity(intent);
        });

        navHome.setOnClickListener(v -> {
            // Already on home
        });

        navLocation.setOnClickListener(v -> {
            // Navigate to Track Location screen
            if (!childrenList.isEmpty()) {
                ChildData currentChild = childrenList.get(currentChildIndex);
                if (currentChild.hasActiveDriver) {
                    openTrackLocationScreen(currentChild);
                } else {
                    Toast.makeText(this, "Driver not assigned yet", Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent intent = new Intent(ParentActiveDashboard.this, TrackLocation.class);
                startActivity(intent);
            }
        });

        navQr.setOnClickListener(v -> {
            if (!childrenList.isEmpty() && currentChildIndex < childrenList.size()) {
                ChildData currentChild = childrenList.get(currentChildIndex);
                Intent histIntent = new Intent(ParentActiveDashboard.this, ChildHistory.class);
                histIntent.putExtra("childId", currentChild.id);
                histIntent.putExtra("childName", currentChild.name);
                startActivity(histIntent);
            } else {
                startActivity(new Intent(ParentActiveDashboard.this, ChildHistory.class));
            }
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ParentActiveDashboard.this, parent_profile.class);
            startActivity(intent);
        });
    }


    private void openTrackLocationScreen(ChildData child) {
        Intent intent = new Intent(ParentActiveDashboard.this, TrackLocation.class);
        intent.putExtra("driverId",   child.driverId);
        intent.putExtra("driverName", child.driverName);
        intent.putExtra("childName",  child.name);
        startActivity(intent);
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
        loadUnreadNotificationCount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop real-time listeners to prevent memory leaks
        stopRealtimeNotificationListeners();
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