package com.eatthepath.jeospatial;

import java.util.List;

/**
 * A GeospatialPointDatabase is a collection of geospatial points that can be
 * queried to locate points that are spatially close to a given query point.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public interface GeospatialPointIndex<E extends GeospatialPoint> {
    /**
     * <p>Returns a list of the nearest neighbors to a given query point. The
     * returned list is sorted by increasing distance from the query point.</p>
     * 
     * <p>This returned list will contain at most {@code maxResults} elements
     * (and may contain fewer if {@code maxResults} is larger than the number of
     * points in the database). If multiple points have the same distance from
     * the query point, the order in which they appear in the returned list is
     * undefined. By extension, if multiple points have the same distance from
     * the query point and those points would "straddle" the end of the returned
     * list, which points are included in the list and which are cut off is not
     * prescribed.</p>
     * 
     * @param queryPoint
     *            the point for which to find neighbors
     * @param maxResults
     *            the maximum length of the returned list
     * 
     * @return a list of the nearest neighbors to the given query point sorted
     *         by increasing distance from the query point
     */
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults);

    /**
     * Returns a list of all points within a given distance to a query point.
     * 
     * @param queryPoint
     *            the point for which to find neighbors
     * @param maxDistance
     *            the maximum allowable distance, in meters, from the query
     *            point; points farther away than {@code maxDistance} will not
     *            be included in the returned list
     * 
     * @return a list of all points within the given distance to the query
     *         point; the returned list is sorted in order of increasing
     *         distance from the query point
     */
    public List<E> getAllNeighborsWithinDistance(GeospatialPoint queryPoint, double maxDistance);

    /**
     * Returns a list of all points in the database within the given bounding
     * "box." A point is considered to be inside the box if its latitude falls
     * between the given north and south limits (inclusive) and its longitude
     * falls between the east and west limits (inclusive). The order of the
     * returned list is not prescribed.
     * 
     * @param west the western limit of the bounding box in degrees
     * @param east the eastern limit of the bounding box in degrees
     * @param north the northern limit of the bounding box in degrees
     * @param south the southern limit of the bounding box in degrees
     * 
     * @return a list of points in the database within the given bounding box
     * 
     * @throws IllegalArgumentException
     *             if the north or south limits fall outside of the range -90 to
     *             +90 (inclusive) or if the northern limit is south of the
     *             southern limit (or vice versa)
     */
    public List<E> getAllPointsInBoundingBox(double west, double east, double north, double south);
}
