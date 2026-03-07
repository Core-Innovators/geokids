package com.coreinnovators.geokids;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link Review} model class.
 *
 * Test Cases Covered:
 *  TC-V01 – Default constructor creates empty object
 *  TC-V02 – Parameterised constructor sets all fields
 *  TC-V03 – setRating boundary – minimum (0.0)
 *  TC-V04 – setRating boundary – maximum (5.0)
 *  TC-V05 – Setter / getter round-trip for reviewText
 */
public class ReviewTest {

    private Review review;

    @Before
    public void setUp() {
        review = new Review();
    }

    // -----------------------------------------------------------------------
    // TC-V01  Default constructor   (all null / zero)
    // -----------------------------------------------------------------------
    @Test
    public void TC_V01_defaultConstructor_emptyObject() {
        assertNull(review.getReviewId());
        assertNull(review.getReviewText());
        assertNull(review.getReviewerName());
        assertNull(review.getReviewerUserId());
        assertNull(review.getDriverId());
        assertEquals(0L, review.getTimestamp());
        assertEquals(0.0f, review.getRating(), 0.001f);
    }

    // -----------------------------------------------------------------------
    // TC-V02  Parameterised constructor
    // -----------------------------------------------------------------------
    @Test
    public void TC_V02_parametrisedConstructor_setsFields() {
        Review r = new Review("Great driver!", "Ayesha Perera",
                "parent-001", "driver-002", 1_700_000_000L, 4.5f);

        assertEquals("Great driver!", r.getReviewText());
        assertEquals("Ayesha Perera", r.getReviewerName());
        assertEquals("parent-001", r.getReviewerUserId());
        assertEquals("driver-002", r.getDriverId());
        assertEquals(1_700_000_000L, r.getTimestamp());
        assertEquals(4.5f, r.getRating(), 0.001f);
    }

    // -----------------------------------------------------------------------
    // TC-V03  Rating – minimum boundary (0)
    // -----------------------------------------------------------------------
    @Test
    public void TC_V03_rating_minimumBoundary() {
        review.setRating(0.0f);
        assertEquals(0.0f, review.getRating(), 0.001f);
    }

    // -----------------------------------------------------------------------
    // TC-V04  Rating – maximum boundary (5)
    // -----------------------------------------------------------------------
    @Test
    public void TC_V04_rating_maximumBoundary() {
        review.setRating(5.0f);
        assertEquals(5.0f, review.getRating(), 0.001f);
    }

    // -----------------------------------------------------------------------
    // TC-V05  Setter / getter for reviewText
    // -----------------------------------------------------------------------
    @Test
    public void TC_V05_settersGetters_reviewTextRoundTrip() {
        review.setReviewId("rev-999");
        review.setReviewText("Very punctual and safe driver.");
        review.setReviewerName("Saman Kumar");
        review.setReviewerUserId("user-555");
        review.setDriverId("driver-444");
        review.setTimestamp(1_720_000_000L);
        review.setRating(3.5f);

        assertEquals("rev-999",                        review.getReviewId());
        assertEquals("Very punctual and safe driver.", review.getReviewText());
        assertEquals("Saman Kumar",                   review.getReviewerName());
        assertEquals("user-555",                      review.getReviewerUserId());
        assertEquals("driver-444",                    review.getDriverId());
        assertEquals(1_720_000_000L,                  review.getTimestamp());
        assertEquals(3.5f,                            review.getRating(), 0.001f);
    }
}
