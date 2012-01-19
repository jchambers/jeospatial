package com.eatthepath.jeospatial.util;

import java.util.Comparator;

import com.eatthepath.jeospatial.GeospatialPoint;

/**
 * <p>A comparator that sorts geospatial points in order of increasing distance
 * from a given origin point.</p>
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class GeospatialDistanceComparator<T extends GeospatialPoint> implements Comparator<T> {
	private final GeospatialPoint origin;
	
	/**
	 * Constructs a new comparator that sorts geospatial points according to
	 * their distance from the given origin point.
	 * 
	 * @param origin
	 *            the point from which to measure other points
	 */
	public GeospatialDistanceComparator(GeospatialPoint origin) {
		this.origin = origin;
	}
	
	/**
	 * Compares two geospatial points for order based on their distance from the
	 * origin point given when this comparator was constructed.
	 * 
	 * @param p1
	 *            the first point to compare
	 * @param p2
	 *            the second point to compare
	 */
	@Override
	public int compare(T p1, T p2) {
		double d1 = this.origin.getDistanceTo(p1);
		double d2 = this.origin.getDistanceTo(p2);
		
		if(d1 < d2) { return -1; }
		if(d1 > d2) { return 1; }
		return 0;
	}
}
