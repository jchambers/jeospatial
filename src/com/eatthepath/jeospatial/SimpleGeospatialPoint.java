package com.eatthepath.jeospatial;

/**
 * <p>A simple geospatial point implementation. Simple geospatial points
 * calculate distance to other points using the spherical law of cosines and
 * assume that the earth is spherical and has a radius of 6,371,000 meters.</p>
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class SimpleGeospatialPoint implements GeospatialPoint {
	private double latitude;
	private double longitude;
	
	private static final double EARTH_RADIUS = 6371000; // meters
	
	/**
	 * Constructs a new geospatial point at the given latitude and longitude
	 * coordinates.
	 * 
	 * @param latitude the latitude of this point in degrees
	 * @param longitude the longitude of this point in degrees
	 */
	public SimpleGeospatialPoint(double latitude, double longitude) {
		this.setLatitude(latitude);
		this.setLongitude(longitude);
	}
	
	/**
	 * Constructs a new geospatial point at the same coordinates as the given
	 * point.
	 * 
	 * @param p the point whose location should be used for this point
	 */
	public SimpleGeospatialPoint(GeospatialPoint p) {
		this(p.getLatitude(), p.getLongitude());
	}
	
	/**
	 * Sets the latitude of this point.
	 * 
	 * @param latitude the latitude of this point in degrees
	 */
	@Override
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	/**
	 * Returns the latitude of this point.
	 * 
	 * @return the latitude of this point in degrees
	 */
	@Override
	public double getLatitude() {
		return this.latitude;
	}
	
	/**
	 * Sets the longitude of this point.
	 * 
	 * @param longitude the longitude of this point in degrees
	 */
	@Override
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	/**
	 * Returns the longitude of this point.
	 * 
	 * @return the longitude of this point in degrees
	 */
	@Override
	public double getLongitude() {
		return this.longitude;
	}
	
	/**
	 * Returns the "great circle" distance to another geospatial point.
	 * 
	 * @param otherPoint the other point to which to calculate distance 
	 * 
	 * @return the great circle distance, in meters, between the two points
	 */
	public double getDistanceTo(GeospatialPoint otherPoint) {
		double lat1 = Math.toRadians(this.getLatitude());
		double lon1 = Math.toRadians(this.getLongitude());
		double lat2 = Math.toRadians(otherPoint.getLatitude());
		double lon2 = Math.toRadians(otherPoint.getLongitude());
		
		double angle = Math.acos((Math.sin(lat1) * Math.sin(lat2)) + (Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)));
		
		return Double.isNaN(angle) ? 0 : EARTH_RADIUS * angle;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SimpleGeospatialPoint [latitude=" + latitude + ", longitude="
				+ longitude + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleGeospatialPoint other = (SimpleGeospatialPoint) obj;
		if (Double.doubleToLongBits(latitude) != Double
				.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double
				.doubleToLongBits(other.longitude))
			return false;
		return true;
	}
}
