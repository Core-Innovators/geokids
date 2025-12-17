package com.coreinnovators.geokids;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteData {
    private List<LatLng> path;
    private String summary;
    private String distance;
    private String duration;
    private LatLng startPoint;
    private LatLng endPoint;

    public RouteData() {
        // Empty constructor for Firestore
    }

    public RouteData(List<LatLng> path, String summary, String distance,
                     String duration, LatLng startPoint, LatLng endPoint) {
        this.path = path;
        this.summary = summary;
        this.distance = distance;
        this.duration = duration;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    // Getters
    public List<LatLng> getPath() {
        return path;
    }

    public String getSummary() {
        return summary;
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }

    public LatLng getStartPoint() {
        return startPoint;
    }

    public LatLng getEndPoint() {
        return endPoint;
    }

    // Setters
    public void setPath(List<LatLng> path) {
        this.path = path;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setStartPoint(LatLng startPoint) {
        this.startPoint = startPoint;
    }

    public void setEndPoint(LatLng endPoint) {
        this.endPoint = endPoint;
    }

    /**
     * Convert route data to Firestore-friendly map
     * Stores all coordinates as lat/lng pairs
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        // Convert path to list of coordinate maps
        List<Map<String, Double>> pathCoordinates = new ArrayList<>();
        for (LatLng point : path) {
            Map<String, Double> coordinate = new HashMap<>();
            coordinate.put("lat", point.latitude);
            coordinate.put("lng", point.longitude);
            pathCoordinates.add(coordinate);
        }

        // Start point
        Map<String, Double> startCoordinate = new HashMap<>();
        startCoordinate.put("lat", startPoint.latitude);
        startCoordinate.put("lng", startPoint.longitude);

        // End point
        Map<String, Double> endCoordinate = new HashMap<>();
        endCoordinate.put("lat", endPoint.latitude);
        endCoordinate.put("lng", endPoint.longitude);

        map.put("pathCoordinates", pathCoordinates);
        map.put("summary", summary);
        map.put("distance", distance);
        map.put("duration", duration);
        map.put("startPoint", startCoordinate);
        map.put("endPoint", endCoordinate);

        return map;
    }

    /**
     * Create RouteData from Firestore map
     */
    public static RouteData fromMap(Map<String, Object> map) {
        RouteData route = new RouteData();

        // Parse path coordinates
        List<Map<String, Double>> pathCoordinates =
                (List<Map<String, Double>>) map.get("pathCoordinates");
        List<LatLng> path = new ArrayList<>();

        if (pathCoordinates != null) {
            for (Map<String, Double> coord : pathCoordinates) {
                path.add(new LatLng(coord.get("lat"), coord.get("lng")));
            }
        }

        route.setPath(path);
        route.setSummary((String) map.get("summary"));
        route.setDistance((String) map.get("distance"));
        route.setDuration((String) map.get("duration"));

        // Parse start point
        Map<String, Double> startCoord = (Map<String, Double>) map.get("startPoint");
        if (startCoord != null) {
            route.setStartPoint(new LatLng(startCoord.get("lat"), startCoord.get("lng")));
        }

        // Parse end point
        Map<String, Double> endCoord = (Map<String, Double>) map.get("endPoint");
        if (endCoord != null) {
            route.setEndPoint(new LatLng(endCoord.get("lat"), endCoord.get("lng")));
        }

        return route;
    }

    @Override
    public String toString() {
        return "RouteData{" +
                "summary='" + summary + '\'' +
                ", distance='" + distance + '\'' +
                ", duration='" + duration + '\'' +
                ", pathPoints=" + (path != null ? path.size() : 0) +
                '}';
    }
}