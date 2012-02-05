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
     * Returns the nearest neighbor to the given query point. The nearest
     * neighbor is the point with the shortest distance to the query point; if
     * multiple points have the same distance from the query point, which of the
     * points is returned is undefined.
     * 
     * @param queryPoint
     *            the point for which to find the nearest neighbor
     * 
     * @return the nearest neighbor to the query point or {@code null} if the
     *         database contains no points
     */
    public E getNearestNeighbor(GeospatialPoint queryPoint);
    
    /**
     * Returns the nearest neighbor to the given query point so long as the
     * nearest neighbor is within the given maximum distance. The nearest
     * neighbor is the point with the shortest distance to the query point; if
     * multiple points have the same distance from the query point, which of the
     * points is returned is undefined.
     * 
     * @param queryPoint
     *            the point for which to find the nearest neighbor
     * 
     * @param maxDistance
     *            the maximum allowable distance from the query point in meters
     * 
     * @return the nearest neighbor to the query point or {@code null} if the
     *         database contains no points within {@code maxDistance} meters of
     *         the query point
     */
    public E getNearestNeighbor(GeospatialPoint queryPoint, double maxDistance);
    
    /**
     * Returns the nearest neighbor to the given query point that satisfies the
     * given search criteria. The nearest neighbor is the point with the
     * shortest distance to the query point; if multiple points have the same
     * distance from the query point, which of the points is returned is
     * undefined.
     * 
     * @param queryPoint
     *            the point for which to find the nearest neighbor
     * 
     * @param searchCriteria
     *            the criteria to apply to potential nearest neighbors
     * 
     * @return the nearest neighbor to the query point or {@code null} if the
     *         database contains no points that satisfy the given search
     *         criteria
     */
    public E getNearestNeighbor(GeospatialPoint queryPoint, SearchCriteria<E> searchCriteria);
    
    /**
     * Returns the nearest neighbor to the query point that satisfies the given
     * search criteria so long as that point falls within the given maximum
     * distance from the query point. The nearest neighbor is the point with the
     * shortest distance to the query point; if multiple points have the same
     * distance from the query point, which of the points is returned is
     * undefined.
     * 
     * @param queryPoint
     *            the point for which to find the nearest neighbor
     * @param maxDistance
     *            the maximum allowable distance from the query point in meters
     * @param searchCriteria
     *            the criteria to apply to potential nearest neighbors
     * 
     * @return the nearest neighbor to the query point or {@code null} if no
     *         points matching the given search criteria were found within
     *         {@code maxDistance} meters of the query point
     */
    public E getNearestNeighbor(GeospatialPoint queryPoint, double maxDistance, SearchCriteria<E> searchCriteria);
    
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
     * <p>Returns a list of the nearest neighbors to a given query point that
     * satisfy the given search criteria. The returned list is sorted by
     * increasing distance from the query point.</p>
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
     * @param searchCriteria
     *            the search criteria to be met by all returned points
     * 
     * @return a list, sorted in order of increasing distance from the query
     *         point, of the nearest neighbors to the given query point that
     *         meet the given search criteria
     */
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults, SearchCriteria<E> searchCriteria);
    
    /**
     * <p>Returns a list of the nearest neighbors to a given query point and
     * within a given maximum distance. The returned list is sorted by
     * increasing distance from the query point.</p>
     * 
     * <p>This returned list will contain at most {@code maxResults} elements
     * (and may contain fewer if {@code maxResults} is larger than the number of
     * points in the database or if fewer than {@code maxResults} points were
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
     *            point; points farther away than {@code maxDistance} will not
     *            be included in the returned list
     * 
     * @return a list of the nearest neighbors within the given maximum distance
     *         to the given query point; the returned list is sorted in order of
     *         increasing distance from the query point
     */
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults, double maxDistance);
    
    /**
     * <p>Returns a list of the nearest neighbors to a given query point and
     * within a given maximum distance. The returned list is sorted by
     * increasing distance from the query point.</p>
     * 
     * <p>This returned list will contain at most {@code maxResults} elements
     * (and may contain fewer if {@code maxResults} is larger than the number of
     * points in the database or if fewer than {@code maxResults} points were
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
     *            point; points farther away than {@code maxDistance} will not
     *            be included in the returned list
     * @param searchCriteria
     *            the search criteria to be met by all returned points
     * 
     * @return a list of the nearest neighbors that meet the given search
     *         criteria within the given maximum distance to the given query
     *         point; the returned list is sorted in order of increasing
     *         distance from the query point
     */
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults, double maxDistance, SearchCriteria<E> searchCriteria);
    
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
     * Returns a list of all points within a given distance to a query point
     * that meet a set of search criteria.
     * 
     * @param queryPoint
     *            the point for which to find neighbors
     * @param maxDistance
     *            the maximum allowable distance, in meters, from the query
     *            point; points farther away than {@code maxDistance} will not
     *            be included in the returned list
     * @param searchCriteria
     *            the search criteria to be met by all points in the returned
     *            list
     * 
     * @return a list of all points within the given distance to the query point
     *         that meet the given search criteria; the returned list is sorted
     *         in order of increasing distance from the query point
     */
    public List<E> getAllNeighborsWithinDistance(GeospatialPoint queryPoint, double maxDistance, SearchCriteria<E> searchCriteria);
    
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
    
    /**
     * Returns a list of all points in the database within the given bounding
     * "box." A point is considered to be inside the box if its latitude falls
     * between the given north and south limits (inclusive) and its longitude
     * falls between the east and west limits (inclusive). The list of returned
     * points is sorted in order of increasing distance from the given point.
     * 
     * @param west
     *            the western limit of the bounding box in degrees
     * @param east
     *            the eastern limit of the bounding box in degrees
     * @param north
     *            the northern limit of the bounding box in degrees
     * @param south
     *            the southern limit of the bounding box in degrees
     * @param orderingPoint
     *            a point to use for sorting the list of results by distance;
     *            may be {@code null} if no ordering is required
     * 
     * @return a list of points in the database within the given bounding box
     *         sorted in order of increasing distance from the
     *         {@code orderingPoint}
     * 
     * @throws IllegalArgumentException
     *             if the north or south limits fall outside of the range -90 to
     *             +90 (inclusive) or if the northern limit is south of the
     *             southern limit (or vice versa)
     */
    public List<E> getAllPointsInBoundingBox(double west, double east, double north, double south,
            GeospatialPoint orderingPoint);
    
    /**
     * Returns a list of all points in the database within the given bounding
     * "box" that also satisfy the given search criteria. A point is considered
     * to be inside the box if its latitude falls between the given north and
     * south limits (inclusive) and its longitude falls between the east and
     * west limits (inclusive). The order of the returned list is not
     * prescribed.
     * 
     * @param west
     *            the western limit of the bounding box in degrees
     * @param east
     *            the eastern limit of the bounding box in degrees
     * @param north
     *            the northern limit of the bounding box in degrees
     * @param south
     *            the southern limit of the bounding box in degrees
     * @param otherCriteria
     *            a set of additional search criteria to apply to points that
     *            lie within the bounding box; may be {@code null} if no
     *            additional criteria are to be applied
     * 
     * @return a list of points in the database within the given bounding box
     *         that satisfy the given search criteria
     * 
     * @throws IllegalArgumentException
     *             if the north or south limits fall outside of the range -90 to
     *             +90 (inclusive) or if the northern limit is south of the
     *             southern limit (or vice versa)
     */
    public List<E> getAllPointsInBoundingBox(double west, double east, double north, double south,
            SearchCriteria<E> otherCriteria);
    
    /**
     * Returns a list of all points in the database within the given bounding
     * "box" that also satisfy the given search criteria. A point is considered
     * to be inside the box if its latitude falls between the given north and
     * south limits (inclusive) and its longitude falls between the east and
     * west limits (inclusive). The list of returned points is sorted in order
     * of increasing distance from the given point.
     * 
     * @param west
     *            the western limit of the bounding box in degrees
     * @param east
     *            the eastern limit of the bounding box in degrees
     * @param north
     *            the northern limit of the bounding box in degrees
     * @param south
     *            the southern limit of the bounding box in degrees
     * @param otherCriteria
     *            a set of additional search criteria to apply to points that
     *            lie within the bounding box; may be {@code null} if no
     *            additional criteria are to be applied
     * @param orderingPoint
     *            a point to use for sorting the list of results by distance;
     *            may be {@code null} if no ordering is required
     * 
     * @return a list of points in the database within the given bounding box
     *         that satisfy the given search criteria sorted in order of
     *         increasing distance from the {@code orderingPoint}
     * 
     * @throws IllegalArgumentException
     *             if the north or south limits fall outside of the range -90 to
     *             +90 (inclusive) or if the northern limit is south of the
     *             southern limit (or vice versa)
     */
    public List<E> getAllPointsInBoundingBox(double west, double east, double north, double south,
            SearchCriteria<E> otherCriteria, GeospatialPoint orderingPoint);
    
    /**
     * Moves a point in the database to the given coordinates. Points in a
     * database should only ever be moved via this method (or its counterparts)
     * rather than the point's {@code setLatitude} or {@code setLongitude}
     * methods.
     * 
     * @param point the point to move
     * @param latitude the new latitude for the given point
     * @param longitude the new longitude for the given point
     * 
     * @see GeospatialPoint#setLatitude(double)
     * @see GeospatialPoint#setLongitude(double)
     */
    public void movePoint(E point, double latitude, double longitude);
    
    /**
     * Moves a point in the database to position of the given destination point.
     * Points in a database should only ever be moved via this method (or its
     * counterparts) rather than the point's {@code setLatitude} or
     * {@code setLongitude} methods.
     * 
     * @param point the point to move
     * @param destination the location to which the given point should be moved
     * 
     * @see GeospatialPoint#setLatitude(double)
     * @see GeospatialPoint#setLongitude(double)
     */
    public void movePoint(E point, GeospatialPoint destination);
}
