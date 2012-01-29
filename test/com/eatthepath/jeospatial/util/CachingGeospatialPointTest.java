package com.eatthepath.jeospatial.util;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import com.eatthepath.jeospatial.GeospatialPoint;
import com.eatthepath.jeospatial.GeospatialPointTest;

/**
 * Test suite for the CachingGeospatialPoint class.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class CachingGeospatialPointTest extends GeospatialPointTest {
    @Override
    public GeospatialPoint getPoint(double latitude, double longitude) {
        return new CachingGeospatialPoint(latitude, longitude);
    }

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
        
        assertEquals(a.getLatitude(), b.getLatitude(), 0);
        assertEquals(a.getLongitude(), b.getLongitude(), 0);
    }

    @Test
    public void testGetDistanceTo() {
        super.testGetDistanceTo();
        
        // In addition to the base tests, we want to make sure that the cached
        // bits of the distance calculation change when the point moves.
        SimpleGeospatialPoint BOS = new SimpleGeospatialPoint(42.3631, -71.0064);
        SimpleGeospatialPoint LAX = new SimpleGeospatialPoint(33.9425, -118.4072);
        SimpleGeospatialPoint SFO = new SimpleGeospatialPoint(37.6189, -122.3750);
        
        CachingGeospatialPoint p = new CachingGeospatialPoint(BOS);
        assertEquals(BOS.getDistanceTo(LAX), p.getDistanceTo(LAX), 0);
        
        p.setLatitude(SFO.getLatitude());
        p.setLongitude(SFO.getLongitude());
        
        assertEquals(SFO.getDistanceTo(LAX), p.getDistanceTo(LAX), 0);
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
    public void testEquals() {
        SimpleGeospatialPoint simplePoint = new SimpleGeospatialPoint(10, 20);
        CachingGeospatialPoint cachingPoint = new CachingGeospatialPoint(10, 20);
        
        assertTrue(simplePoint.equals(cachingPoint));
    }
}
