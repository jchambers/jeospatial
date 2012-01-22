package com.eatthepath.jeospatial.util;

import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

import com.eatthepath.jeospatial.CachingGeospatialPoint;
import com.eatthepath.jeospatial.GeospatialPoint;
import com.eatthepath.jeospatial.SearchCriteria;

public class SearchResults<E extends GeospatialPoint> extends PriorityQueue<E> {
    private static final long serialVersionUID = 1L;
    
    private final CachingGeospatialPoint queryPoint;
    private final int maxSize;
    private final double maxDistance;
    private final SearchCriteria<E> criteria;
    
    public SearchResults(GeospatialPoint queryPoint, int maxSize) {
        this(queryPoint, maxSize, Double.POSITIVE_INFINITY, null);
    }
    
    public SearchResults(GeospatialPoint queryPoint, int maxSize, double maxDistance) {
        this(queryPoint, maxSize, maxDistance, null);
    }
    
    public SearchResults(GeospatialPoint queryPoint, int maxSize, SearchCriteria<E> criteria) {
        this(queryPoint, maxSize, Double.POSITIVE_INFINITY, criteria);
    }
    
    public SearchResults(GeospatialPoint queryPoint, int maxSize, double maxDistance, SearchCriteria<E> criteria) {
        super(maxSize, new ReverseComparator<E>(new GeospatialDistanceComparator<E>(queryPoint)));
        
        this.queryPoint = new CachingGeospatialPoint(queryPoint);
        this.maxSize = maxSize;
        this.maxDistance = maxDistance;
        this.criteria = criteria;
    }
    
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
    
    @Override
    public boolean addAll(Collection<? extends E> points) {
        if(this.equals(points)) {
            throw new IllegalArgumentException("Cannot addAll of a queue to itself.");
        }
        
        boolean changed = false;
        
        for(E point : points) {
            changed = changed || this.add(point);
        }
        
        return changed;
    }
    
    public double getLongestDistanceFromQueryPoint() {
        if(this.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        
        return this.queryPoint.getDistanceTo(this.peek());
    }
    
    public List<E> toSortedList() {
        Vector<E> sortedList = new Vector<E>(this);
        java.util.Collections.sort(sortedList, new GeospatialDistanceComparator<E>(this.queryPoint));
        
        return sortedList;
    }
}
