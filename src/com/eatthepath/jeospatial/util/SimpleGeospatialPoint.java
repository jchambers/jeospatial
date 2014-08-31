package com.eatthepath.jeospatial.util;

import com.eatthepath.jeospatial.GeospatialPoint;

/**
 * <p>A simple, immutable geospatial point implementation.</p>
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class SimpleGeospatialPoint implements GeospatialPoint {
    private final double latitude;
    private final double longitude;

    /**
     * Constructs a new geospatial point at the given latitude and longitude
     * coordinates.
     * 
     * @param latitude the latitude of this point in degrees
     * @param longitude the longitude of this point in degrees
     * 
     * @throws IllegalArgumentException
     *             if the given latitude is outside of the allowable range
     */
    public SimpleGeospatialPoint(final double latitude, final double longitude) {
        if(latitude < -90 || latitude > 90) {
            // TODO Normalize instead of throwing an exception
            throw new IllegalArgumentException("Latitude must be in the range -90 (inclusive) to +90 (inclusive).");
        }

        this.latitude = latitude;
        this.longitude = ((longitude + 180) % 360) - 180;
    }

    /**
     * Returns the latitude of this point.
     * 
     * @return the latitude of this point in degrees
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * Returns the longitude of this point.
     * 
     * @return the longitude of this point in degrees
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * Returns a human-readable {@code String} representation of this point.
     * 
     * @return a {@code String} representation of this point
     */
    @Override
    public String toString() {
        return "SimpleGeospatialPoint [latitude=" + latitude + ", longitude="
                + longitude + "]";
    }

    /**
     * Generates a hash code value for this point.
     * 
     * @return a hash code value for this point
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(this.getLatitude());
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.getLongitude());
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * Compares this point to another object. The other object is considered
     * equal if it is not {@code null}, is also a {@code SimpleGeospatialPoint}
     * (or a subclass thereof), and has the same latitude and longitude as this
     * point.
     * 
     * @return {@code true} if the other object is equal to this point or
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if(!(obj instanceof SimpleGeospatialPoint))
            return false;
        SimpleGeospatialPoint other = (SimpleGeospatialPoint) obj;
        if (Double.doubleToLongBits(latitude) != Double
                .doubleToLongBits(other.getLatitude()))
            return false;
        if (Double.doubleToLongBits(longitude) != Double
                .doubleToLongBits(other.getLongitude()))
            return false;
        return true;
    }
}
