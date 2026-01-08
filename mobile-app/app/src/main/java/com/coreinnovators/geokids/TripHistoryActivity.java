package com.coreinnovators.geokids;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class TripHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_history);

        // This is where you would call an API to fetch the latest history
        updateTripHistory();
    }

    private void updateTripHistory() {
        // Logic to refresh the UI with new data
        // For example: List<Trip> trips = database.getRecentTrips();
        // adapter.notifyDataSetChanged();
    }
}

// Data Model to represent the events from the screenshot
class TripEvent {
    String studentName; // e.g., "Kavidu" [1]
    String time;        // e.g., "07.15 A.M." [1]
    String location;    // e.g., "School" or "Home" [1]
    boolean isPickup;   // True for Green Dot, False for Red Dot
}
