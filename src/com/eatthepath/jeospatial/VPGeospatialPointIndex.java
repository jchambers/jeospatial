package com.eatthepath.jeospatial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.eatthepath.jvptree.VPTree;

public class VPGeospatialPointIndex<E extends GeospatialPoint> implements GeospatialIndex<E> {

    private final VPTree<GeospatialPoint> vpTree;

    public VPGeospatialPointIndex() {
        this.vpTree = new VPTree<GeospatialPoint>(new HaversineDistanceFunction<GeospatialPoint>());
    }

    @SuppressWarnings("unchecked")
    public List<E> getAllPointsInBoundingBox(final double south, final double west, final double north, final double east) {
        final GeospatialPoint centroid;
        {
            // Via http://www.movable-type.co.uk/scripts/latlong.html
            final double Bx = Math.cos(north) * Math.cos(east-west);
            final double By = Math.cos(north) * Math.sin(east-west);

            final double latitude = Math.atan2(Math.sin(south) + Math.sin(north), Math.sqrt((Math.cos(south) + Bx) * (Math.cos(south) + Bx) + (By * By)));
            final double longitude = west + Math.atan2(By, Math.cos(south) + Bx);

            centroid = new GeospatialPoint() {
                public double getLongitude() {
                    return longitude;
                }

                public double getLatitude() {
                    return latitude;
                }
            };
        }

        final double searchRadius;
        {
            final HaversineDistanceFunction<GeospatialPoint> distanceFunction =
                    new HaversineDistanceFunction<GeospatialPoint>();

            searchRadius = distanceFunction.getDistance(centroid, new GeospatialPoint() {
                public double getLongitude() {
                    return east;
                }

                public double getLatitude() {
                    return north;
                }
            });
        }

        final ArrayList<E> points = new ArrayList<E>();

        for (final E point : (List<E>)this.vpTree.getAllWithinRange(centroid, searchRadius)) {
            if (point.getLatitude() <= north && point.getLatitude() >= south) {
                // If the point is inside the bounding box, it will be shorter to get to the point by traveling east
                // from the western boundary than by traveling east from the eastern boundary.
                if (this.getDegreesEastFromMeridian(west, point) <= this.getDegreesEastFromMeridian(east, point)) {
                    // Similarly, it should be shorter to get to the point by traveling west from the eastern boundary
                    // than by traveling west from the western boundary.
                    if(this.getDegreesWestFromMeridian(east, point) <= this.getDegreesWestFromMeridian(west, point)) {
                        points.add(point);
                    }
                }
            }
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

    @SuppressWarnings("unchecked")
    public <T extends E> List<E> getNearestNeighbors(T queryPoint, int maxResults) {
        return (List<E>) this.vpTree.getNearestNeighbors(queryPoint, maxResults);
    }

    @SuppressWarnings("unchecked")
    public <T extends E> List<E> getAllWithinRange(T queryPoint, double maxDistance) {
        return (List<E>) this.vpTree.getAllWithinRange(queryPoint, maxDistance);
    }

    public int size() {
        return this.vpTree.size();
    }

    public boolean isEmpty() {
        return this.vpTree.isEmpty();
    }

    public boolean contains(Object o) {
        return this.vpTree.contains(o);
    }

    @SuppressWarnings("unchecked")
    public Iterator<E> iterator() {
        return (Iterator<E>) this.vpTree.iterator();
    }

    public Object[] toArray() {
        return this.vpTree.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return this.vpTree.toArray(a);
    }

    public boolean add(E e) {
        return this.vpTree.add(e);
    }

    public boolean remove(Object o) {
        return this.vpTree.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return this.vpTree.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        return this.vpTree.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return this.vpTree.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return this.vpTree.retainAll(c);
    }

    public void clear() {
        this.vpTree.clear();
    }
}
