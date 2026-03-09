package com.coreinnovators.geokids;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class view_request extends AppCompatActivity {

    private static final String TAG = "ViewRequest";

    private RecyclerView requestsRecyclerView;
    private LinearLayout emptyState;
    private TextView requestCountLabel;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private List<RequestItem> requestList = new ArrayList<>();
    private RequestCardAdapter adapter;

    // ── Data model ──────────────────────────────────────────────────
    static class RequestItem {
        String childId;
        String childName;
        String childAge;
        String childGrade;
        String childSchool;
        String childProfileImageUrl;
        String parentId;
        String parentName;
        String parentContact1;
        String parentContact2;
        String pickupAddress; // fetched from parents collection
        double pickupLat;
        double pickupLng;
        String driverName;
    }

    // ── Lifecycle ───────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        requestsRecyclerView = findViewById(R.id.requests_recycler_view);
        emptyState = findViewById(R.id.empty_state);
        requestCountLabel = findViewById(R.id.request_count_label);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        adapter = new RequestCardAdapter(this, requestList,
                this::onAccept,
                this::onReject);
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestsRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequests();
    }

    // ── Load: children where assignedDriver.driverId == me AND status == "assigned" ──
    private void loadRequests() {
        if (auth.getCurrentUser() == null) return;

        String driverId = auth.getCurrentUser().getUid();
        requestCountLabel.setText("Loading...");

        db.collection("children")
                .whereEqualTo("assignedDriver.driverId", driverId)
                .whereEqualTo("assignedDriver.status", "assigned")
                .get()
                .addOnSuccessListener(snapshots -> {
                    requestList.clear();

                    if (snapshots.isEmpty()) {
                        showEmpty();
                        return;
                    }

                    // We need to fetch pickup address from parents collection for each request.
                    // Use a counter to know when all async fetches are done.
                    final int[] pending = {snapshots.size()};

                    for (QueryDocumentSnapshot doc : snapshots) {
                        RequestItem item = new RequestItem();
                        item.childId             = doc.getId();
                        item.childName           = doc.getString("childName");
                        item.childAge            = doc.getString("childAge");
                        item.childGrade          = doc.getString("childGrade");
                        item.childSchool         = doc.getString("childSchool");
                        item.childProfileImageUrl = doc.getString("childProfileImageUrl");
                        item.parentId            = doc.getString("parentId");
                        item.parentName          = doc.getString("parentName");

                        Map<String, Object> assignedDriver = (Map<String, Object>) doc.get("assignedDriver");
                        if (assignedDriver != null) {
                            item.driverName = (String) assignedDriver.get("driverName");
                        }

                        // Fetch pickup address and contact from parents collection using parentId field
                        if (item.parentId != null && !item.parentId.isEmpty()) {
                            db.collection("parents")
                                    .whereEqualTo("parentId", item.parentId)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener(parentQuery -> {
                                        if (!parentQuery.isEmpty()) {
                                            DocumentSnapshot parentDoc = parentQuery.getDocuments().get(0);
                                            item.parentContact1 = parentDoc.getString("parentContact1");
                                            item.parentContact2 = parentDoc.getString("parentContact2");
                                            item.pickupAddress = parentDoc.getString("pickupAddress");

                                            // Fetch coordinates for map clicking
                                            Map<String, Object> coords = (Map<String, Object>) parentDoc.get("pickupCoordinates");
                                            if (coords != null) {
                                                Number lat = (Number) coords.get("latitude");
                                                Number lng = (Number) coords.get("longitude");
                                                if (lat != null && lng != null) {
                                                    item.pickupLat = lat.doubleValue();
                                                    item.pickupLng = lng.doubleValue();
                                                }
                                            }

                                            // Fallback: If parentName is missing in child doc, get from parent doc
                                            if (item.parentName == null || item.parentName.isEmpty()) {
                                                item.parentName = parentDoc.getString("parentName");
                                            }
                                        }
                                        requestList.add(item);
                                        pending[0]--;
                                        if (pending[0] == 0) onAllLoaded();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to fetch parent for parentId: " + item.parentId + " - " + e.getMessage());
                                        requestList.add(item); // add anyway with no pickup
                                        pending[0]--;
                                        if (pending[0] == 0) onAllLoaded();
                                    });
                        } else {
                            requestList.add(item);
                            pending[0]--;
                            if (pending[0] == 0) onAllLoaded();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load requests: " + e.getMessage());
                    Toast.makeText(this, "Failed to load requests", Toast.LENGTH_SHORT).show();
                    showEmpty();
                });
    }

    private void onAllLoaded() {
        adapter.notifyDataSetChanged();
        if (requestList.isEmpty()) {
            showEmpty();
        } else {
            emptyState.setVisibility(View.GONE);
            requestsRecyclerView.setVisibility(View.VISIBLE);
            requestCountLabel.setText(requestList.size() + " pending request" + (requestList.size() == 1 ? "" : "s"));
        }
    }

    private void showEmpty() {
        requestsRecyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        requestCountLabel.setText("No pending requests");
    }

    // ── Accept: update child's assignedDriver.status → "accepted"
    //            add childId to driver's assignedChildren array ──────
    private void onAccept(RequestItem item, int position) {
        if (auth.getCurrentUser() == null) return;
        String driverId = auth.getCurrentUser().getUid();

        // 1. Update assignedDriver map on child document
        Map<String, Object> assignedDriver = new HashMap<>();
        assignedDriver.put("driverId", driverId);
        assignedDriver.put("driverName", item.driverName);
        assignedDriver.put("status", "accepted");
        assignedDriver.put("assignedAt", FieldValue.serverTimestamp());

        db.collection("children").document(item.childId)
                .update("assignedDriver", assignedDriver)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Child assignedDriver updated to accepted");

                    // 2. Add childId to driver's assignedChildren array
                    db.collection("drivers").document(driverId)
                            .update("assignedChildren", FieldValue.arrayUnion(item.childId))
                            .addOnFailureListener(e -> {
                                // Field may not exist yet — create it
                                Map<String, Object> patch = new HashMap<>();
                                List<String> list = new ArrayList<>();
                                list.add(item.childId);
                                patch.put("assignedChildren", list);
                                db.collection("drivers").document(driverId)
                                        .set(patch, com.google.firebase.firestore.SetOptions.merge());
                            });

                    removeItem(position);
                    Toast.makeText(this, "✅ Request accepted!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Accept failed: " + e.getMessage());
                    Toast.makeText(this, "Failed to accept request", Toast.LENGTH_SHORT).show();
                });
    }

    // ── Reject: delete assignedDriver from child
    //            remove childId from driver's assignedChildren array ──
    private void onReject(RequestItem item, int position) {
        if (auth.getCurrentUser() == null) return;
        String driverId = auth.getCurrentUser().getUid();

        // 1. Delete the assignedDriver map from child
        db.collection("children").document(item.childId)
                .update("assignedDriver", FieldValue.delete())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "assignedDriver removed from child");

                    // 2. Remove childId from driver's assignedChildren array
                    db.collection("drivers").document(driverId)
                            .update("assignedChildren", FieldValue.arrayRemove(item.childId));

                    removeItem(position);
                    Toast.makeText(this, "Request declined", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Reject failed: " + e.getMessage());
                    Toast.makeText(this, "Failed to decline request", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeItem(int position) {
        if (position >= 0 && position < requestList.size()) {
            requestList.remove(position);
            adapter.notifyItemRemoved(position);
            if (requestList.isEmpty()) showEmpty();
            else requestCountLabel.setText(requestList.size() + " pending request" + (requestList.size() == 1 ? "" : "s"));
        }
    }

    // ── Adapter ─────────────────────────────────────────────────────
    interface OnActionListener {
        void onAction(RequestItem item, int position);
    }

    static class RequestCardAdapter extends RecyclerView.Adapter<RequestCardAdapter.VH> {

        private final Context ctx;
        private final List<RequestItem> list;
        private final OnActionListener onAccept;
        private final OnActionListener onReject;

        RequestCardAdapter(Context ctx, List<RequestItem> list,
                           OnActionListener onAccept, OnActionListener onReject) {
            this.ctx = ctx;
            this.list = list;
            this.onAccept = onAccept;
            this.onReject = onReject;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(ctx).inflate(R.layout.request_item, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            RequestItem item = list.get(pos);

            h.childName.setText(item.childName != null ? item.childName : "Unknown Child");

            String gradeSchool = "";
            if (item.childGrade != null) gradeSchool += item.childGrade;
            if (item.childSchool != null) gradeSchool += (gradeSchool.isEmpty() ? "" : " · ") + item.childSchool;
            h.childGradeSchool.setText(gradeSchool.isEmpty() ? "School not set" : gradeSchool);

            h.childAge.setText(item.childAge != null ? item.childAge : "-");
            h.parentName.setText("Parent: " + (item.parentName != null ? item.parentName : "Unknown"));

            // Show both contacts if available
            StringBuilder contactStr = new StringBuilder();
            if (item.parentContact1 != null) contactStr.append(item.parentContact1);
            if (item.parentContact2 != null && !item.parentContact2.isEmpty()) {
                if (contactStr.length() > 0) contactStr.append(" / ");
                contactStr.append(item.parentContact2);
            }
            h.parentContact.setText(contactStr.length() > 0 ? contactStr.toString() : "No contact");

            // Click to call
            h.parentContact.setOnClickListener(v -> {
                String phone = (item.parentContact1 != null) ? item.parentContact1 : item.parentContact2;
                if (phone != null && !phone.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                    ctx.startActivity(intent);
                }
            });

            h.pickupAddress.setText(item.pickupAddress != null ? item.pickupAddress : "Pickup not set");

            // Click to open map
            h.pickupAddress.setOnClickListener(v -> {
                if (item.pickupLat != 0) {
                    String uri = "geo:0,0?q=" + item.pickupLat + "," + item.pickupLng + "(" + Uri.encode(item.childName + "'s Pickup") + ")";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");
                    if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                        ctx.startActivity(intent);
                    } else {
                        ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                    }
                } else if (item.pickupAddress != null && !item.pickupAddress.equals("Pickup not set")) {
                    String uri = "geo:0,0?q=" + Uri.encode(item.pickupAddress);
                    ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                }
            });

            // Child profile image
            if (item.childProfileImageUrl != null && !item.childProfileImageUrl.isEmpty()) {
                Glide.with(ctx)
                        .load(item.childProfileImageUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .circleCrop()
                        .into(h.childAvatar);
            } else {
                h.childAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            }

            // Button animations + callbacks
            h.acceptButton.setOnClickListener(v -> {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80)
                        .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()).start();
                onAccept.onAction(item, h.getAdapterPosition());
            });

            h.rejectButton.setOnClickListener(v -> {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80)
                        .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()).start();
                onReject.onAction(item, h.getAdapterPosition());
            });

            // Staggered card entrance animation
            h.itemView.setAlpha(0f);
            h.itemView.setTranslationY(30f);
            h.itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(pos * 80L)
                    .start();
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            CardView card;
            CircleImageView childAvatar;
            TextView childName, childGradeSchool, childAge;
            TextView parentName, parentContact, pickupAddress;
            Button acceptButton, rejectButton;

            VH(@NonNull View v) {
                super(v);
                card             = v.findViewById(R.id.request_card);
                childAvatar      = v.findViewById(R.id.child_avatar);
                childName        = v.findViewById(R.id.child_name);
                childGradeSchool = v.findViewById(R.id.child_grade_school);
                childAge         = v.findViewById(R.id.child_age);
                parentName       = v.findViewById(R.id.parent_name);
                parentContact    = v.findViewById(R.id.parent_contact);
                pickupAddress    = v.findViewById(R.id.pickup_address);
                acceptButton     = v.findViewById(R.id.accept_button);
                rejectButton     = v.findViewById(R.id.reject_button);
            }
        }
    }
}