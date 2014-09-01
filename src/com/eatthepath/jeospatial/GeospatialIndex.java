package com.eatthepath.jeospatial;

import java.util.List;

import com.eatthepath.jvptree.SpatialIndex;

/**
 * A GeospatialPointDatabase is a collection of geospatial points that can be
 * queried to locate points that are spatially close to a given query point.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public interface GeospatialIndex<E extends GeospatialPoint> extends SpatialIndex<E> {

    /**
     * Returns a list of all points in the index within the given bounding "box." A point is considered to be inside the
     * box if its latitude falls between the given north and south limits (inclusive) and its longitude falls between
     * the east and west limits (inclusive). The order of the returned list is not prescribed.
     * 
     * @param west the western limit of the bounding box in degrees
     * @param east the eastern limit of the bounding box in degrees
     * @param north the northern limit of the bounding box in degrees
     * @param south the southern limit of the bounding box in degrees
     * 
     * @return a list of points in the index within the given bounding box
     * 
     * @throws IllegalArgumentException if the north or south limits fall outside of the range -90 to +90 (inclusive) or
     * if the northern limit is south of the southern limit (or vice versa)
     */
    public List<E> getAllPointsInBoundingBox(double west, double east, double north, double south);
}
