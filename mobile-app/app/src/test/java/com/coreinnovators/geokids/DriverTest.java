package com.coreinnovators.geokids;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link Driver} model class.
 *
 * Test Cases Covered:
 *  TC-D01 – Default constructor produces all-null fields
 *  TC-D02 – Parameterised constructor sets fields correctly
 *  TC-D03 – Setters / Getters round-trip for every property
 *  TC-D04 – setRideActive / isRideActive toggles correctly
 *  TC-D05 – toMap() contains all expected keys
 *  TC-D06 – toString() includes id, fullName, vehicleNumber, status
 *  TC-D07 – setDriverId() / getDriverId() alias works correctly
 *  TC-D08 – LocationPoint constructor and getters work correctly
 *  TC-D09 – LocationPoint.toMap() contains lat & lng keys
 *  TC-D10 – setVehicleImageUrls stores and returns the same list
 */
public class DriverTest {

    private Driver driver;

    @Before
    public void setUp() {
        driver = new Driver();
    }

    // -----------------------------------------------------------------------
    // TC-D01  Default constructor
    // -----------------------------------------------------------------------
    @Test
    public void TC_D01_defaultConstructor_allNullFields() {
        assertNull("id should be null", driver.getId());
        assertNull("fullName should be null", driver.getFullName());
        assertNull("address should be null", driver.getAddress());
        assertNull("nic should be null", driver.getNic());
        assertNull("birthday should be null", driver.getBirthday());
        assertNull("contactNumber should be null", driver.getContactNumber());
        assertNull("status should be null", driver.getStatus());
        assertFalse("rideActive default should be false", driver.isRideActive());
    }

    // -----------------------------------------------------------------------
    // TC-D02  Parameterised constructor
    // -----------------------------------------------------------------------
    @Test
    public void TC_D02_parametrisedConstructor_setsFieldsCorrectly() {
        Driver d = new Driver("Kasun Perera", "Colombo", "987654321V",
                "1990-05-10", "0711234567", "http://img.url/profile.jpg");

        assertEquals("Kasun Perera", d.getFullName());
        assertEquals("Colombo", d.getAddress());
        assertEquals("987654321V", d.getNic());
        assertEquals("1990-05-10", d.getBirthday());
        assertEquals("0711234567", d.getContactNumber());
        assertEquals("http://img.url/profile.jpg", d.getProfileImageUrl());
        assertTrue("createdAt should be > 0 after construction", d.getCreatedAt() > 0);
    }

    // -----------------------------------------------------------------------
    // TC-D03  Full setter / getter round-trip
    // -----------------------------------------------------------------------
    @Test
    public void TC_D03_settersGetters_roundTrip() {
        driver.setId("driver-001");
        driver.setFullName("Nimal Silva");
        driver.setAddress("Kandy");
        driver.setNic("123456789V");
        driver.setBirthday("1985-03-21");
        driver.setContactNumber("0777654321");
        driver.setVehicleNumber("WP-1234");
        driver.setStatus("approved");
        driver.setFrontLicenseUrl("http://img.url/front.jpg");
        driver.setBackLicenseUrl("http://img.url/back.jpg");
        driver.setCreatedAt(1_700_000_000L);
        driver.setLastUpdated(1_700_000_100L);
        driver.setSubmittedAt(1_700_000_050L);

        assertEquals("driver-001", driver.getId());
        assertEquals("Nimal Silva", driver.getFullName());
        assertEquals("Kandy", driver.getAddress());
        assertEquals("123456789V", driver.getNic());
        assertEquals("1985-03-21", driver.getBirthday());
        assertEquals("0777654321", driver.getContactNumber());
        assertEquals("WP-1234", driver.getVehicleNumber());
        assertEquals("approved", driver.getStatus());
        assertEquals("http://img.url/front.jpg", driver.getFrontLicenseUrl());
        assertEquals("http://img.url/back.jpg", driver.getBackLicenseUrl());
        assertEquals(1_700_000_000L, driver.getCreatedAt());
        assertEquals(1_700_000_100L, driver.getLastUpdated());
        assertEquals(1_700_000_050L, driver.getSubmittedAt());
    }

    // -----------------------------------------------------------------------
    // TC-D04  rideActive flag toggle
    // -----------------------------------------------------------------------
    @Test
    public void TC_D04_rideActiveFlag_toggling() {
        assertFalse(driver.isRideActive());
        driver.setRideActive(true);
        assertTrue(driver.isRideActive());
        driver.setRideActive(false);
        assertFalse(driver.isRideActive());
    }

    // -----------------------------------------------------------------------
    // TC-D05  toMap() completeness
    // -----------------------------------------------------------------------
    @Test
    public void TC_D05_toMap_containsAllKeys() {
        driver.setFullName("Ruwan Dias");
        driver.setAddress("Galle");
        driver.setNic("999888777V");
        driver.setBirthday("1992-11-30");
        driver.setContactNumber("0714567890");
        driver.setStatus("pending");
        driver.setVehicleNumber("SB-5678");

        Map<String, Object> map = driver.toMap();

        assertTrue(map.containsKey("fullName"));
        assertTrue(map.containsKey("address"));
        assertTrue(map.containsKey("nic"));
        assertTrue(map.containsKey("birthday"));
        assertTrue(map.containsKey("contactNumber"));
        assertTrue(map.containsKey("status"));
        assertTrue(map.containsKey("vehicleNumber"));
        assertTrue(map.containsKey("rideActive"));
        assertTrue(map.containsKey("createdAt"));
        assertTrue(map.containsKey("lastUpdated"));
        assertTrue(map.containsKey("submittedAt"));

        assertEquals("Ruwan Dias", map.get("fullName"));
        assertEquals("pending", map.get("status"));
        assertEquals("SB-5678", map.get("vehicleNumber"));
    }

    // -----------------------------------------------------------------------
    // TC-D06  toString() format
    // -----------------------------------------------------------------------
    @Test
    public void TC_D06_toString_containsKeyInfo() {
        driver.setId("driver-123");
        driver.setFullName("Amara Jayasinghe");
        driver.setVehicleNumber("NB-9999");
        driver.setStatus("active");

        String str = driver.toString();

        assertTrue(str.contains("driver-123"));
        assertTrue(str.contains("Amara Jayasinghe"));
        assertTrue(str.contains("NB-9999"));
        assertTrue(str.contains("active"));
    }

    // -----------------------------------------------------------------------
    // TC-D07  getDriverId / setDriverId alias
    // -----------------------------------------------------------------------
    @Test
    public void TC_D07_driverIdAlias_worksCorrectly() {
        driver.setDriverId("alias-driver-007");
        assertEquals("alias-driver-007", driver.getDriverId());
        assertEquals("alias-driver-007", driver.getId()); // same backing field
    }

    // -----------------------------------------------------------------------
    // TC-D08  LocationPoint constructor & getters
    // -----------------------------------------------------------------------
    @Test
    public void TC_D08_locationPoint_constructorAndGetters() {
        Driver.LocationPoint lp = new Driver.LocationPoint(6.9271, 79.8612);
        assertEquals(6.9271, lp.getLat(), 0.0001);
        assertEquals(79.8612, lp.getLng(), 0.0001);
    }

    // -----------------------------------------------------------------------
    // TC-D09  LocationPoint.toMap()
    // -----------------------------------------------------------------------
    @Test
    public void TC_D09_locationPoint_toMap_containsLatLng() {
        Driver.LocationPoint lp = new Driver.LocationPoint(7.8731, 80.7718);
        Map<String, Object> map = lp.toMap();

        assertTrue(map.containsKey("lat"));
        assertTrue(map.containsKey("lng"));
        assertEquals(7.8731, (Double) map.get("lat"), 0.0001);
        assertEquals(80.7718, (Double) map.get("lng"), 0.0001);
    }

    // -----------------------------------------------------------------------
    // TC-D10  vehicleImageUrls list stored & retrieved correctly
    // -----------------------------------------------------------------------
    @Test
    public void TC_D10_vehicleImageUrls_storedAndRetrieved() {
        List<String> images = Arrays.asList(
                "http://img.url/van1.jpg",
                "http://img.url/van2.jpg"
        );
        driver.setVehicleImageUrls(images);

        List<String> retrieved = driver.getVehicleImageUrls();
        assertNotNull(retrieved);
        assertEquals(2, retrieved.size());
        assertEquals("http://img.url/van1.jpg", retrieved.get(0));
        assertEquals("http://img.url/van2.jpg", retrieved.get(1));
    }
}
