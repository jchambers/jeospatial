package com.eatthepath.jeospatial;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test suite for the SimpleGeospatialPoint class.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class SimpleGeospatialPointTest {
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
		
		assertNotSame(a, b);
		assertEquals(a, b);
		
		assertEquals(10, b.getLatitude(), 0);
		assertEquals(20, b.getLongitude(), 0);
	}

	@Test
	public void testSetLatitude() {
		SimpleGeospatialPoint p = new SimpleGeospatialPoint(10, 20);
		p.setLatitude(30);
		
		assertEquals(30, p.getLatitude(), 0);
	}

	@Test
	public void testSetLongitude() {
		SimpleGeospatialPoint p = new SimpleGeospatialPoint(10, 20);
		p.setLongitude(30);
		
		assertEquals(30, p.getLongitude(), 0);
	}

	@Test
	public void testGetDistanceTo() {
		SimpleGeospatialPoint BOS = new SimpleGeospatialPoint(42.3631, -71.0064);
		SimpleGeospatialPoint LAX = new SimpleGeospatialPoint(33.9425, -118.4072);
		
		assertEquals("Distance from point to self must be zero.",
				0, BOS.getDistanceTo(BOS), 0);
		
		assertEquals("Distance from A to B must be equal to distance from B to A.",
				BOS.getDistanceTo(LAX), LAX.getDistanceTo(BOS), 0);
		
		assertEquals("Distance between BOS and LAX should be within 50km of 4,200km.",
				4200000, BOS.getDistanceTo(LAX), 50000);
		
		SimpleGeospatialPoint a = new SimpleGeospatialPoint(0, 0);
		SimpleGeospatialPoint b = new SimpleGeospatialPoint(0, 180);
		
		assertEquals("Distance between diametrically opposed points should be within 1m of 20015086m.",
				20015086, a.getDistanceTo(b), 1d);
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
