package com.eatthepath.jeospatial;

import java.util.ArrayList;
import java.util.List;

import com.eatthepath.jeospatial.util.SimpleGeospatialPoint;
import com.eatthepath.jvptree.DistanceFunction;
import com.eatthepath.jvptree.VPTree;

public class VPGeospatialPointIndex<E extends GeospatialPoint> extends VPTree<GeospatialPoint> implements GeospatialIndex<GeospatialPoint> {

    public VPGeospatialPointIndex() {
        super(new HaversineDistanceFunction<GeospatialPoint>());
    }

    // TODO Resolve generics issues
    public List<E> getAllPointsInBoundingBox(double west, double east, double north, double south) {
        final SimpleGeospatialPoint centroid;
        {
            // Via http://www.movable-type.co.uk/scripts/latlong.html
            final double Bx = Math.cos(north) * Math.cos(east-west);
            final double By = Math.cos(north) * Math.sin(east-west);

            centroid = new SimpleGeospatialPoint(
                    Math.atan2(Math.sin(south) + Math.sin(north), Math.sqrt((Math.cos(south) + Bx) * (Math.cos(south) + Bx) + (By * By))),
                    west + Math.atan2(By, Math.cos(south) + Bx));
        }

        final double searchRadius =
                this.getDistanceFunction().getDistance(centroid, new SimpleGeospatialPoint(north, east));

        final ArrayList<GeospatialPoint> points = new ArrayList<GeospatialPoint>();

        // TODO Invert the logic here to make it more affirmative
        for (final GeospatialPoint point : this.getAllWithinRange(centroid, searchRadius)) {
            if (point.getLatitude() > north || point.getLatitude() < south) {
                continue;
            }

            // If the point is inside the bounding box, it will be shorter to get to the point by traveling east from
            // the western boundary than by traveling east from the eastern boundary.
            if(this.getDegreesEastFromMeridian(west, point) > this.getDegreesEastFromMeridian(east, point)) {
                continue;
            }

            // Similarly, it should be shorter to get to the point by traveling west from the eastern boundary than by
            // traveling west from the western boundary.
            if(this.getDegreesWestFromMeridian(east, point) > this.getDegreesWestFromMeridian(west, point)) {
                continue;
            }

            points.add(point);
        }

        return points;
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
    private double getDegreesEastFromMeridian(double longitude, E point) {
        return point.getLongitude() > longitude ?
                point.getLongitude() - longitude : Math.abs(360 - (point.getLongitude() - longitude));
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
    private double getDegreesWestFromMeridian(double longitude, E point) {
        return point.getLongitude() < longitude ?
                longitude - point.getLongitude() : Math.abs(360 - (longitude - point.getLongitude()));
    }
}
