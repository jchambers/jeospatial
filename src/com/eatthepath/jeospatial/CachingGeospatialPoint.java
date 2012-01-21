package com.eatthepath.jeospatial;

/**
 * <p>A caching geospatial point is an extension of a simple geospatial point
 * that trades memory efficiency for processing time in calculating distance to
 * other points. From an external perspective, caching geospatial points behave
 * identically to simple geospatial points. Internally, caching geospatial
 * points pre-calculate parts of the spherical distance calculation. This uses
 * more memory, but reduces the number of trigonometric calculations that need
 * to be made in repeated calls to the @{code getDistanceTo} method.</p>
 * 
 * <p>Caching geospatial points are a good choice in cases where one point will
 * be the origin in distance calculations to lots of other points.</p>
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class CachingGeospatialPoint extends SimpleGeospatialPoint {
	private static final double EARTH_RADIUS = 6371000; // meters
	
	private double lon1;
	private double sinLat1;
	private double cosLat1;
	
	/**
	 * Constructs a new caching geospatial point at the given coordinates.
	 * 
	 * @param latitude the latitude of the new point in degrees
	 * @param longitude the longitude of the new point in degrees
	 */
	public CachingGeospatialPoint(double latitude, double longitude) {
		super(latitude, longitude);
		
		this.setLatitude(latitude);
		this.setLongitude(longitude);
	}
	
	    /**
     * Constructs a new caching geospatial point at the location of the given
     * point.
     * 
     * @param p
     *            the point at which the new point should be created
     */
	public CachingGeospatialPoint(GeospatialPoint p) {
		this(p.getLatitude(), p.getLongitude());
	}
	
	/**
	 * Sets the latitude of this point.
	 * 
	 * @param latitude the latitude of this point in degrees
	 */
	@Override
	public void setLatitude(double latitude) {
		super.setLatitude(latitude);
		
		double lat1 = Math.toRadians(latitude);
		this.sinLat1 = Math.sin(lat1);
		this.cosLat1 = Math.cos(lat1);
	}

	/**
	 * Sets the longitude of this point.
	 * 
	 * @param longitude the longitude of this point in degrees
	 */
	@Override
	public void setLongitude(double longitude) {
		super.setLongitude(longitude);
		
		this.lon1 = Math.toRadians(longitude);
	}
	
	/**
	 * Returns the "great circle" distance to another geospatial point.
	 * 
	 * @param otherPoint the other point to which to calculate distance 
	 * 
	 * @return the great circle distance, in meters, between the two points
	 */
	@Override
	public double getDistanceTo(GeospatialPoint otherPoint) {
		double lat2 = Math.toRadians(otherPoint.getLatitude());
		double lon2 = Math.toRadians(otherPoint.getLongitude());
		
		double angle = Math.acos((this.sinLat1 * Math.sin(lat2)) +
				(this.cosLat1 * Math.cos(lat2) * Math.cos(lon2 - this.lon1)));
		
		return Double.isNaN(angle) ? 0 : EARTH_RADIUS * angle;
	}

}
