package com.coreinnovators.geokids;

public class RideRequest {
    private String requestId;
    private String pickupAddress;
    private String dropoffAddress;
    private String parentName;
    private String parentId;
    private String parentContact1;
    private String parentContact2;
    private String parentNic;
    private String childName;
    private String childAge;
    private String childGrade;
    private String childSchool;
    private String childProfileImageUrl;
    private String status;
    private String driverId;
    private String driverName;
    private long createdAt;
    private long updatedAt;

    public RideRequest() {
        // Default constructor required for Firestore
    }

    // Getters
    public String getRequestId() {
        return requestId;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public String getDropoffAddress() {
        return dropoffAddress;
    }

    public String getParentName() {
        return parentName;
    }

    public String getParentId() {
        return parentId;
    }

    public String getParentContact1() {
        return parentContact1;
    }

    public String getParentContact2() {
        return parentContact2;
    }

    public String getParentNic() {
        return parentNic;
    }

    public String getChildName() {
        return childName;
    }

    public String getChildAge() {
        return childAge;
    }

    public String getChildGrade() {
        return childGrade;
    }

    public String getChildSchool() {
        return childSchool;
    }

    public String getChildProfileImageUrl() {
        return childProfileImageUrl;
    }

    public String getStatus() {
        return status;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public void setDropoffAddress(String dropoffAddress) {
        this.dropoffAddress = dropoffAddress;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setParentContact1(String parentContact1) {
        this.parentContact1 = parentContact1;
    }

    public void setParentContact2(String parentContact2) {
        this.parentContact2 = parentContact2;
    }

    public void setParentNic(String parentNic) {
        this.parentNic = parentNic;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public void setChildAge(String childAge) {
        this.childAge = childAge;
    }

    public void setChildGrade(String childGrade) {
        this.childGrade = childGrade;
    }

    public void setChildSchool(String childSchool) {
        this.childSchool = childSchool;
    }

    public void setChildProfileImageUrl(String childProfileImageUrl) {
        this.childProfileImageUrl = childProfileImageUrl;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper method to get route text - format addresses nicely
    public String getRouteText() {
        String pickup = pickupAddress != null ? formatAddress(pickupAddress) : "Unknown";
        String dropoff = dropoffAddress != null ? formatAddress(dropoffAddress) : "Unknown";
        return pickup + " to " + dropoff;
    }

    // Format address to show shorter version
    private String formatAddress(String address) {
        if (address == null) return "Unknown";
        // If address is too long, show first part only
        String[] parts = address.split(",");
        if (parts.length > 0) {
            return parts[0].trim();
        }
        return address;
    }
}