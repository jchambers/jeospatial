package com.eatthepath.jeospatial.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test suite for the GeospatialDistanceComparator class.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class GeospatialDistanceComparatorTest {
	@Test
	public void testCompare() {
		SimpleGeospatialPoint BOS = new SimpleGeospatialPoint(42.3631, -71.0064);
		SimpleGeospatialPoint LAX = new SimpleGeospatialPoint(33.9425, -118.4072);
		SimpleGeospatialPoint ORD = new SimpleGeospatialPoint(41.9808, -87.9067);
		SimpleGeospatialPoint PVD = new SimpleGeospatialPoint(41.7239, -71.4283);
		
		SimpleGeospatialPoint[] points = new SimpleGeospatialPoint[] {ORD, LAX, PVD};
		
		GeospatialDistanceComparator<SimpleGeospatialPoint> comparator =
				new GeospatialDistanceComparator<SimpleGeospatialPoint>(BOS);
		
		java.util.Arrays.sort(points, comparator);
		
		SimpleGeospatialPoint[] expected = new SimpleGeospatialPoint[] {PVD, ORD, LAX};
		
		assertArrayEquals(expected, points);
	}
}
