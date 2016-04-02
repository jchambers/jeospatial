package com.eatthepath.jeospatial;

import java.util.List;

import com.eatthepath.jvptree.SpatialIndex;

/**
 * A collection of points on the earth's surface that can be searched efficiently to find points near a given query
 * point.
 *
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public interface GeospatialIndex<E extends GeospatialPoint> extends SpatialIndex<GeospatialPoint, E> {

    /**
     * Returns a list of all points in the index within the given bounding "box." A point is considered to be inside the
     * box if its latitude falls between the given north and south limits (inclusive) and its longitude falls between
     * the east and west limits (inclusive). The order of the returned list is not prescribed.
     *
     * @param south the southern limit of the bounding box in degrees
     * @param west the western limit of the bounding box in degrees
     * @param north the northern limit of the bounding box in degrees
     * @param east the eastern limit of the bounding box in degrees
     *
     * @return a list of points in the index within the given bounding box
     *
     * @throws IllegalArgumentException if the north or south limits fall outside of the range -90 to +90 (inclusive) or
     * if the northern limit is south of the southern limit (or vice versa)
     */
    public List<E> getAllPointsInBoundingBox(double south, double west, double north, double east);
}
