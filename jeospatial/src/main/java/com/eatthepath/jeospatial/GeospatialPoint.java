package com.eatthepath.jeospatial;

/**
 * A geospatial point is a single point on the earth's surface.
 * 
 * @author <a href="jon.chambers@gmail.com">Jon Chambers</a>
 */
public interface GeospatialPoint {

    /**
     * Returns the latitude of this point.
     * 
     * @return the latitude of this point in degrees
     */
    double getLatitude();

    /**
     * Returns the longitude of this point. The returned longitude should be
     * normalized to the range -180 degrees (inclusive) to 180 degrees
     * (exclusive).
     * 
     * @return the longitude of this point in degrees
     */
    double getLongitude();
}
