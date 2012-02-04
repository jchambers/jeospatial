package com.eatthepath.jeospatial.util;

import static org.junit.Assert.*;

import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eatthepath.jeospatial.SearchCriteria;

public class SearchResultsTest {
    private static Hashtable<String, SimpleGeospatialPoint> cities;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        SearchResultsTest.cities = new Hashtable<String, SimpleGeospatialPoint>();
        
        cities.put("Boston", new SimpleGeospatialPoint(42.338947, -70.919635));
        cities.put("New York", new SimpleGeospatialPoint(40.780751, -73.977182));
        cities.put("San Francisco", new SimpleGeospatialPoint(37.766529, -122.39577));
        cities.put("Los Angeles", new SimpleGeospatialPoint(34.048411, -118.34015));
        cities.put("Dallas", new SimpleGeospatialPoint(32.787629, -96.79941));
        cities.put("Chicago", new SimpleGeospatialPoint(41.904667, -87.62504));
        cities.put("Memphis", new SimpleGeospatialPoint(35.169255, -89.990415));
        cities.put("Las Vegas", new SimpleGeospatialPoint(36.145303, -115.18358));
        cities.put("Detroit", new SimpleGeospatialPoint(42.348937, -83.08994));
    }
    
    @Before
    public void setUp() throws Exception {
    }
    
    @Test
    public void testAdd() {
        CachingGeospatialPoint somerville = new CachingGeospatialPoint(42.387597, -71.099497);
        
        SearchResults<SimpleGeospatialPoint> results = new SearchResults<SimpleGeospatialPoint>(somerville, 3);
        
        // All three initial adds should work since we haven't hit queue
        // capacity and we don't have a maximum distance or search criteria
        assertTrue(results.add(SearchResultsTest.cities.get("Chicago")));
        assertTrue(results.add(SearchResultsTest.cities.get("Detroit")));
        assertTrue(results.add(SearchResultsTest.cities.get("Memphis")));
        
        // This should NOT get added because the queue should be full and LA is
        // farther from Somerville than the current worst option.
        assertFalse(results.add(SearchResultsTest.cities.get("Los Angeles")));
        
        // Boston, on the other hand, SHOULD get added because it's closer than
        // our current worst option.
        assertTrue(results.add(SearchResultsTest.cities.get("Boston")));
        
        // Now make sure we have the set of cities we expect.
        assertEquals(3, results.size());
        assertTrue(results.contains(SearchResultsTest.cities.get("Boston")));
        assertTrue(results.contains(SearchResultsTest.cities.get("Detroit")));
        assertTrue(results.contains(SearchResultsTest.cities.get("Chicago")));
        
        results = new SearchResults<SimpleGeospatialPoint>(somerville, 8, 1000 * 1000);
        
        assertTrue(results.add(SearchResultsTest.cities.get("Boston")));
        
        // Should be rejected even though we don't have a full queue because
        // it's out of range
        assertFalse(results.add(SearchResultsTest.cities.get("Las Vegas")));
        
        SearchCriteria<SimpleGeospatialPoint> criteria = new SearchCriteria<SimpleGeospatialPoint>() {
            @Override
            public boolean matches(SimpleGeospatialPoint point) {
                return SearchResultsTest.cities.get("Boston").equals(point);
            }
        };
        
        results = new SearchResults<SimpleGeospatialPoint>(somerville, 8, criteria);
        
        assertTrue(results.add(SearchResultsTest.cities.get("Boston")));
        
        // Should be rejected even though we don't have a full queue or a range
        // requirement because it's not Boston
        assertFalse(results.add(SearchResultsTest.cities.get("New York")));
        
        criteria = new SearchCriteria<SimpleGeospatialPoint>() {
            @Override
            public boolean matches(SimpleGeospatialPoint point) {
                return !SearchResultsTest.cities.get("Boston").equals(point);
            }
        };
        
        results = new SearchResults<SimpleGeospatialPoint>(somerville, 8, 1000 * 1000, criteria);
        
        // Should be accepted because it's within range and not excluded by
        // search criteria
        assertTrue(results.add(SearchResultsTest.cities.get("New York")));
        
        // Should be rejected based on search criteria
        assertFalse(results.add(SearchResultsTest.cities.get("Boston")));
        
        // Should be rejected due to distance restriction
        assertFalse(results.add(SearchResultsTest.cities.get("San Francisco")));
    }
    
    @Test
    public void testAddAll() {
        CachingGeospatialPoint somerville = new CachingGeospatialPoint(42.387597, -71.099497);
        
        SearchResults<SimpleGeospatialPoint> results = new SearchResults<SimpleGeospatialPoint>(somerville, 4);
        results.addAll(SearchResultsTest.cities.values());
        
        // Make sure we store everything when we have the capacity to do so
        assertEquals(4, results.size());
        assertTrue(results.contains(SearchResultsTest.cities.get("Boston")));
        assertTrue(results.contains(SearchResultsTest.cities.get("New York")));
        assertTrue(results.contains(SearchResultsTest.cities.get("Detroit")));
        assertTrue(results.contains(SearchResultsTest.cities.get("Chicago")));
        
        results = new SearchResults<SimpleGeospatialPoint>(somerville, 2);
        results.addAll(SearchResultsTest.cities.values());
        
        // Make sure we're still enforcing distance cutoffs when we don't have
        // capacity for everything
        assertEquals(2, results.size());
        assertTrue(results.contains(SearchResultsTest.cities.get("Boston")));
        assertTrue(results.contains(SearchResultsTest.cities.get("New York")));
    }
    
    @Test
    public void testGetLongestDistanceFromQueryPoint() {
        CachingGeospatialPoint somerville = new CachingGeospatialPoint(42.387597, -71.099497);
        
        SearchResults<SimpleGeospatialPoint> results =
                new SearchResults<SimpleGeospatialPoint>(somerville, SearchResultsTest.cities.size());
        
        results.addAll(SearchResultsTest.cities.values());
        
        assertEquals(somerville.getDistanceTo(SearchResultsTest.cities.get("San Francisco")),
                results.getLongestDistanceFromQueryPoint(), 0);
    }
    
    @Test
    public void testToSortedList() {
        CachingGeospatialPoint somerville = new CachingGeospatialPoint(42.387597, -71.099497);
        
        SearchResults<SimpleGeospatialPoint> results = new SearchResults<SimpleGeospatialPoint>(somerville, 4);
        results.addAll(SearchResultsTest.cities.values());
        
        List<SimpleGeospatialPoint> sortedResults = results.toSortedList();
        
        List<SimpleGeospatialPoint> expectedResults =
                new ArrayList<SimpleGeospatialPoint>(SearchResultsTest.cities.values());
        
        java.util.Collections.sort(expectedResults, new GeospatialDistanceComparator<SimpleGeospatialPoint>(somerville));
        
        expectedResults = expectedResults.subList(0, 4);
        
        assertEquals(4, sortedResults.size());
        assertEquals(expectedResults, sortedResults);
    }
}
