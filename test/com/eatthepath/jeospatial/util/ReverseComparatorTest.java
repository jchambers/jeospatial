package com.eatthepath.jeospatial.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test suite for the ReverseComparator class.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class ReverseComparatorTest {
	@Test
	public void testCompare() {
		SimpleGeospatialPoint BOS = new SimpleGeospatialPoint(42.3631, -71.0064);
		SimpleGeospatialPoint LAX = new SimpleGeospatialPoint(33.9425, -118.4072);
		SimpleGeospatialPoint ORD = new SimpleGeospatialPoint(41.9808, -87.9067);
		SimpleGeospatialPoint PVD = new SimpleGeospatialPoint(41.7239, -71.4283);
		
		SimpleGeospatialPoint[] points = new SimpleGeospatialPoint[] {ORD, LAX, PVD};
		
		GeospatialDistanceComparator<SimpleGeospatialPoint> baseComparator =
				new GeospatialDistanceComparator<SimpleGeospatialPoint>(BOS);
		
		ReverseComparator<SimpleGeospatialPoint> reverseComparator =
				new ReverseComparator<SimpleGeospatialPoint>(baseComparator);
		
		java.util.Arrays.sort(points, reverseComparator);
		
		SimpleGeospatialPoint[] expected = new SimpleGeospatialPoint[] {LAX, ORD, PVD};
		
		assertArrayEquals(expected, points);
	}
}
