package com.coreinnovators.geokids;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class available_pickup extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "AvailablePickup";

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private LinearLayout childrenListContainer;
    private TextView pendingCountBadge;
    private CardView childDetailCard;
    private ImageView detailChildAvatar, closeDetailCard, backButton;
    private TextView detailChildName, detailChildSchool, detailChildGrade,
            detailChildAddress, detailChildStatus;

    // Bottom nav
    private LinearLayout navHome, navLocation, navQr, navProfile;

    // Data
    private final List<ChildPickupData> pendingChildren = new ArrayList<>();
    private final Map<String, Marker> childMarkers = new HashMap<>();
    private ListenerRegistration tripListener;

    // Currently selected child
    private ChildPickupData selectedChild = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_pickup);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupMap();
        setupClickListeners();
        loadAssignedChildren();
    }

    private void initViews() {
        childrenListContainer = findViewById(R.id.children_list_container);
        pendingCountBadge      = findViewById(R.id.pending_count_badge);
        childDetailCard        = findViewById(R.id.child_detail_card);
        detailChildAvatar      = findViewById(R.id.detail_child_avatar);
        closeDetailCard        = findViewById(R.id.close_detail_card);
        detailChildName        = findViewById(R.id.detail_child_name);
        detailChildSchool      = findViewById(R.id.detail_child_school);
        detailChildGrade       = findViewById(R.id.detail_child_grade);
        detailChildAddress     = findViewById(R.id.detail_child_address);
        detailChildStatus      = findViewById(R.id.detail_child_status);
        backButton             = findViewById(R.id.back_button);

        navHome     = findViewById(R.id.nav_home);
        navLocation = findViewById(R.id.nav_location);
        navQr       = findViewById(R.id.nav_qr);
        navProfile  = findViewById(R.id.nav_profile);
    }

    // ─────────────────────────────────────────────────────────────────
    //  MAP
    // ─────────────────────────────────────────────────────────────────

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMarkerClickListener(marker -> {
            String childId = (String) marker.getTag();
            if (childId != null) {
                for (ChildPickupData child : pendingChildren) {
                    if (child.childId.equals(childId)) {
                        showChildDetailCard(child);
                        return true;
                    }
                }
            }
            return false;
        });

        mMap.setOnMapClickListener(latLng -> hideChildDetailCard());

        // If children already loaded before map was ready, add markers now
        if (!pendingChildren.isEmpty()) {
            addMarkersToMap();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  DATA LOADING
    // ─────────────────────────────────────────────────────────────────

    private void loadAssignedChildren() {
        if (auth.getCurrentUser() == null) return;
        String driverId = auth.getCurrentUser().getUid();

        db.collection("drivers").document(driverId)
                .get()
                .addOnSuccessListener(driverDoc -> {
                    if (!driverDoc.exists()) return;

                    List<String> assignedChildIds =
                            (List<String>) driverDoc.get("assignedChildren");

                    if (assignedChildIds == null || assignedChildIds.isEmpty()) {
                        pendingCountBadge.setText("0 pending");
                        return;
                    }

                    fetchChildrenDetails(assignedChildIds, driverId);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading driver: " + e.getMessage()));
    }

    private void fetchChildrenDetails(List<String> childIds, String driverId) {
        pendingChildren.clear();
        childrenListContainer.removeAllViews();

        final int total = childIds.size();
        final int[] resolvedCount = {0};

        for (String childId : childIds) {
            db.collection("children").document(childId)
                    .get()
                    .addOnSuccessListener(childDoc -> {
                        if (!childDoc.exists()) {
                            resolvedCount[0]++;
                            if (resolvedCount[0] >= total) finalizeChildrenList();
                            return;
                        }

                        ChildPickupData child = parseChild(childDoc);
                        String parentId = child.parentId;

                        if (parentId == null || parentId.isEmpty()) {
                            checkPickupStatusAndAdd(child, driverId, total, resolvedCount);
                            return;
                        }

                        // Parent docs use auto-generated IDs; query by parentId field instead
                        db.collection("parents")
                                .whereEqualTo("parentId", parentId)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(parentQuery -> {
                                    if (!parentQuery.isEmpty()) {
                                        DocumentSnapshot parentDoc = parentQuery.getDocuments().get(0);
                                        String addr = parentDoc.getString("pickupAddress");
                                        if (addr != null) child.pickupAddress = addr;

                                        Map<String, Object> coords =
                                                (Map<String, Object>) parentDoc.get("pickupCoordinates");
                                        if (coords != null) {
                                            Number lat = (Number) coords.get("latitude");
                                            Number lng = (Number) coords.get("longitude");
                                            if (lat != null && lng != null) {
                                                child.pickupLat = lat.doubleValue();
                                                child.pickupLng = lng.doubleValue();
                                            }
                                        }
                                    }
                                    checkPickupStatusAndAdd(child, driverId, total, resolvedCount);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error loading parent: " + e.getMessage());
                                    checkPickupStatusAndAdd(child, driverId, total, resolvedCount);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading child " + childId + ": " + e.getMessage());
                        resolvedCount[0]++;
                        if (resolvedCount[0] >= total) finalizeChildrenList();
                    });
        }
    }




    private ChildPickupData parseChild(DocumentSnapshot doc) {
        ChildPickupData child   = new ChildPickupData();
        child.childId           = doc.getId();
        child.childName         = doc.getString("childName");
        child.childSchool       = doc.getString("childSchool");
        child.childGrade        = doc.getString("childGrade");
        child.childProfileImageUrl = doc.getString("childProfileImageUrl");
        child.parentName        = doc.getString("parentName");
        child.parentId          = doc.getString("parentId");
        child.lastAction        = doc.getString("lastAction");
        // pickupAddress and pickupCoordinates are fetched from the parent document
        return child;
    }

    /**
     * Checks if the child is in the active trip's pickedUpChildren array.
     * Only adds to pendingChildren if NOT picked up.
     * Uses the shared resolvedCount[] array so finalizeChildrenList() fires
     * only after every child's async trip-status check has completed.
     */
    private void checkPickupStatusAndAdd(ChildPickupData child, String driverId,
                                         int total, int[] resolvedCount) {
        db.collection("trips")
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("status", "active")
                .limit(1)
                .get()
                .addOnSuccessListener(tripQuery -> {
                    boolean alreadyPickedUp = false;

                    if (!tripQuery.isEmpty()) {
                        DocumentSnapshot tripDoc = tripQuery.getDocuments().get(0);
                        List<String> pickedUpIds =
                                (List<String>) tripDoc.get("pickedUpChildren");
                        if (pickedUpIds != null && pickedUpIds.contains(child.childId)) {
                            alreadyPickedUp = true;
                        }
                    }

                    child.isPickedUp = alreadyPickedUp;
                    if (!alreadyPickedUp) {
                        pendingChildren.add(child);
                    }

                    resolvedCount[0]++;
                    if (resolvedCount[0] >= total) finalizeChildrenList();
                })
                .addOnFailureListener(e -> {
                    pendingChildren.add(child);   // assume pending on error
                    resolvedCount[0]++;
                    if (resolvedCount[0] >= total) finalizeChildrenList();
                });
    }

    private void finalizeChildrenList() {
        runOnUiThread(() -> {
            pendingCountBadge.setText(pendingChildren.size() + " pending");
            buildChildList();
            if (mMap != null) addMarkersToMap();
            listenToActiveTripChanges();
        });
    }

    // ─────────────────────────────────────────────────────────────────
    //  LIST
    // ─────────────────────────────────────────────────────────────────

    private void buildChildList() {
        childrenListContainer.removeAllViews();

        if (pendingChildren.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No pending pickups");
            emptyText.setTextColor(0xFF999999);
            emptyText.setTextSize(15);
            emptyText.setPadding(0, 24, 0, 24);
            childrenListContainer.addView(emptyText);
            return;
        }

        for (ChildPickupData child : pendingChildren) {
            addChildListItem(child);
        }
    }

    private void addChildListItem(ChildPickupData child) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(rowParams);
        row.setPadding(0, 20, 0, 20);

        TextView nameText = new TextView(this);
        nameText.setText(child.childName + " needs to be pickup");
        nameText.setTextColor(0xFF0D2D4D);
        nameText.setTextSize(15);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        nameText.setLayoutParams(nameParams);

        ImageView walkIcon = new ImageView(this);
        walkIcon.setImageResource(android.R.drawable.ic_menu_directions);
        walkIcon.setColorFilter(0xFF0D2D4D);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(40, 40);
        walkIcon.setLayoutParams(iconParams);

        row.addView(nameText);
        row.addView(walkIcon);

        row.setOnClickListener(v -> {
            showChildDetailCard(child);
            if (mMap != null && child.pickupLat != 0) {
                LatLng pos = new LatLng(child.pickupLat, child.pickupLng);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
            }
        });

        View divider = new View(this);
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int)(1 * getResources().getDisplayMetrics().density));
        divider.setBackgroundColor(0xFFE0E0E0);
        divider.setLayoutParams(divParams);

        childrenListContainer.addView(row);
        childrenListContainer.addView(divider);
    }

    // ─────────────────────────────────────────────────────────────────
    //  MAP MARKERS
    // ─────────────────────────────────────────────────────────────────

    private void addMarkersToMap() {
        if (mMap == null) return;

        for (Marker m : childMarkers.values()) m.remove();
        childMarkers.clear();

        if (pendingChildren.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasCoords = false;

        for (ChildPickupData child : pendingChildren) {
            if (child.pickupLat == 0 && child.pickupLng == 0) continue;

            LatLng pos = new LatLng(child.pickupLat, child.pickupLng);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(child.childName)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED)));

            if (marker != null) {
                marker.setTag(child.childId);
                childMarkers.put(child.childId, marker);
            }

            boundsBuilder.include(pos);
            hasCoords = true;
        }

        if (hasCoords) {
            try {
                LatLngBounds bounds = boundsBuilder.build();
                findViewById(R.id.map_container).post(() -> {
                    try {
                        mMap.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(bounds, 150));
                    } catch (Exception e) {
                        Log.e(TAG, "Camera bounds error: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Bounds error: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  DETAIL CARD
    // ─────────────────────────────────────────────────────────────────

    private void showChildDetailCard(ChildPickupData child) {
        selectedChild = child;
        childDetailCard.setVisibility(View.VISIBLE);

        detailChildName.setText(child.childName != null ? child.childName : "Unknown");
        detailChildSchool.setText(child.childSchool != null
                ? child.childSchool : "Unknown School");
        detailChildGrade.setText(child.childGrade != null ? child.childGrade : "");
        detailChildAddress.setText(child.pickupAddress != null
                ? child.pickupAddress : "Address not available");
        detailChildStatus.setText("Pending Pickup");

        if (child.childProfileImageUrl != null && !child.childProfileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(child.childProfileImageUrl)
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.avatar)
                    .into(detailChildAvatar);
        } else {
            detailChildAvatar.setImageResource(R.drawable.avatar);
        }
    }

    private void hideChildDetailCard() {
        childDetailCard.setVisibility(View.GONE);
        selectedChild = null;
    }

    // ─────────────────────────────────────────────────────────────────
    //  REAL-TIME TRIP LISTENER  (reacts to QR scans)
    // ─────────────────────────────────────────────────────────────────

    private void listenToActiveTripChanges() {
        if (auth.getCurrentUser() == null) return;
        String driverId = auth.getCurrentUser().getUid();

        if (tripListener != null) tripListener.remove();

        tripListener = db.collection("trips")
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("status", "active")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null || snapshots.isEmpty()) return;

                    DocumentSnapshot tripDoc = snapshots.getDocuments().get(0);
                    List<String> pickedUpIds =
                            (List<String>) tripDoc.get("pickedUpChildren");
                    if (pickedUpIds == null) return;

                    List<ChildPickupData> toRemove = new ArrayList<>();
                    for (ChildPickupData child : pendingChildren) {
                        if (pickedUpIds.contains(child.childId)) {
                            toRemove.add(child);
                        }
                    }

                    if (!toRemove.isEmpty()) {
                        runOnUiThread(() -> {
                            for (ChildPickupData child : toRemove) {
                                pendingChildren.remove(child);
                                Marker marker = childMarkers.remove(child.childId);
                                if (marker != null) marker.remove();

                                if (selectedChild != null &&
                                        selectedChild.childId.equals(child.childId)) {
                                    hideChildDetailCard();
                                }
                            }
                            pendingCountBadge.setText(pendingChildren.size() + " pending");
                            buildChildList();
                        });
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────────────────────

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        closeDetailCard.setOnClickListener(v -> hideChildDetailCard());

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, driver_active_dashboard.class));
            finish();
        });
        navLocation.setOnClickListener(v ->
                startActivity(new Intent(this, driver_map.class)));
        navQr.setOnClickListener(v ->
                startActivity(new Intent(this, QR_scan.class)));
        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, driver_profile.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tripListener != null) tripListener.remove();
    }

    // ─────────────────────────────────────────────────────────────────
    //  DATA MODEL
    // ─────────────────────────────────────────────────────────────────

    public static class ChildPickupData {
        public String  childId;
        public String  childName;
        public String  childSchool;
        public String  childGrade;
        public String  childProfileImageUrl;
        public String  parentName;
        public String  parentId;
        public String  pickupAddress;
        public String  lastAction;
        public double  pickupLat;
        public double  pickupLng;
        public boolean isPickedUp = false;
    }
}