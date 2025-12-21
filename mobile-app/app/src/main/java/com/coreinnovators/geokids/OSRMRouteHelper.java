package com.coreinnovators.geokids;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OSRM (OpenStreetMap Routing Machine) - 100% FREE Alternative to Google Directions API
 * No API key needed, no billing, completely open source!
 */
public class OSRMRouteHelper {

    private static final String TAG = "OSRMRouteHelper";
    // Public OSRM demo server (FREE, no API key needed!)
    private static final String OSRM_BASE_URL = "https://router.project-osrm.org/route/v1/driving/";

    public interface RouteCallback {
        void onRouteFetched(List<RouteData> routes);
        void onError(String error);
    }

    /**
     * Fetch route from OSRM - Completely FREE, no API key needed!
     *
     * @param start Starting point
     * @param end Ending point
     * @param callback Callback with route results
     */
    public static void fetchRoute(LatLng start, LatLng end, RouteCallback callback) {
        new Thread(() -> {
            try {
                // Build OSRM URL: lng,lat (note: OSRM uses lng,lat not lat,lng!)
                String url = OSRM_BASE_URL +
                        start.longitude + "," + start.latitude + ";" +
                        end.longitude + "," + end.latitude +
                        "?overview=full&geometries=geojson&alternatives=3"; // Get up to 3 alternatives

                Log.d(TAG, "OSRM URL: " + url);

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                Log.d(TAG, "OSRM Response: " + responseBody);

                if (response.isSuccessful()) {
                    List<RouteData> routes = parseOSRMResponse(responseBody, start, end);
                    callback.onRouteFetched(routes);
                } else {
                    callback.onError("Failed to fetch route: " + response.code());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error fetching OSRM route", e);
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    private static List<RouteData> parseOSRMResponse(String jsonResponse, LatLng start, LatLng end)
            throws Exception {
        List<RouteData> routes = new ArrayList<>();

        JSONObject json = new JSONObject(jsonResponse);
        String code = json.getString("code");

        if (!"Ok".equals(code)) {
            throw new Exception("OSRM returned error code: " + code);
        }

        JSONArray routesArray = json.getJSONArray("routes");

        for (int i = 0; i < routesArray.length(); i++) {
            JSONObject route = routesArray.getJSONObject(i);

            // Get geometry (route path)
            JSONObject geometry = route.getJSONObject("geometry");
            JSONArray coordinates = geometry.getJSONArray("coordinates");

            // Decode coordinates (OSRM returns [lng, lat] format)
            List<LatLng> path = new ArrayList<>();
            for (int j = 0; j < coordinates.length(); j++) {
                JSONArray coord = coordinates.getJSONArray(j);
                double lng = coord.getDouble(0);
                double lat = coord.getDouble(1);
                path.add(new LatLng(lat, lng));
            }

            // Get distance and duration
            double distanceMeters = route.getDouble("distance");
            double durationSeconds = route.getDouble("duration");

            // Convert to readable format
            String distance = formatDistance(distanceMeters);
            String duration = formatDuration(durationSeconds);

            // Get route name/summary (if available)
            String summary = "Route " + (i + 1);
            if (route.has("legs")) {
                JSONArray legs = route.getJSONArray("legs");
                if (legs.length() > 0) {
                    JSONObject leg = legs.getJSONObject(0);
                    if (leg.has("summary")) {
                        summary = leg.getString("summary");
                    }
                }
            }

            RouteData routeData = new RouteData(path, summary, distance, duration, start, end);
            routes.add(routeData);

            Log.d(TAG, "Parsed route " + (i+1) + ": " + summary + " - " + distance + ", " + duration);
        }

        return routes;
    }

    private static String formatDistance(double meters) {
        if (meters < 1000) {
            return String.format("%.0f m", meters);
        } else {
            return String.format("%.1f km", meters / 1000);
        }
    }

    private static String formatDuration(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);

        if (hours > 0) {
            return String.format("%d hr %d min", hours, minutes);
        } else {
            return String.format("%d min", minutes);
        }
    }

    /**
     * Geocode location name to coordinates using Nominatim (OpenStreetMap)
     * Also 100% FREE, no API key needed!
     */
    public static void geocodeLocation(String locationName, GeocodeCallback callback) {
        new Thread(() -> {
            try {
                String url = "https://nominatim.openstreetmap.org/search?" +
                        "q=" + locationName.replace(" ", "+") +
                        "&format=json&limit=1";

                Log.d(TAG, "Nominatim URL: " + url);

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "GeoKidsApp/1.0") // Required by Nominatim
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                Log.d(TAG, "Nominatim response: " + responseBody);

                JSONArray results = new JSONArray(responseBody);

                if (results.length() > 0) {
                    JSONObject location = results.getJSONObject(0);
                    double lat = location.getDouble("lat");
                    double lon = location.getDouble("lon");

                    Log.d(TAG, "Geocoded " + locationName + " to: " + lat + ", " + lon);
                    callback.onGeocodeResult(new LatLng(lat, lon));
                } else {
                    Log.e(TAG, "No results for: " + locationName);
                    callback.onGeocodeResult(null);
                }

            } catch (Exception e) {
                Log.e(TAG, "Geocoding error", e);
                callback.onGeocodeResult(null);
            }
        }).start();
    }

    public interface GeocodeCallback {
        void onGeocodeResult(LatLng latLng);
    }
}