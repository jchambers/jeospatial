package com.eatthepath.jeospatial.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.eatthepath.jeospatial.SearchCriteria;

/**
 * Test suite for the {@link BoundingBoxSearchCriteria} class.
 * 
 * @author <a href="jon.chambers@gmail.com">Jon Chambers</a>
 */
public class BoundingBoxSearchCriteriaTest {
    @Test
    public void testMatches() {
        BoundingBoxSearchCriteria<SimpleGeospatialPoint> criteria =
                new BoundingBoxSearchCriteria<SimpleGeospatialPoint>(-10, 10, 10, -10);
        
        assertTrue(criteria.matches(new SimpleGeospatialPoint(0, 0)));
        assertTrue(criteria.matches(new SimpleGeospatialPoint(-10, 10)));
        assertFalse(criteria.matches(new SimpleGeospatialPoint(20, 0)));
        assertFalse(criteria.matches(new SimpleGeospatialPoint(0, 20)));
        
        criteria = new BoundingBoxSearchCriteria<SimpleGeospatialPoint>(-100, 100, 10, -10);
        assertTrue(criteria.matches(new SimpleGeospatialPoint(0, 0)));
        assertTrue(criteria.matches(new SimpleGeospatialPoint(0, 100)));
        
        criteria = new BoundingBoxSearchCriteria<SimpleGeospatialPoint>(100, -100, 10, -10);
        assertFalse(criteria.matches(new SimpleGeospatialPoint(0, 0)));
        
        SearchCriteria<SimpleGeospatialPoint> rejectEverythingCriteria = new SearchCriteria<SimpleGeospatialPoint>() {
            @Override
            public boolean matches(SimpleGeospatialPoint point) {
                return false;
            }
        };
        
        criteria = new BoundingBoxSearchCriteria<SimpleGeospatialPoint>(-10, 10, 10, -10, rejectEverythingCriteria);
        
        assertFalse(criteria.matches(new SimpleGeospatialPoint(0, 0)));
        assertFalse(criteria.matches(new SimpleGeospatialPoint(-10, 10)));
        assertFalse(criteria.matches(new SimpleGeospatialPoint(20, 0)));
        assertFalse(criteria.matches(new SimpleGeospatialPoint(0, 20)));
    }
}
