package com.coreinnovators.geokids;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Driver {
    private String id;
    private String fullName;
    private String address;
    private String nic;
    private String birthday;
    private String contactNumber;
    private String profileImageUrl;
    private String frontLicenseUrl;
    private String backLicenseUrl;
    private List<String> vehicleImageUrls;
    private String status;
    private boolean rideActive;
    private long createdAt;
    private long lastUpdated;
    private long submittedAt;
    private RouteData routeData;

    // Empty constructor required for Firestore
    public Driver() {
    }

    public Driver(String fullName, String address, String nic, String birthday,
                  String contactNumber, String profileImageUrl) {
        this.fullName = fullName;
        this.address = address;
        this.nic = nic;
        this.birthday = birthday;
        this.contactNumber = contactNumber;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Alias method for compatibility
    public String getDriverId() {
        return id;
    }

    public void setDriverId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getFrontLicenseUrl() {
        return frontLicenseUrl;
    }

    public void setFrontLicenseUrl(String frontLicenseUrl) {
        this.frontLicenseUrl = frontLicenseUrl;
    }

    public String getBackLicenseUrl() {
        return backLicenseUrl;
    }

    public void setBackLicenseUrl(String backLicenseUrl) {
        this.backLicenseUrl = backLicenseUrl;
    }

    public List<String> getVehicleImageUrls() {
        return vehicleImageUrls;
    }

    public void setVehicleImageUrls(List<String> vehicleImageUrls) {
        this.vehicleImageUrls = vehicleImageUrls;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRideActive() {
        return rideActive;
    }

    public void setRideActive(boolean rideActive) {
        this.rideActive = rideActive;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public long getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(long submittedAt) {
        this.submittedAt = submittedAt;
    }

    public RouteData getRouteData() {
        return routeData;
    }

    public void setRouteData(RouteData routeData) {
        this.routeData = routeData;
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("fullName", fullName);
        map.put("address", address);
        map.put("nic", nic);
        map.put("birthday", birthday);
        map.put("contactNumber", contactNumber);
        map.put("profileImageUrl", profileImageUrl);
        map.put("frontLicenseUrl", frontLicenseUrl);
        map.put("backLicenseUrl", backLicenseUrl);
        map.put("vehicleImageUrls", vehicleImageUrls);
        map.put("status", status);
        map.put("rideActive", rideActive);
        map.put("createdAt", createdAt);
        map.put("lastUpdated", lastUpdated);
        map.put("submittedAt", submittedAt);
        if (routeData != null) {
            map.put("routeData", routeData.toMap());
        }
        return map;
    }

    @Override
    public String toString() {
        return "Driver{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", address='" + address + '\'' +
                ", nic='" + nic + '\'' +
                ", birthday='" + birthday + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", status='" + status + '\'' +
                ", rideActive=" + rideActive +
                ", createdAt=" + createdAt +
                '}';
    }

    // Nested class for RouteData
    public static class RouteData {
        private String distance;
        private String duration;
        private String summary;
        private LocationPoint startPoint;
        private LocationPoint endPoint;
        private List<LocationPoint> pathCoordinates;

        public RouteData() {
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public LocationPoint getStartPoint() {
            return startPoint;
        }

        public void setStartPoint(LocationPoint startPoint) {
            this.startPoint = startPoint;
        }

        public LocationPoint getEndPoint() {
            return endPoint;
        }

        public void setEndPoint(LocationPoint endPoint) {
            this.endPoint = endPoint;
        }

        public List<LocationPoint> getPathCoordinates() {
            return pathCoordinates;
        }

        public void setPathCoordinates(List<LocationPoint> pathCoordinates) {
            this.pathCoordinates = pathCoordinates;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("distance", distance);
            map.put("duration", duration);
            map.put("summary", summary);
            if (startPoint != null) {
                map.put("startPoint", startPoint.toMap());
            }
            if (endPoint != null) {
                map.put("endPoint", endPoint.toMap());
            }
            return map;
        }
    }

    // Nested class for Location Points
    public static class LocationPoint {
        private double lat;
        private double lng;

        public LocationPoint() {
        }

        public LocationPoint(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("lat", lat);
            map.put("lng", lng);
            return map;
        }
    }
}