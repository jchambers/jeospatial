package com.eatthepath.jeospatial;

import java.util.Collection;
import java.util.List;

/**
 * A GeospatialPointDatabase is a collection of geospatial points that can be
 * queried to locate points that are spatially close to a given query point.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public interface GeospatialPointDatabase<E extends GeospatialPoint> extends Collection<E> {
    /**
     * <p>Returns a list of the nearest neighbors to a given query point. The
     * returned list is sorted by increasing distance from the query point.</p>
     * 
     * <p>This returned list will contain at most @{code maxResults} elements
     * (and may contain fewer if @{code maxResults} is larger than the number of
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
    
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults, SearchCriteria<E> searchCriteria);
    
    /**
     * <p>Returns a list of the nearest neighbors to a given query point and
     * within a given maximum distance. The returned list is sorted by
     * increasing distance from the query point.</p>
     * 
     * <p>This returned list will contain at most @{code maxResults} elements
     * (and may contain fewer if @{code maxResults} is larger than the number of
     * points in the database or if fewer than @{code maxResults} points were
     * found within the given maximum distance). If multiple points have the
     * same distance from the query point, the order in which they appear in the
     * returned list is undefined. By extension, if multiple points have the
     * same distance from the query point and those points would "straddle" the
     * end of the returned list, which points are included in the list and which
     * are cut off is not prescribed.</p>
     * 
     * @param queryPoint
     *            the point for which to find neighbors
     * @param maxResults
     *            the maximum length of the returned list
     * @param maxDistance
     *            the maximum allowable distance, in meters, from the query
     *            point; points farther away than @{code maxDistance} will not
     *            be included in the returned list
     * 
     * @return a list of the nearest neighbors within the given maximum distance
     *         to the given query point; the returned list is sorted in order of
     *         increasing distance from the query point
     */
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults, double maxDistance);
    
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults, double maxDistance, SearchCriteria<E> searchCriteria);
    
    /**
     * Returns a list of all of the points within a given distance to the given
     * query point.
     * 
     * @param queryPoint
     *            the point for which to find neighbors
     * @param maxDistance
     *            the maximum allowable distance, in meters, from the query
     *            point; points farther away than @{code maxDistance} will not
     *            be included in the returned list
     * 
     * @return a list of all points within the given distance to the query point
     */
    public List<E> getAllNeighborsWithinDistance(GeospatialPoint queryPoint, double maxDistance);
    
    public List<E> getAllNeighborsWithinDistance(GeospatialPoint queryPoint, double maxDistance, SearchCriteria<E> searchCriteria);
    
    public void movePoint(E point, double latitude, double longitude);
    public void movePoint(E point, GeospatialPoint destination);
}
