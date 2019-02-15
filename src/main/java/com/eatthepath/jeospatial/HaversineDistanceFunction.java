package com.eatthepath.jeospatial;

import com.eatthepath.jvptree.DistanceFunction;

/**
 * A distance function that calculates the "great circle" distance between two points on the earth's surface using the
 * Haversine formula.
 *
 * @author <a href="https://github.com/jchambers">Jon Chambers</a>
 */
class HaversineDistanceFunction implements DistanceFunction<GeospatialPoint> {

    private static final double EARTH_RADIUS = 6371e3; // meters

    /**
     * Returns the "great cricle" distance in meters between two points on the earth's surface.
     *
     * @param firstPoint the first geospatial point
     * @param secondPoint the second geospatial point
     *
     * @return the "great circle" distance in meters between the given points
     */
    public double getDistance(final GeospatialPoint firstPoint, final GeospatialPoint secondPoint) {
        final double lat1 = Math.toRadians(firstPoint.getLatitude());
        final double lon1 = Math.toRadians(firstPoint.getLongitude());
        final double lat2 = Math.toRadians(secondPoint.getLatitude());
        final double lon2 = Math.toRadians(secondPoint.getLongitude());

        final double angle = 2 * Math.asin(Math.min(1, Math.sqrt(haversine(lat2 - lat1) + Math.cos(lat1) * Math.cos(lat2) * haversine(lon2 - lon1))));

        return angle * HaversineDistanceFunction.EARTH_RADIUS;
    }

    /**
     * Returns the haversine of the given angle.
     *
     * @param theta the angle, in radians, for which to calculate the haversine
     *
     * @return the haversine of the given angle
     *
     * @see <a href="https://en.wikipedia.org/wiki/Versine">Versine - Wikipedia</a>
     */
    private static double haversine(final double theta) {
        final double x = Math.sin(theta / 2);
        return (x * x);
    }
}
