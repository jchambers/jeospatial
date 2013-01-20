package com.eatthepath.jeospatial;

/**
 * A geospatial point is a single point on the earth's surface.
 * 
 * @author <a href="jon.chambers@gmail.com">Jon Chambers</a>
 */
public interface GeospatialPoint {
    /**
     * <p>The radius of the earth in meters; all implementing whose distance
     * calculations rely upon the radius of the earth should reference this
     * value.</p>
     * 
     * <p>The earth, for the purposes of this library, is considered to be a
     * sphere with a radius of {@value #EARTH_RADIUS} meters.</p>
     * 
     * @see GeospatialPoint#getDistanceTo(GeospatialPoint)
     */
    public static final double EARTH_RADIUS = 6371 * 1000; // meters
    
    /**
     * Returns the latitude of this point.
     * 
     * @return the latitude of this point in degrees
     */
	public double getLatitude();
	
    /**
     * Returns the longitude of this point. The returned longitude should be
     * normalized to the range -180 degrees (inclusive) to 180 degrees
     * (exclusive).
     * 
     * @return the longitude of this point in degrees
     */
	public double getLongitude();
	
    /**
     * <p>Calculates the "great circle" or orthometric distance between this
     * point and another point on the earth's surface. The great circle distance
     * is the minimum distance traveled to get from one point to the next on the
     * surface of the earth.</p>
     * 
     * <p>Implementations of this method that incorporate the radius of the
     * earth should reference the {@link #EARTH_RADIUS} property of this
     * interface.</p>
     * 
     * @param otherPoint
     *            the other point to which to calculate distance
     * 
     * @return the great circle distance between the two points in meters
     * 
     * @see GeospatialPoint#EARTH_RADIUS
     */
	public double getDistanceTo(GeospatialPoint otherPoint);
	
    /**
     * <p>Calculates the "great circle" or orthometric distance between this
     * point and another point on the earth's surface. The great circle distance
     * is the minimum distance traveled to get from one point to the next on the
     * surface of the earth.</p>
     * 
     * <p>Implementations of this method that incorporate the radius of the
     * earth should reference the {@link #EARTH_RADIUS} property of this
     * interface.</p>
     * 
     * @param latitude
     *            the latitude, in degrees, of the other point to which to
     *            calculate distance
     * @param longitude
     *            the longitude, in degrees, of the other point to which to
     *            calculate distance
     * 
     * @return the great circle distance between the two points in meters
     * 
     * @see GeospatialPoint#EARTH_RADIUS
     */
	public double getDistanceTo(double latitude, double longitude);
}
