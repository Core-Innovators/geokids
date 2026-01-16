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

