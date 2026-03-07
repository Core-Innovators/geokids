package com.coreinnovators.geokids;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link RideRequest} model class.
 *
 * Test Cases Covered:
 *  TC-R01 – Default constructor produces all-null / zero fields
 *  TC-R02 – Full setter / getter round-trip
 *  TC-R03 – getRouteText() with both addresses present
 *  TC-R04 – getRouteText() with null pickup address
 *  TC-R05 – getRouteText() with null dropoff address
 */
public class RideRequestTest {

    private RideRequest request;

    @Before
    public void setUp() {
        request = new RideRequest();
    }

    // -----------------------------------------------------------------------
    // TC-R01  Default constructor
    // -----------------------------------------------------------------------
    @Test
    public void TC_R01_defaultConstructor_allNullOrZero() {
        assertNull(request.getRequestId());
        assertNull(request.getPickupAddress());
        assertNull(request.getDropoffAddress());
        assertNull(request.getParentName());
        assertNull(request.getParentId());
        assertNull(request.getParentContact1());
        assertNull(request.getParentContact2());
        assertNull(request.getParentNic());
        assertNull(request.getChildName());
        assertNull(request.getChildAge());
        assertNull(request.getChildGrade());
        assertNull(request.getChildSchool());
        assertNull(request.getStatus());
        assertNull(request.getDriverId());
        assertNull(request.getDriverName());
        assertEquals(0L, request.getCreatedAt());
        assertEquals(0L, request.getUpdatedAt());
    }

    // -----------------------------------------------------------------------
    // TC-R02  Full setter / getter round-trip
    // -----------------------------------------------------------------------
    @Test
    public void TC_R02_settersGetters_roundTrip() {
        request.setRequestId("req-001");
        request.setPickupAddress("123 School Lane, Colombo");
        request.setDropoffAddress("45 Main St, Kandy");
        request.setParentName("Sunitha Fernando");
        request.setParentId("parent-abc");
        request.setParentContact1("0711112222");
        request.setParentContact2("0723334444");
        request.setParentNic("876543210V");
        request.setChildName("Dineth Fernando");
        request.setChildAge("8");
        request.setChildGrade("Grade 3");
        request.setChildSchool("Royal College");
        request.setChildProfileImageUrl("http://img.url/child.jpg");
        request.setStatus("pending");
        request.setDriverId("driver-xyz");
        request.setDriverName("Kamal Wijeratne");
        request.setCreatedAt(1_710_000_000L);
        request.setUpdatedAt(1_710_000_500L);

        assertEquals("req-001",               request.getRequestId());
        assertEquals("123 School Lane, Colombo", request.getPickupAddress());
        assertEquals("45 Main St, Kandy",      request.getDropoffAddress());
        assertEquals("Sunitha Fernando",       request.getParentName());
        assertEquals("parent-abc",             request.getParentId());
        assertEquals("0711112222",             request.getParentContact1());
        assertEquals("0723334444",             request.getParentContact2());
        assertEquals("876543210V",             request.getParentNic());
        assertEquals("Dineth Fernando",        request.getChildName());
        assertEquals("8",                      request.getChildAge());
        assertEquals("Grade 3",               request.getChildGrade());
        assertEquals("Royal College",          request.getChildSchool());
        assertEquals("http://img.url/child.jpg", request.getChildProfileImageUrl());
        assertEquals("pending",               request.getStatus());
        assertEquals("driver-xyz",            request.getDriverId());
        assertEquals("Kamal Wijeratne",       request.getDriverName());
        assertEquals(1_710_000_000L,          request.getCreatedAt());
        assertEquals(1_710_000_500L,          request.getUpdatedAt());
    }

    // -----------------------------------------------------------------------
    // TC-R03  getRouteText() – both addresses present (comma in address)
    // -----------------------------------------------------------------------
    @Test
    public void TC_R03_getRouteText_bothAddressesPresent_returnsFirstParts() {
        request.setPickupAddress("123 School Lane, Colombo, Sri Lanka");
        request.setDropoffAddress("45 Main St, Kandy, Sri Lanka");

        String routeText = request.getRouteText();

        // formatAddress trims to the first segment before the comma
        assertTrue("Should start with pickup first part", routeText.startsWith("123 School Lane"));
        assertTrue("Should contain ' to '", routeText.contains(" to "));
        assertTrue("Should end with dropoff first part", routeText.contains("45 Main St"));
    }

    // -----------------------------------------------------------------------
    // TC-R04  getRouteText() – null pickup
    // -----------------------------------------------------------------------
    @Test
    public void TC_R04_getRouteText_nullPickup_showsUnknown() {
        request.setPickupAddress(null);
        request.setDropoffAddress("45 Main St, Kandy");

        String routeText = request.getRouteText();
        assertTrue("Should say Unknown for null pickup", routeText.startsWith("Unknown"));
    }

    // -----------------------------------------------------------------------
    // TC-R05  getRouteText() – null dropoff
    // -----------------------------------------------------------------------
    @Test
    public void TC_R05_getRouteText_nullDropoff_showsUnknown() {
        request.setPickupAddress("123 School Lane, Colombo");
        request.setDropoffAddress(null);

        String routeText = request.getRouteText();
        assertTrue("Should end with Unknown for null dropoff",
                routeText.endsWith("Unknown"));
    }
}
