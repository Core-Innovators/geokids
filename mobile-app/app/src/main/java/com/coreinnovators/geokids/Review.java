package com.coreinnovators.geokids;

public class Review {
    private String reviewId;
    private String reviewText;
    private String reviewerName;
    private String reviewerUserId;
    private String driverId;
    private long timestamp;
    private float rating;

    // Empty constructor for Firestore
    public Review() {}

    public Review(String reviewText, String reviewerName, String reviewerUserId,
                  String driverId, long timestamp, float rating) {
        this.reviewText = reviewText;
        this.reviewerName = reviewerName;
        this.reviewerUserId = reviewerUserId;
        this.driverId = driverId;
        this.timestamp = timestamp;
        this.rating = rating;
    }

    // Getters and Setters
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewerUserId() {
        return reviewerUserId;
    }

    public void setReviewerUserId(String reviewerUserId) {
        this.reviewerUserId = reviewerUserId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}