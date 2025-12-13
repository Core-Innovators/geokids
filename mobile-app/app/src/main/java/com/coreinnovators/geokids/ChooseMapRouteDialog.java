package com.coreinnovators.geokids;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChooseMapRouteDialog extends DialogFragment implements OnMapReadyCallback {

    private static final String TAG = "ChooseMapRouteDialog";
    private static final String ARG_START_LAT = "start_lat";
    private static final String ARG_START_LNG = "start_lng";
    private static final String ARG_END_LAT = "end_lat";
    private static final String ARG_END_LNG = "end_lng";

    // TODO: Replace with your Google Maps API Key
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyA7gzSifsS-B5CyokjNZLb0Jq8wGeclU-A";

    private GoogleMap mMap;
    private Button setRouteButton;

    private LatLng startPoint;
    private LatLng endPoint;
    private List<RouteData> availableRoutes = new ArrayList<>();
    private RouteData selectedRoute;
    private List<Polyline> drawnPolylines = new ArrayList<>();

    private RouteSelectionListener listener;

    public interface RouteSelectionListener {
        void onRouteSelected(RouteData route);
    }

    public static ChooseMapRouteDialog newInstance(LatLng start, LatLng end) {
        ChooseMapRouteDialog dialog = new ChooseMapRouteDialog();
        Bundle args = new Bundle();
        args.putDouble(ARG_START_LAT, start.latitude);
        args.putDouble(ARG_START_LNG, start.longitude);
        args.putDouble(ARG_END_LAT, end.latitude);
        args.putDouble(ARG_END_LNG, end.longitude);
        dialog.setArguments(args);
        return dialog;
    }

    // NEW: Create dialog with pre-fetched routes (for OSRM)
    public static ChooseMapRouteDialog newInstanceWithRoutes(List<RouteData> routes) {
        ChooseMapRouteDialog dialog = new ChooseMapRouteDialog();
        dialog.availableRoutes = routes;
        if (!routes.isEmpty()) {
            dialog.startPoint = routes.get(0).getStartPoint();
            dialog.endPoint = routes.get(0).getEndPoint();
        }
        return dialog;
    }

    public void setRouteSelectionListener(RouteSelectionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);

        if (getArguments() != null) {
            double startLat = getArguments().getDouble(ARG_START_LAT);
            double startLng = getArguments().getDouble(ARG_START_LNG);
            double endLat = getArguments().getDouble(ARG_END_LAT);
            double endLng = getArguments().getDouble(ARG_END_LNG);

            startPoint = new LatLng(startLat, startLng);
            endPoint = new LatLng(endLat, endLng);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_choose_map_route_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setRouteButton = view.findViewById(R.id.btn_set_route);
        setRouteButton.setEnabled(false);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_fragment_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setRouteButton.setOnClickListener(v -> {
            if (selectedRoute != null && listener != null) {
                listener.onRouteSelected(selectedRoute);
                dismiss();
            } else {
                Toast.makeText(getContext(), "Please select a route", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Add markers for start and end points
        mMap.addMarker(new MarkerOptions()
                .position(startPoint)
                .title("Start Point"));

        mMap.addMarker(new MarkerOptions()
                .position(endPoint)
                .title("End Point"));

        // If routes already loaded (OSRM), display them directly
        if (!availableRoutes.isEmpty()) {
            displayRoutesOnMap();
        } else {
            // Otherwise fetch from Google (requires API key)
            fetchRoutes();
        }

        // Move camera to show both points
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(startPoint);
        builder.include(endPoint);
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    private void fetchRoutes() {
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + startPoint.latitude + "," + startPoint.longitude +
                "&destination=" + endPoint.latitude + "," + endPoint.longitude +
                "&alternatives=true" +  // Request alternative routes
                "&key=" + GOOGLE_MAPS_API_KEY;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to fetch routes", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Route fetch failed", e);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    parseAndDisplayRoutes(responseData);
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error fetching routes", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void parseAndDisplayRoutes(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray routes = jsonObject.getJSONArray("routes");

            availableRoutes.clear();

            for (int i = 0; i < routes.length(); i++) {
                JSONObject route = routes.getJSONObject(i);

                // Get polyline
                String encodedPolyline = route.getJSONArray("legs")
                        .getJSONObject(0)
                        .getJSONObject("end_location")
                        .toString();

                // Get overview polyline
                String overviewPolyline = route.getJSONObject("overview_polyline")
                        .getString("points");

                // Decode polyline
                List<LatLng> decodedPath = decodePolyline(overviewPolyline);

                // Get route summary
                String summary = route.has("summary") ? route.getString("summary") : "Route " + (i + 1);

                // Get distance and duration
                JSONObject leg = route.getJSONArray("legs").getJSONObject(0);
                String distance = leg.getJSONObject("distance").getString("text");
                String duration = leg.getJSONObject("duration").getString("text");

                RouteData routeData = new RouteData(
                        decodedPath,
                        summary,
                        distance,
                        duration,
                        startPoint,
                        endPoint
                );

                availableRoutes.add(routeData);
            }

            // Display routes on map
            getActivity().runOnUiThread(() -> displayRoutesOnMap());

        } catch (Exception e) {
            Log.e(TAG, "Error parsing routes", e);
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "Error parsing routes", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void displayRoutesOnMap() {
        // Clear existing polylines
        for (Polyline polyline : drawnPolylines) {
            polyline.remove();
        }
        drawnPolylines.clear();

        // Draw each route with different colors
        int[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN};

        for (int i = 0; i < availableRoutes.size(); i++) {
            RouteData route = availableRoutes.get(i);
            int color = colors[i % colors.length];

            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(route.getPath())
                    .color(color)
                    .width(10)
                    .clickable(true);

            Polyline polyline = mMap.addPolyline(polylineOptions);
            polyline.setTag(route);
            drawnPolylines.add(polyline);
        }

        // Set up polyline click listener
        mMap.setOnPolylineClickListener(this::onRouteClicked);

        Toast.makeText(getContext(),
                availableRoutes.size() + " routes found. Tap a route to select.",
                Toast.LENGTH_LONG).show();
    }

    private void onRouteClicked(Polyline polyline) {
        // Reset all polylines to normal width
        for (Polyline p : drawnPolylines) {
            p.setWidth(10);
        }

        // Highlight selected polyline
        polyline.setWidth(15);

        selectedRoute = (RouteData) polyline.getTag();
        setRouteButton.setEnabled(true);

        if (selectedRoute != null) {
            Toast.makeText(getContext(),
                    "Selected: " + selectedRoute.getSummary() +
                            " (" + selectedRoute.getDistance() + ", " + selectedRoute.getDuration() + ")",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Decode Google's encoded polyline format
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}