package com.eatthepath.jeospatial.util;

import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.ArrayList;

import com.eatthepath.jeospatial.GeospatialPoint;
import com.eatthepath.jeospatial.SearchCriteria;

/**
 * <p>The {@code SearchResults} class is a fixed-size {@link PriorityQueue}
 * subclass that may or may not accept points that are offered to it based on
 * several criteria. {@code SearchResults} instances are always created with a
 * reference to a query point and a prescribed size; when points are offered via
 * the {@link SearchResults#add} or {@link SearchResults#addAll} methods, the
 * {@code SearchResults} instance will accept the point if and only if the
 * following conditions are met:</p>
 * 
 * <ol>
 * <li>The {@code SearchResults} instance contains fewer points than its
 * maximum capacity <em>or</em> the offered point is closer to the query point
 * than the most distant point already in the queue.</li>
 * <li>No maximum distance has been specified or the offered point falls within
 * the maximum allowable distance from the query point.</li>
 * <li>No search criteria have been specified or the offered point meets the
 * given search criteria.</li>
 * </ol>
 * 
 * <p>{@code SearchResults} objects use an internal reverse geospatial distance
 * comparator; this ensures that the point at the head of the queue is always
 * the most distant point from the query point.</p>
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class SearchResults<E extends GeospatialPoint> extends PriorityQueue<E> {
    private static final long serialVersionUID = 1L;
    
    private final CachingGeospatialPoint queryPoint;
    private final int maxSize;
    private final double maxDistance;
    private final SearchCriteria<E> criteria;
    
    /**
     * Constructs a new search result collector that collects up to
     * {@code maxSize} points.
     * 
     * @param queryPoint
     *            the point to use as the origin of all distance calculations
     * @param maxSize
     *            the maximum number of points this result set may contain
     */
    public SearchResults(GeospatialPoint queryPoint, int maxSize) {
        this(queryPoint, maxSize, Double.POSITIVE_INFINITY, null);
    }
    
    /**
     * Constructs a new search result collector that collects up to
     * {@code maxSize} points that fall within the given distance to the query
     * point.
     * 
     * @param queryPoint
     *            the point to use as the origin of all distance calculations
     * @param maxSize
     *            the maximum number of points this result set may contain
     * @param maxDistance
     *            the maximum allowable distance from the query point; points
     *            more distant than this threshold will always be rejected
     */
    public SearchResults(GeospatialPoint queryPoint, int maxSize, double maxDistance) {
        this(queryPoint, maxSize, maxDistance, null);
    }
    
    /**
     * Constructs a new search result collector that collects up to
     * {@code maxSize} points that meet the given search criteria.
     * 
     * @param queryPoint
     *            the point to use as the origin of all distance calculations
     * @param maxSize
     *            the maximum number of points this result set may contain
     * @param criteria
     *            the search criteria to be met by all points included in this
     *            result set; points that do not meet the search criteria will
     *            always be rejected
     */
    public SearchResults(GeospatialPoint queryPoint, int maxSize, SearchCriteria<E> criteria) {
        this(queryPoint, maxSize, Double.POSITIVE_INFINITY, criteria);
    }
    
    /**
     * Constructs a new search result collector that collects up to
     * {@code maxSize} points that fall within the given distance to the query
     * point and meet the given search criteria.
     * 
     * @param queryPoint
     *            the point to use as the origin of all distance calculations
     * @param maxSize
     *            the maximum number of points this result set may contain
     * @param maxDistance
     *            the maximum allowable distance from the query point; points
     *            more distant than this threshold will always be rejected
     * @param criteria
     *            the search criteria to be met by all points included in this
     *            result set; points that do not meet the search criteria will
     *            always be rejected
     */
    public SearchResults(GeospatialPoint queryPoint, int maxSize, double maxDistance, SearchCriteria<E> criteria) {
        super(maxSize, new ReverseComparator<E>(new GeospatialDistanceComparator<E>(queryPoint)));
        
        this.queryPoint = new CachingGeospatialPoint(queryPoint);
        this.maxSize = maxSize;
        this.maxDistance = maxDistance;
        this.criteria = criteria;
    }
    
    /**
     * Offers a point to this result set. The point will be added only if it
     * meets all criteria provided at construction time and if the search result
     * set is not yet full or the offered point is closer to the query point
     * than the most distant point currently in the result set.
     * 
     * @param point
     *            the point to offer to this result set
     * 
     * @return {@code true} if the point was added to this result set or
     *         {@code false} otherwise
     */
    @Override
    public boolean add(E point) {
        // If we have room for this point, we should always add it as long as
        // it's not outside of our maximum range and meets our search criteria.
        if(this.size() < this.maxSize) {
            // Is the point within range?
            if(this.maxDistance == Double.POSITIVE_INFINITY || this.queryPoint.getDistanceTo(point) <= this.maxDistance) {
                // Does it meet our criteria?
                if(this.criteria == null || this.criteria.matches(point)) {                
                    return super.add(point);
                }
            }
        } else {
            // The queue is full, so if we want to add this point, we need to
            // bump the most distant point from the head of the queue (and we
            // should only do that if this new point is closer than the current
            // most distant point). We DON'T need to explicitly check against
            // our maximum range because we know that the most distant point
            // already in the queue must already be within range, and the check
            // against the most distant point will implicitly check against the
            // maximum range.
            if(this.queryPoint.getDistanceTo(point) < this.getLongestDistanceFromQueryPoint()) {
                // ...we do still need to check against our search criteria,
                // though.
                if(this.criteria == null || this.criteria.matches(point)) {
                    this.poll();
                    return super.add(point);
                }
            }
        }
        
        // If we've made it this far, we decided not to add the point for any
        // number of reasons earlier.
        return false;
    }
    
    /**
     * Offers all of the points in the given collection to this result set.
     * 
     * @param points
     *            the collection of points to add to this queue
     * 
     * @return {@code true} if any of the offered points were added to this
     *         result set or {@code false} otherwise
     * 
     * @throws IllegalArgumentException
     *             if the specified collection is this result set
     * 
     * @see SearchResults#add(GeospatialPoint)
     */
    @Override
    public boolean addAll(Collection<? extends E> points) {
        if(this.equals(points)) {
            throw new IllegalArgumentException("Cannot addAll of a queue to itself.");
        }
        
        boolean anyAdded = false;
        
        for(E point : points) {
            boolean added = this.add(point);
            if(added) { anyAdded = true; }
        }
        
        return anyAdded;
    }
    
    /**
     * Returns the distance from the query point provided at construction time
     * to the most distant point in this result set.
     * 
     * @return the distance, in meters, from the query point to the most distant
     *         point in this result set, or {@link Double#POSITIVE_INFINITY} if
     *         this result set is empty
     */
    public double getLongestDistanceFromQueryPoint() {
        if(this.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        
        return this.queryPoint.getDistanceTo(this.peek());
    }
    
    /**
     * Returns a list of the points in this result set sorted in order of
     * increasing distance from the query point provided at construction time.
     * The result set itself is not modified by calls to this method.
     * 
     * @return a sorted list of the points in this result set
     */
    public List<E> toSortedList() {
        ArrayList<E> sortedList = new ArrayList<E>(this);
        java.util.Collections.sort(sortedList, new GeospatialDistanceComparator<E>(this.queryPoint));
        
        return sortedList;
    }
}
