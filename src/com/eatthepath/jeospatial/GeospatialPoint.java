package com.eatthepath.jeospatial;

/**
 * A geospatial point is a single point on the earth's surface.
 * 
 * @author <a href="jon.chambers@gmail.com">Jon Chambers</a>
 */
public interface GeospatialPoint {
	/**
	 * Sets the latitude of this point.
	 * 
	 * @param latitude the latitude of this point in degrees
	 */
	public void setLatitude(double latitude);
	
	/**
	 * Sets the longitude of this point.
	 * 
	 * @param longitude the longitude of this point in degrees
	 */
	public void setLongitude(double longitude);
	
	/**
	 * Returns the latitude of this point.
	 * 
	 * @return the latitude of this point in degrees
	 */
	public double getLatitude();
	
	/**
	 * Returns the longitude of this point.
	 * 
	 * @return the longitude of this point in degrees
	 */
	public double getLongitude();
	
	/**
	 * Calculates the "great circle" or orthometric distance between this point
	 * and another point on the earth's surface. The great circle distance is
	 * the minimum distance traveled to get from one point to the next on the
	 * surface of the earth.
	 * 
	 * @param otherPoint the other point to which to calculate distance
	 * 
	 * @return the great circle distance between the two points in meters
	 */
	public double getDistanceTo(GeospatialPoint otherPoint);
}
