package com.eatthepath.jeospatial;

/**
 * A simple, immutable implementation of the {@link GeospatialPoint} interface.
 */
public class SimpleGeospatialPoint implements GeospatialPoint {

    private final double latitude;
    private final double longitude;

    /**
     * Constructs a new, immutable geospatial point with the given latitude and longitude.
     *
     * @param latitude the latitude (in degrees) of this point
     * @param longitude the longitude (in degrees) of this point
     */
    public SimpleGeospatialPoint(final double latitude, final double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public double getLatitude() {
        return 0;
    }

    @Override
    public double getLongitude() {
        return 0;
    }
}
