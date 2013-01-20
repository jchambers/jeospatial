package com.eatthepath.jeospatial.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.eatthepath.jeospatial.GeospatialPointTest;

/**
 * Test suite for the SimpleGeospatialPoint class.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class SimpleGeospatialPointTest extends GeospatialPointTest {
	@Override
    public SimpleGeospatialPoint getPoint(double latitude, double longitude) {
	    return new SimpleGeospatialPoint(latitude, longitude);
    }
	
    @Test
    public void testGetSetLatitude() {
        SimpleGeospatialPoint p = this.getPoint(10, 20);
        assertEquals(10, p.getLatitude(), 0);
        
        p.setLatitude(30);
        assertEquals(30, p.getLatitude(), 0);
    }
    
    @Test
    public void testSetLatitudeEdgeOfRange() {
    	SimpleGeospatialPoint p = this.getPoint(10, 20);
        
        // As long as we don't throw an exception here, everything's fine.
        p.setLatitude(90);
        p.setLatitude(-90);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetLatitudeOutOfRange() {
    	SimpleGeospatialPoint p = this.getPoint(10, 20);
        p.setLatitude(120);
    }
    
    @Test
    public void testGetSetLongitude() {
    	SimpleGeospatialPoint p = this.getPoint(10, 20);
        assertEquals(20, p.getLongitude(), 0);
        
        p.setLongitude(30);
        assertEquals(30, p.getLongitude(), 0);
    }
    
    @Test
    public void testSetLongitudeNormalization() {
    	SimpleGeospatialPoint p = this.getPoint(0, 0);
        
        p.setLongitude(-180);
        assertEquals(-180, p.getLongitude(), 0);
        
        p.setLongitude(180);
        assertEquals(-180, p.getLongitude(), 0);
        
        p.setLongitude(240);
        assertEquals(-120, p.getLongitude(), 0);
        
        p.setLongitude(360);
        assertEquals(0, p.getLongitude(), 0);
    }

    @Test
	public void testSimpleGeospatialPointDoubleDouble() {
		SimpleGeospatialPoint p = new SimpleGeospatialPoint(10, 20);
		
		assertEquals(10, p.getLatitude(), 0);
		assertEquals(20, p.getLongitude(), 0);
	}

	@Test
	public void testSimpleGeospatialPointGeospatialPoint() {
		SimpleGeospatialPoint a = new SimpleGeospatialPoint(10, 20);
		SimpleGeospatialPoint b = new SimpleGeospatialPoint(a);
		
		// Make sure we're not .equal by accident
		assertNotSame(a, b);
		assertEquals(a, b);
		
		assertEquals(10, b.getLatitude(), 0);
		assertEquals(20, b.getLongitude(), 0);
	}

	@Test
	public void testEquals() {
	    SimpleGeospatialPoint a = new SimpleGeospatialPoint(10, 20);
	    SimpleGeospatialPoint b = new SimpleGeospatialPoint(10, 20);
	    SimpleGeospatialPoint c = new SimpleGeospatialPoint(20, 10);
	    
	    assertTrue(a.equals(b));
	    assertFalse(a.equals(c));
	}
}
