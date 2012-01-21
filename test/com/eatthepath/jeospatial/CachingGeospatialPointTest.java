package com.eatthepath.jeospatial;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

/**
 * Test suite for the CachingGeospatialPoint class.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class CachingGeospatialPointTest {
    @Test
    public void testCachingGeospatialPointDoubleDouble() {
        CachingGeospatialPoint p = new CachingGeospatialPoint(10, 20);
        
        assertEquals(10, p.getLatitude(), 0);
        assertEquals(20, p.getLongitude(), 0);
    }

    @Test
    public void testCachingGeospatialPointGeospatialPoint() {
        CachingGeospatialPoint a = new CachingGeospatialPoint(10, 20);
        CachingGeospatialPoint b = new CachingGeospatialPoint(a);
        
        assertNotSame(a, b);
        assertEquals(a, b);
        
        assertEquals(10, b.getLatitude(), 0);
        assertEquals(20, b.getLongitude(), 0);
    }

    @Test
    public void testSetLatitude() {
        CachingGeospatialPoint p = new CachingGeospatialPoint(10, 20);
        p.setLatitude(30);
        
        assertEquals(30, p.getLatitude(), 0);
    }

    @Test
    public void testSetLongitude() {
        CachingGeospatialPoint p = new CachingGeospatialPoint(10, 20);
        p.setLongitude(30);
        
        assertEquals(30, p.getLongitude(), 0);
    }

    @Test
    public void testGetDistanceTo() {
        CachingGeospatialPoint BOS = new CachingGeospatialPoint(42.3631, -71.0064);
        CachingGeospatialPoint LAX = new CachingGeospatialPoint(33.9425, -118.4072);
        
        assertEquals("Distance from point to self must be zero.",
                0, BOS.getDistanceTo(BOS), 0);
        
        assertEquals("Distance from A to B must be equal to distance from B to A.",
                BOS.getDistanceTo(LAX), LAX.getDistanceTo(BOS), 0);
        
        assertEquals("Distance between BOS and LAX should be within 50km of 4,200km.",
                4200000, BOS.getDistanceTo(LAX), 50000);
        
        CachingGeospatialPoint a = new CachingGeospatialPoint(0, 0);
        CachingGeospatialPoint b = new CachingGeospatialPoint(0, 180);
        
        assertEquals("Distance between diametrically opposed points should be within 1m of 20015086m.",
                20015086, a.getDistanceTo(b), 1d);
    }
    
    /**
     * Compares performance of distance calculations between caching points and
     * simple points. The whole purpose of caching points is to trade memory for
     * speed, so if the caching points aren't faster, they're not living up to
     * their behavioral contract.
     */
    @Test
    public void testGetDistancePerformance() {
        // Pre-generate test points to cut generation time out of the comparison
        // and also to make sure that the comparison is "fair" in that both
        // distance calculation strategies are faced with the same point set
        // (not that the distribution of points should affect calculation time,
        // but why leave anything in doubt?).
        Random r = new Random();
        SimpleGeospatialPoint[] testPoints = new SimpleGeospatialPoint[100000];
        
        for(int i = 0; i < testPoints.length; i++) {
            double latitude = (r.nextDouble() * 360d) - 180d;
            double longitude = (r.nextDouble() * 360d) - 180d;
            
            testPoints[i] = new SimpleGeospatialPoint(latitude, longitude);
        }
        
        SimpleGeospatialPoint simpleOrigin = new SimpleGeospatialPoint(0, 0);
        CachingGeospatialPoint cachingOrigin = new CachingGeospatialPoint(0, 0);
        
        // Calculate the distance to all test points using the simple approach
        long start = System.currentTimeMillis();
        
        for(SimpleGeospatialPoint p : testPoints) {
            simpleOrigin.getDistanceTo(p);
        }
        
        long end = System.currentTimeMillis();
        
        long simpleTime = end - start;
        
        // ...and do it again with the caching approach
        start = System.currentTimeMillis();
        
        for(SimpleGeospatialPoint p : testPoints) {
            cachingOrigin.getDistanceTo(p);
        }
        
        end = System.currentTimeMillis();
        
        long cachingTime = end - start;
        
        assertTrue("Caching calculations should be faster than non-caching calculations.",
                cachingTime < simpleTime);
    }
    
    @Test
    public void testHashCode() {
        SimpleGeospatialPoint simplePoint = new SimpleGeospatialPoint(10, 20);
        CachingGeospatialPoint cachingPoint = new CachingGeospatialPoint(10, 20);
        
        assertEquals(simplePoint.hashCode(), cachingPoint.hashCode());
    }
    
    @Test
    public void testEquals() {
        SimpleGeospatialPoint simplePoint = new SimpleGeospatialPoint(10, 20);
        CachingGeospatialPoint cachingPoint = new CachingGeospatialPoint(10, 20);
        
        assertTrue(simplePoint.equals(cachingPoint));
    }
}
