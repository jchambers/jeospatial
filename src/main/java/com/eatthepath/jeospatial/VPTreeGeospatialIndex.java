package com.eatthepath.jeospatial;

import java.util.*;

import com.eatthepath.jvptree.PointFilter;
import com.eatthepath.jvptree.VPTree;

public class VPTreeGeospatialIndex<E extends GeospatialPoint> extends VPTree<GeospatialPoint, E> implements GeospatialIndex<E> {

    private static final PointFilter NO_OP_POINT_FILTER = new PointFilter() {
        @Override
        public boolean allowPoint(final Object point) {
            return true;
        }
    };

    private static final HaversineDistanceFunction HAVERSINE_DISTANCE_FUNCTION = new HaversineDistanceFunction();

    public VPTreeGeospatialIndex() {
        super(HAVERSINE_DISTANCE_FUNCTION);
    }

    public VPTreeGeospatialIndex(final Collection<E> points) {
        super(HAVERSINE_DISTANCE_FUNCTION, points);
    }

    public List<E> getAllPointsInBoundingBox(final double south, final double west, final double north, final double east) {
        //noinspection unchecked
        return getAllPointsInBoundingBox(south, west, north, east, NO_OP_POINT_FILTER);
    }

    @Override
    public List<E> getAllPointsInBoundingBox(final double south, final double west, final double north, final double east, final PointFilter<? super E> filter) {
        final GeospatialPoint centroid;
        {
            final double southRad = Math.toRadians(south);
            final double northRad = Math.toRadians(north);
            final double westRad = Math.toRadians(west);
            final double eastRad = Math.toRadians(east);

            // Via https://www.movable-type.co.uk/scripts/latlong.html
            final double Bx = Math.cos(northRad) * Math.cos(eastRad - westRad);
            final double By = Math.cos(northRad) * Math.sin(eastRad - westRad);

            final double latitudeRad = Math.atan2(Math.sin(southRad) + Math.sin(northRad), Math.sqrt((Math.cos(southRad) + Bx) * (Math.cos(southRad) + Bx) + (By * By)));
            final double longitudeRad = westRad + Math.atan2(By, Math.cos(southRad) + Bx);

            centroid = new SimpleGeospatialPoint(Math.toDegrees(latitudeRad), Math.toDegrees(longitudeRad));
        }

        // TODO There's almost certainly a more efficient way to figure this out
        final double searchRadius = Collections.max(Arrays.asList(
                HAVERSINE_DISTANCE_FUNCTION.getDistance(centroid, new SimpleGeospatialPoint(south, west)),
                HAVERSINE_DISTANCE_FUNCTION.getDistance(centroid, new SimpleGeospatialPoint(north, west)),
                HAVERSINE_DISTANCE_FUNCTION.getDistance(centroid, new SimpleGeospatialPoint(north, east)),
                HAVERSINE_DISTANCE_FUNCTION.getDistance(centroid, new SimpleGeospatialPoint(south, east))));

        final BoundingBoxPointFilter boundingBoxPointFilter = new BoundingBoxPointFilter(south, west, north, east);

        final PointFilter<? super E> combinedFilter = new PointFilter<E>() {

            @Override
            public boolean allowPoint(final E point) {
                return filter.allowPoint(point) && boundingBoxPointFilter.allowPoint(point);
            }
        };

        return this.getAllWithinDistance(centroid, searchRadius, combinedFilter);
    }
}
