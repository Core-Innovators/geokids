package com.coreinnovators.geokids;

import java.util.HashMap;
import java.util.Map;

public class Driver {
    private String id;
    private String fullName;
    private String address;
    private String nic;
    private String birthday;
    private String contactNumber;
    private String profileImageUrl;
    private long createdAt;

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

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
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
        map.put("createdAt", createdAt);
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
                ", createdAt=" + createdAt +
                '}';
    }
}