package com.eatthepath.jeospatial;

import com.eatthepath.jvptree.PointFilter;

/**
 * A point filter that accepts only points that fall within a given bounding box.
 *
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
class BoundingBoxPointFilter implements PointFilter<GeospatialPoint> {

    private final double south;
    private final double west;
    private final double north;
    private final double east;

    BoundingBoxPointFilter(final double south, final double west, final double north, final double east) {
        this.south = south;
        this.west = west;
        this.north = north;
        this.east = east;
    }

    @Override
    public boolean allowPoint(final GeospatialPoint point) {
        if (point.getLatitude() <= this.north && point.getLatitude() >= this.south) {

            // If the point is inside the bounding box, it will be shorter to get to the point by traveling east
            // from the western boundary than by traveling east from the eastern boundary.
            if (this.getDegreesEastFromMeridian(this.west, point) <= this.getDegreesEastFromMeridian(this.east, point)) {

                // Similarly, it should be shorter to get to the point by traveling west from the eastern boundary
                // than by traveling west from the western boundary.
                return this.getDegreesWestFromMeridian(this.east, point) <= this.getDegreesWestFromMeridian(this.west, point);
            }
        }

        return false;
    }

    /**
     * Calculates the minimum eastward angle traveled from a meridian to a point. If the point is coincident with the
     * meridian, this method returns 360 degrees.
     *
     * @param longitude the line of longitude at which to begin travel
     * @param point the point to which to travel
     *
     * @return the eastward-traveling distance between the line and the point in degrees
     */
    private double getDegreesEastFromMeridian(final double longitude, final GeospatialPoint point) {
        return point.getLongitude() > longitude
                ? point.getLongitude() - longitude : Math.abs(360 - (point.getLongitude() - longitude));
    }

    /**
     * Calculates the minimum westward angle traveled from a meridian to a point. If the point is coincident with the
     * meridian, this method returns 360 degrees.
     *
     * @param longitude the line of longitude at which to begin travel
     * @param point the point to which to travel
     *
     * @return the westward-traveling distance between the line and the point in degrees
     */
    private double getDegreesWestFromMeridian(final double longitude, final GeospatialPoint point) {
        return point.getLongitude() < longitude
                ? longitude - point.getLongitude() : Math.abs(360 - (longitude - point.getLongitude()));
    }
}
