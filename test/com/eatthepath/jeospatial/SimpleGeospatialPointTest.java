package com.eatthepath.jeospatial;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test suite for the SimpleGeospatialPoint class.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class SimpleGeospatialPointTest extends GeospatialPointTest {
	@Override
    public GeospatialPoint getPoint(double latitude, double longitude) {
	    return new SimpleGeospatialPoint(latitude, longitude);
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
