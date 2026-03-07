package com.coreinnovators.geokids;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * InformAbsence Activity
 *
 * Firestore schema written to "absence" collection:
 * {
 *   absenceId:      String  (auto-generated doc ID),
 *   childId:        String,
 *   childName:      String,
 *   parentId:       String,
 *   driverId:       String,
 *   absenceDate:    String  ("yyyy-MM-dd"),
 *   absenceDateTs:  long    (midnight UTC epoch – handy for range queries),
 *   reason:         String  (optional free-text from parent),
 *   status:         String  ("active" | "cancelled"),
 *   createdAt:      long    (System.currentTimeMillis())
 * }
 *
 * A compound index on (driverId, absenceDate, status) is recommended so that
 * available_pickup can run:
 *   .whereEqualTo("driverId", driverId)
 *   .whereEqualTo("absenceDate", todayStr)
 *   .whereEqualTo("status", "active")
 */
public class InformAbsence extends AppCompatActivity {

    private static final String TAG = "InformAbsence";

    // ── Views ──────────────────────────────────────────────────────────
    private Spinner       childSpinner;
    private TextView      selectedDateText;
    private EditText      reasonInput;
    private Button        btnSelectDate, btnSubmit, btnCancel;
    private ProgressBar   progressBar;
    private ScrollView    contentLayout;

    // ── Firebase ───────────────────────────────────────────────────────
    private FirebaseFirestore db;
    private FirebaseAuth      auth;

    // ── State ──────────────────────────────────────────────────────────
    private final List<ChildItem> childList    = new ArrayList<>();
    private final List<String>    selectedDates = new ArrayList<>(); // "yyyy-MM-dd"
    private String parentId;

    private final SimpleDateFormat dateFmt =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayFmt =
            new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());

    // ──────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inform_absence);

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        loadChildren();
    }

    private void initViews() {
        childSpinner     = findViewById(R.id.spinner_child);
        selectedDateText = findViewById(R.id.tv_selected_dates);
        reasonInput      = findViewById(R.id.et_reason);
        btnSelectDate    = findViewById(R.id.btn_select_date);
        btnSubmit        = findViewById(R.id.btn_submit);
        btnCancel        = findViewById(R.id.btn_cancel);
        progressBar      = findViewById(R.id.progress_bar);
        contentLayout    = findViewById(R.id.content_layout);

        // Back arrow in header
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSubmit.setOnClickListener(v    -> submitAbsence());
        btnCancel.setOnClickListener(v    -> finish());
    }

    // ── Load parent's children ─────────────────────────────────────────
    private void loadChildren() {
        if (auth.getCurrentUser() == null) { finish(); return; }
        parentId = auth.getCurrentUser().getUid();

        showLoading(true);

        db.collection("children")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(query -> {
                    childList.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        ChildItem item = new ChildItem();
                        item.childId   = doc.getId();
                        item.childName = doc.getString("childName");

                        // assignedDriver is a MAP: { driverId, driverName, status, assignedAt }
                        String driverId = null;
                        Object assignedDriverObj = doc.get("assignedDriver");
                        if (assignedDriverObj instanceof java.util.Map) {
                            java.util.Map<String, Object> assignedDriverMap =
                                    (java.util.Map<String, Object>) assignedDriverObj;
                            Object did = assignedDriverMap.get("driverId");
                            if (did instanceof String) driverId = (String) did;
                        }
                        // Fallback: try top-level fields just in case schema varies
                        if (driverId == null || driverId.isEmpty())
                            driverId = doc.getString("driverId");
                        if (driverId == null || driverId.isEmpty())
                            driverId = doc.getString("assignedDriverId");
                        item.driverId = driverId != null ? driverId : "";

                        childList.add(item);
                    }

                    if (childList.isEmpty()) {
                        showLoading(false);
                        Toast.makeText(this,
                                "No children found. Please add a child profile first.",
                                Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    // For any child whose driverId is still blank, look it up from
                    // the drivers collection (where assignedChildren[] contains childId)
                    resolveBlankDriverIds();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading children: " + e.getMessage());
                    showLoading(false);
                    Toast.makeText(this, "Failed to load children data.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * For each child that still has no driverId, query the drivers collection
     * for a doc whose assignedChildren array contains that childId.
     * When all lookups finish, populate the spinner.
     */
    private void resolveBlankDriverIds() {
        List<ChildItem> needsLookup = new ArrayList<>();
        for (ChildItem c : childList) {
            if (c.driverId == null || c.driverId.isEmpty()) needsLookup.add(c);
        }

        if (needsLookup.isEmpty()) {
            populateSpinner();
            showLoading(false);
            return;
        }

        final int   total    = needsLookup.size();
        final int[] resolved = {0};

        for (ChildItem child : needsLookup) {
            db.collection("drivers")
                    .whereArrayContains("assignedChildren", child.childId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(driverQuery -> {
                        if (!driverQuery.isEmpty()) {
                            child.driverId = driverQuery.getDocuments().get(0).getId();
                            Log.d(TAG, "Resolved driverId for " + child.childName
                                    + " -> " + child.driverId);
                        } else {
                            Log.w(TAG, "No driver found for child: " + child.childId);
                        }
                        resolved[0]++;
                        if (resolved[0] >= total) {
                            runOnUiThread(() -> {
                                populateSpinner();
                                showLoading(false);
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Driver lookup failed for " + child.childId
                                + ": " + e.getMessage());
                        resolved[0]++;
                        if (resolved[0] >= total) {
                            runOnUiThread(() -> {
                                populateSpinner();
                                showLoading(false);
                            });
                        }
                    });
        }
    }

    private void populateSpinner() {
        List<String> names = new ArrayList<>();
        for (ChildItem c : childList) names.add(c.childName != null ? c.childName : "Unknown");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childSpinner.setAdapter(adapter);
    }

    // ── Date picker ────────────────────────────────────────────────────
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        // Minimum date = today
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day);
            String dateKey = dateFmt.format(selected.getTime());

            if (selectedDates.contains(dateKey)) {
                Toast.makeText(this, "Date already added.", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedDates.add(dateKey);
            refreshDateDisplay();

        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void refreshDateDisplay() {
        if (selectedDates.isEmpty()) {
            selectedDateText.setText("No dates selected");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String d : selectedDates) {
            try {
                Date parsed = dateFmt.parse(d);
                sb.append("• ").append(displayFmt.format(parsed)).append("\n");
            } catch (Exception e) {
                sb.append("• ").append(d).append("\n");
            }
        }
        selectedDateText.setText(sb.toString().trim());
    }

    // ── Submit ─────────────────────────────────────────────────────────
    private void submitAbsence() {
        int selectedPos = childSpinner.getSelectedItemPosition();
        if (selectedPos < 0 || selectedPos >= childList.size()) {
            Toast.makeText(this, "Please select a child.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDates.isEmpty()) {
            Toast.makeText(this, "Please select at least one absence date.", Toast.LENGTH_SHORT).show();
            return;
        }

        ChildItem child  = childList.get(selectedPos);
        String    reason = reasonInput.getText().toString().trim();

        showLoading(true);

        // Write one absence document per selected date (makes querying by date trivial)
        final int total          = selectedDates.size();
        final int[] doneCount    = {0};
        final boolean[] anyError = {false};

        for (String dateStr : selectedDates) {
            long midnightTs = getMidnightTimestamp(dateStr);

            Map<String, Object> absenceData = new HashMap<>();
            absenceData.put("childId",       child.childId);
            absenceData.put("childName",     child.childName);
            absenceData.put("parentId",      parentId);
            absenceData.put("driverId",      child.driverId != null ? child.driverId : "");
            absenceData.put("absenceDate",   dateStr);        // "yyyy-MM-dd" — primary query key
            absenceData.put("absenceDateTs", midnightTs);     // long for range queries
            absenceData.put("reason",        reason);
            absenceData.put("status",        "active");       // "active" | "cancelled"
            absenceData.put("createdAt",     System.currentTimeMillis());

            db.collection("absence")
                    .add(absenceData)
                    .addOnSuccessListener(ref -> {
                        Log.d(TAG, "Absence stored: " + ref.getId());
                        doneCount[0]++;
                        if (doneCount[0] >= total) onAllWritesDone(anyError[0]);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to write absence: " + e.getMessage());
                        anyError[0] = true;
                        doneCount[0]++;
                        if (doneCount[0] >= total) onAllWritesDone(anyError[0]);
                    });
        }
    }

    private void onAllWritesDone(boolean hadErrors) {
        showLoading(false);
        if (hadErrors) {
            Toast.makeText(this,
                    "Some absence records could not be saved. Please try again.",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,
                    "Absence reported successfully! The driver will be notified.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────
    private long getMidnightTimestamp(String dateStr) {
        try {
            Date d = dateFmt.parse(dateStr);
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            c.setTime(d);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    // ── Inner model ────────────────────────────────────────────────────
    private static class ChildItem {
        String childId;
        String childName;
        String driverId;
    }
}