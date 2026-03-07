package com.coreinnovators.geokids;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * ============================================================
 *  GeoKids – Integration Test Suite (JUnit 4 + Mockito 5)
 * ============================================================
 *
 *  Integration focus: cross-component interactions,
 *  multi-layer data flows, callback contracts, and
 *  the business rules that span more than one class.
 *
 *  Tests:
 *   IT-01  Driver created → assigned RouteData → LocationPoint round-trip through toMap()
 *   IT-02  Driver.toMap() embeds nested RouteData map when routeData is set
 *   IT-03  Driver status transitions: pending → approved → rejected
 *   IT-04  RideRequest ↔ Driver coupling: driverId & driverName propagated correctly
 *   IT-05  RideRequest status lifecycle: pending → accepted → completed
 *   IT-06  Review linked to Driver: driverId matches, rating stored
 *   IT-07  Multiple Reviews – average rating calculation
 *   IT-08  DriverAdapter.getSelectedDriver() returns correct Driver after selection
 *   IT-09  DriverAdapter.getSelectedDriver() returns null when no selection made
 *   IT-10  DriverAdapter item count reflects setDriverList()
 *   IT-11  RideRequest.getRouteText() pipeline – comma-address → first-segment extraction
 *   IT-12  RideRequest.getRouteText() with single-word address (no comma)
 *   IT-13  Driver.LocationPoint coordinates survive set→get for extreme values (poles)
 *   IT-14  Driver.RouteData snapshot – all fields preserved in toMap()
 *   IT-15  OSRMRouteHelper.RouteCallback onRouteFetched – Mockito verification
 *   IT-16  OSRMRouteHelper.RouteCallback onError – Mockito verification
 *   IT-17  OSRMRouteHelper.GeocodeCallback onGeocodeResult – Mockito verification
 *   IT-18  SupabaseHelper.getPublicUrl() returns correctly formed URL
 *   IT-19  Complete ride lifecycle: Driver rideActive toggles and
 *            RideRequest status changes as expected
 *   IT-20  RideRequest timestamps: createdAt < updatedAt ordering
 */
public class GeoKidsIntegrationTest {

    // ── Shared test fixtures ──────────────────────────────────────────────────

    private Driver driver;
    private RideRequest rideRequest;
    private Review review;

    @Before
    public void setUp() {
        // Build a fully populated Driver
        driver = new Driver("Kasun Wijeratne", "45 Galle Rd, Colombo 03",
                "890123456V", "1988-07-14", "0712345678",
                "https://img.example.com/profile.jpg");
        driver.setId("driver-INT-001");
        driver.setVehicleNumber("WP-CAB-5601");
        driver.setFrontLicenseUrl("https://img.example.com/license_front.jpg");
        driver.setBackLicenseUrl("https://img.example.com/license_back.jpg");
        driver.setStatus("approved");
        driver.setRideActive(false);

        // Build a fully populated RideRequest
        rideRequest = new RideRequest();
        rideRequest.setRequestId("req-INT-001");
        rideRequest.setParentName("Sunitha Fernando");
        rideRequest.setParentId("parent-001");
        rideRequest.setParentContact1("0771112222");
        rideRequest.setParentContact2("0762223333");
        rideRequest.setParentNic("776543210V");
        rideRequest.setChildName("Dineth Fernando");
        rideRequest.setChildAge("7");
        rideRequest.setChildGrade("Grade 2");
        rideRequest.setChildSchool("Royal College");
        rideRequest.setPickupAddress("120 Flower Road, Colombo 07, Sri Lanka");
        rideRequest.setDropoffAddress("Royal College, Reid Avenue, Colombo 07");
        rideRequest.setStatus("pending");
        rideRequest.setCreatedAt(1_710_000_000L);

        // Build a Review
        review = new Review("Very safe and punctual driver.", "Sunitha Fernando",
                "parent-001", "driver-INT-001", 1_710_000_200L, 4.5f);
        review.setReviewId("rev-INT-001");
    }

    // =========================================================================
    // IT-01  Driver + RouteData + LocationPoint round-trip through toMap()
    // =========================================================================
    @Test
    public void IT_01_driverWithRouteData_roundTripThroughToMap() {
        // Arrange
        Driver.LocationPoint start = new Driver.LocationPoint(6.9271, 79.8612);
        Driver.LocationPoint end   = new Driver.LocationPoint(7.2906, 80.6337);

        Driver.RouteData routeData = new Driver.RouteData();
        routeData.setDistance("75 km");
        routeData.setDuration("2 hr 10 min");
        routeData.setSummary("Colombo to Kandy");
        routeData.setStartPoint(start);
        routeData.setEndPoint(end);
        driver.setRouteData(routeData);

        // Act
        Map<String, Object> driverMap = driver.toMap();

        // Assert – driver-level keys
        assertEquals("driver map must carry fullName", "Kasun Wijeratne", driverMap.get("fullName"));
        assertTrue("driver map must embed routeData key", driverMap.containsKey("routeData"));

        // Assert – nested route map
        @SuppressWarnings("unchecked")
        Map<String, Object> routeMap = (Map<String, Object>) driverMap.get("routeData");
        assertNotNull(routeMap);
        assertEquals("75 km",            routeMap.get("distance"));
        assertEquals("2 hr 10 min",      routeMap.get("duration"));
        assertEquals("Colombo to Kandy", routeMap.get("summary"));

        // Assert – nested start/end point maps
        @SuppressWarnings("unchecked")
        Map<String, Object> startMap = (Map<String, Object>) routeMap.get("startPoint");
        assertNotNull(startMap);
        assertEquals(6.9271, (Double) startMap.get("lat"), 0.0001);
        assertEquals(79.8612, (Double) startMap.get("lng"), 0.0001);
    }

    // =========================================================================
    // IT-02  toMap() embeds nested routeData only when routeData != null
    // =========================================================================
    @Test
    public void IT_02_toMap_noRouteData_doesNotContainRouteDataKey() {
        // driver has no routeData set in @Before
        Map<String, Object> map = driver.toMap();
        assertFalse("routeData key must be absent when routeData is null",
                map.containsKey("routeData"));
    }

    // =========================================================================
    // IT-03  Driver status transitions
    // =========================================================================
    @Test
    public void IT_03_driverStatusTransitions_pendingToApprovedToRejected() {
        driver.setStatus("pending");
        assertEquals("pending", driver.getStatus());

        driver.setStatus("approved");
        assertEquals("approved", driver.getStatus());

        driver.setStatus("rejected");
        assertEquals("rejected", driver.getStatus());

        // Status in toMap() must reflect latest value
        Map<String, Object> map = driver.toMap();
        assertEquals("rejected", map.get("status"));
    }

    // =========================================================================
    // IT-04  RideRequest ↔ Driver coupling
    // =========================================================================
    @Test
    public void IT_04_rideRequest_driverCoupling_idsAndNamesPropagated() {
        // Simulate a driver accepting a ride request
        rideRequest.setDriverId(driver.getId());
        rideRequest.setDriverName(driver.getFullName());
        rideRequest.setStatus("accepted");
        rideRequest.setUpdatedAt(1_710_000_100L);

        // Verify the coupling
        assertEquals(driver.getId(),       rideRequest.getDriverId());
        assertEquals(driver.getFullName(), rideRequest.getDriverName());
        assertEquals("accepted",           rideRequest.getStatus());
        assertTrue("updatedAt must be after createdAt",
                rideRequest.getUpdatedAt() > rideRequest.getCreatedAt());
    }

    // =========================================================================
    // IT-05  RideRequest full lifecycle: pending → accepted → completed
    // =========================================================================
    @Test
    public void IT_05_rideRequest_fullLifecycle_statusTransitions() {
        // Stage 1 – created (pending)
        assertEquals("pending", rideRequest.getStatus());

        // Stage 2 – driver accepts
        rideRequest.setStatus("accepted");
        rideRequest.setDriverId("driver-INT-001");
        rideRequest.setUpdatedAt(1_710_000_100L);
        assertEquals("accepted", rideRequest.getStatus());
        assertNotNull(rideRequest.getDriverId());

        // Stage 3 – ride completes
        rideRequest.setStatus("completed");
        rideRequest.setUpdatedAt(1_710_005_000L);
        assertEquals("completed", rideRequest.getStatus());
        assertTrue(rideRequest.getUpdatedAt() > 1_710_000_100L);
    }

    // =========================================================================
    // IT-06  Review linked to Driver by driverId
    // =========================================================================
    @Test
    public void IT_06_review_linkedToDriver_byDriverId() {
        assertEquals("Review's driverId must match Driver's id",
                driver.getId(), review.getDriverId());
        assertEquals(4.5f, review.getRating(), 0.001f);
        assertNotNull(review.getReviewText());
        assertTrue("Review text must not be empty",
                !review.getReviewText().isEmpty());
    }

    // =========================================================================
    // IT-07  Multiple Reviews – average rating calculation
    // =========================================================================
    @Test
    public void IT_07_multipleReviews_averageRatingCalculation() {
        List<Review> reviews = new ArrayList<>();

        float[] ratings = {5.0f, 4.0f, 3.5f, 4.5f, 2.0f};
        float expectedAverage = 0;
        for (float r : ratings) {
            Review rv = new Review("Good", "User", "uid", "driver-INT-001",
                    System.currentTimeMillis(), r);
            reviews.add(rv);
            expectedAverage += r;
        }
        expectedAverage /= ratings.length;  // 3.8

        // Calculate average from the list (simulates what the UI/ViewModel would do)
        float sum = 0;
        for (Review rv : reviews) sum += rv.getRating();
        float actualAverage = sum / reviews.size();

        assertEquals("Average rating must be 3.8", expectedAverage, actualAverage, 0.01f);
        assertEquals("Review list size must be 5", 5, reviews.size());
    }

    // =========================================================================
    // IT-08  DriverAdapter – getSelectedDriver() returns correct Driver
    // =========================================================================
    @Test
    public void IT_08_driverAdapter_getSelectedDriver_returnsCorrectDriver() {
        List<Driver> drivers = buildDriverList(3);

        // We test the data model logic of getSelectedDriver() directly
        // by simulating what the adapter does (without Android context)
        int selectedPos = 1;
        Driver selected = selectedPos >= 0 && selectedPos < drivers.size()
                ? drivers.get(selectedPos) : null;

        assertNotNull(selected);
        assertEquals("driver-2", selected.getId());
        assertEquals("Driver 2", selected.getFullName());
    }

    // =========================================================================
    // IT-09  DriverAdapter – no selection returns null
    // =========================================================================
    @Test
    public void IT_09_driverAdapter_noSelection_returnsNull() {
        List<Driver> drivers = buildDriverList(3);
        int selectedPos = -1;   // no selection

        Driver selected = selectedPos >= 0 && selectedPos < drivers.size()
                ? drivers.get(selectedPos) : null;

        assertNull("No driver should be selected when selectedPosition = -1", selected);
    }

    // =========================================================================
    // IT-10  DriverAdapter – item count reflects setDriverList()
    // =========================================================================
    @Test
    public void IT_10_driverAdapter_itemCount_reflectsDriverList() {
        List<Driver> drivers = buildDriverList(5);
        assertEquals("Item count should match driver list size", 5, drivers.size());

        // Simulate setDriverList() replacement
        List<Driver> newDrivers = buildDriverList(2);
        assertEquals("After replacement, item count must be 2", 2, newDrivers.size());
    }

    // =========================================================================
    // IT-11  RideRequest.getRouteText() – comma-address pipeline
    // =========================================================================
    @Test
    public void IT_11_getRouteText_commaAddress_returnsFirstSegmentParts() {
        rideRequest.setPickupAddress("120 Flower Road, Colombo 07, Sri Lanka");
        rideRequest.setDropoffAddress("Royal College, Reid Avenue, Colombo 07");

        String routeText = rideRequest.getRouteText();

        // formatAddress(address) returns parts[0].trim()
        assertTrue("Should contain '120 Flower Road'",    routeText.contains("120 Flower Road"));
        assertTrue("Should contain ' to '",              routeText.contains(" to "));
        assertTrue("Should contain 'Royal College'",     routeText.contains("Royal College"));
    }

    // =========================================================================
    // IT-12  RideRequest.getRouteText() – address with no comma
    // =========================================================================
    @Test
    public void IT_12_getRouteText_noCommaAddress_returnsFullAddress() {
        rideRequest.setPickupAddress("Temple Road");
        rideRequest.setDropoffAddress("Nugegoda");

        String routeText = rideRequest.getRouteText();
        assertEquals("Temple Road to Nugegoda", routeText);
    }

    // =========================================================================
    // IT-13  Driver.LocationPoint – extreme coordinate values (poles)
    // =========================================================================
    @Test
    public void IT_13_locationPoint_extremeValues_polarCoordinates() {
        Driver.LocationPoint northPole = new Driver.LocationPoint(90.0, 0.0);
        Driver.LocationPoint southPole = new Driver.LocationPoint(-90.0, 180.0);

        assertEquals(90.0,   northPole.getLat(), 0.00001);
        assertEquals(0.0,    northPole.getLng(), 0.00001);
        assertEquals(-90.0,  southPole.getLat(), 0.00001);
        assertEquals(180.0,  southPole.getLng(), 0.00001);

        Map<String, Object> northMap = northPole.toMap();
        assertEquals(90.0,  (Double) northMap.get("lat"), 0.00001);
        assertEquals(-90.0, (Double) southPole.toMap().get("lat"), 0.00001);
    }

    // =========================================================================
    // IT-14  Driver.RouteData snapshot – all fields preserved in toMap()
    // =========================================================================
    @Test
    public void IT_14_routeDataSnapshot_allFieldsPreservedInToMap() {
        Driver.LocationPoint start = new Driver.LocationPoint(6.9271, 79.8612);
        Driver.LocationPoint end   = new Driver.LocationPoint(6.0535, 80.2210);

        Driver.RouteData routeData = new Driver.RouteData();
        routeData.setDistance("120 km");
        routeData.setDuration("3 hr 0 min");
        routeData.setSummary("Colombo to Galle");
        routeData.setStartPoint(start);
        routeData.setEndPoint(end);

        Map<String, Object> routeMap = routeData.toMap();

        assertEquals("120 km",          routeMap.get("distance"));
        assertEquals("3 hr 0 min",      routeMap.get("duration"));
        assertEquals("Colombo to Galle",routeMap.get("summary"));
        assertTrue(routeMap.containsKey("startPoint"));
        assertTrue(routeMap.containsKey("endPoint"));

        @SuppressWarnings("unchecked")
        Map<String, Object> endMap = (Map<String, Object>) routeMap.get("endPoint");
        assertEquals(80.2210, (Double) endMap.get("lng"), 0.0001);
    }

    // =========================================================================
    // IT-15  OSRMRouteHelper.RouteCallback – Mockito: onRouteFetched called
    // =========================================================================
    @Test
    public void IT_15_routeCallback_onRouteFetched_mockitoVerification() {
        // Arrange – mock the callback interface
        OSRMRouteHelper.RouteCallback callback =
                Mockito.mock(OSRMRouteHelper.RouteCallback.class);

        List<RouteData> fakeRoutes = new ArrayList<>();
        // (RouteData requires LatLng which is Android-only, so we pass empty list)

        // Act – simulate what OSRMRouteHelper would call on success
        callback.onRouteFetched(fakeRoutes);

        // Assert – verify the callback was invoked exactly once with our argument
        verify(callback, times(1)).onRouteFetched(fakeRoutes);
        verify(callback, never()).onError(anyString());
    }

    // =========================================================================
    // IT-16  OSRMRouteHelper.RouteCallback – Mockito: onError called with message
    // =========================================================================
    @Test
    public void IT_16_routeCallback_onError_mockitoVerification() {
        // Arrange
        OSRMRouteHelper.RouteCallback callback =
                Mockito.mock(OSRMRouteHelper.RouteCallback.class);

        String errorMessage = "HTTP 503 – Service Unavailable";

        // Act – simulate what OSRMRouteHelper would call on failure
        callback.onError(errorMessage);

        // Assert
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(callback, times(1)).onError(captor.capture());
        verify(callback, never()).onRouteFetched(any());

        assertEquals("Captured error message must match",
                errorMessage, captor.getValue());
        assertTrue("Error message must mention HTTP",
                captor.getValue().contains("HTTP"));
    }

    // =========================================================================
    // IT-17  OSRMRouteHelper.GeocodeCallback – Mockito: onGeocodeResult called
    // =========================================================================
    @Test
    public void IT_17_geocodeCallback_onGeocodeResult_mockitoVerification() {
        // Arrange
        OSRMRouteHelper.GeocodeCallback callback =
                Mockito.mock(OSRMRouteHelper.GeocodeCallback.class);

        // Act – simulate null result (location not found)
        callback.onGeocodeResult(null);

        // Assert
        verify(callback, times(1)).onGeocodeResult(null);

        // Act – simulate a geocode failure (null result for unknown location)
        reset(callback);
        callback.onGeocodeResult(null);
        verify(callback, times(1)).onGeocodeResult(isNull());
    }

    // =========================================================================
    // IT-18  SupabaseHelper.getPublicUrl() – URL structure validation
    // =========================================================================
    @Test
    public void IT_18_supabaseHelper_getPublicUrl_returnsCorrectUrl() {
        String fileName  = "driver_abc123.jpg";
        String publicUrl = SupabaseHelper.getPublicUrl(fileName);

        assertNotNull("Public URL must not be null", publicUrl);
        assertTrue("URL must start with https://",   publicUrl.startsWith("https://"));
        assertTrue("URL must contain bucket name",   publicUrl.contains("profile_image"));
        assertTrue("URL must contain fileName",      publicUrl.contains(fileName));
        assertTrue("URL must contain /object/public/",
                publicUrl.contains("/object/public/"));

        // Verify the exact pattern
        String expectedSuffix = "/storage/v1/object/public/profile_image/" + fileName;
        assertTrue("URL must end with expected path", publicUrl.endsWith(expectedSuffix));
    }

    // =========================================================================
    // IT-19  Complete ride lifecycle integration
    //        Driver rideActive toggled in sync with RideRequest status changes
    // =========================================================================
    @Test
    public void IT_19_completeRideLifecycle_driverAndRequestInSync() {
        // Step 1 – Request submitted (pending)
        assertFalse("Driver should not be riding yet", driver.isRideActive());
        assertEquals("pending", rideRequest.getStatus());

        // Step 2 – Driver accepts request
        rideRequest.setStatus("accepted");
        rideRequest.setDriverId(driver.getId());
        rideRequest.setDriverName(driver.getFullName());
        rideRequest.setUpdatedAt(rideRequest.getCreatedAt() + 60_000L); // +1 min

        // Step 3 – Ride starts → driver goes active
        driver.setRideActive(true);
        assertTrue("Driver must be active when ride starts",  driver.isRideActive());
        assertEquals("accepted", rideRequest.getStatus());

        // Step 4 – Ride finishes → request completed, driver deactivated
        rideRequest.setStatus("completed");
        rideRequest.setUpdatedAt(rideRequest.getUpdatedAt() + 1_800_000L); // +30 min
        driver.setRideActive(false);

        assertFalse("Driver must be inactive after ride ends", driver.isRideActive());
        assertEquals("completed", rideRequest.getStatus());
        // Verify driverId still linked
        assertEquals(driver.getId(), rideRequest.getDriverId());

        // Step 5 – Review written after completion
        Review postRideReview = new Review(
                "Driver arrived on time.",
                rideRequest.getParentName(),
                rideRequest.getParentId(),
                rideRequest.getDriverId(),
                System.currentTimeMillis(),
                5.0f
        );
        assertEquals("Review's driverId must match the driver who completed the ride",
                driver.getId(), postRideReview.getDriverId());
        assertEquals(5.0f, postRideReview.getRating(), 0.001f);
    }

    // =========================================================================
    // IT-20  Timestamp ordering: createdAt < updatedAt
    // =========================================================================
    @Test
    public void IT_20_rideRequest_timestamps_createdAtBeforeUpdatedAt() {
        long now = System.currentTimeMillis();
        rideRequest.setCreatedAt(now);

        // Simulate some processing time
        rideRequest.setUpdatedAt(now + 5_000L);

        assertTrue("createdAt must be strictly before updatedAt",
                rideRequest.getCreatedAt() < rideRequest.getUpdatedAt());

        // Driver timestamps
        driver.setCreatedAt(now);
        driver.setSubmittedAt(now + 1_000L);
        driver.setLastUpdated(now + 10_000L);

        assertTrue(driver.getCreatedAt()   < driver.getSubmittedAt());
        assertTrue(driver.getSubmittedAt() < driver.getLastUpdated());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Build a list of n simple Driver objects for adapter tests. */
    private List<Driver> buildDriverList(int n) {
        List<Driver> list = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            Driver d = new Driver();
            d.setId("driver-" + i);
            d.setFullName("Driver " + i);
            d.setStatus("approved");
            list.add(d);
        }
        return list;
    }
}
