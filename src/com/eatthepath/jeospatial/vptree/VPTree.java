package com.eatthepath.jeospatial.vptree;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import com.eatthepath.jeospatial.CachingGeospatialPoint;
import com.eatthepath.jeospatial.GeospatialPoint;
import com.eatthepath.jeospatial.GeospatialPointDatabase;
import com.eatthepath.jeospatial.SearchCriteria;
import com.eatthepath.jeospatial.util.GeospatialDistanceComparator;
import com.eatthepath.jeospatial.util.SearchResults;

/**
 * <p>A geospatial database that uses a vantage point tree as its storage
 * mechanism.</p>
 * 
 * <p>Vantage point trees (or "vp-trees") are a subclass of metric trees.
 * Vantage point trees use binary space partitioning to recursively divide
 * points among their nodes. Nodes in a vantage point tree have a center point
 * and a distance threshold; points with a distance less than or equal to the
 * threshold are assigned to the "left" child of a node and the others are
 * assigned to the "right" child.</p>
 * 
 * <p>Queries in a vp-tree execute in O(log n) time in the best case, and tree
 * construction takes O(n log n) time.</p>
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 * 
 * @see <a href="http://pnylab.com/pny/papers/vptree/main.html">Peter N. Yianilos' original paper on vp-trees</a>
 */
public class VPTree<E extends GeospatialPoint> implements GeospatialPointDatabase<E> {
    protected class VPNode<T extends GeospatialPoint> {
        private CachingGeospatialPoint center;
        private double threshold;
        
        private VPNode<T> closer;
        private VPNode<T> farther;
        
        private Vector<T> points;
        private final int binSize;
        
        public VPNode(int binSize) {
            this.binSize = binSize;
            this.points = new Vector<T>(this.binSize);
            
            this.center = null;
        }
        
        public VPNode(T[] points, int fromIndex, int toIndex, int binSize) {
            this.binSize = binSize;
            
            if(toIndex - fromIndex <= binSize) {
                // All done! This is a leaf node.
                this.storePoints(points, fromIndex, toIndex);
            } else {
                // We have more points than we want to store in a single leaf
                // node; try to partition the nodes.
                try {
                    this.partition(points, fromIndex, toIndex);
                } catch(PartitionException e) {
                    // Partitioning failed; this is most likely because all of
                    // the points we were given are coincident.
                    this.storePoints(points, fromIndex, toIndex);
                }
            }
        }
        
        protected VPNode<T> getCloserNode() {
            return this.closer;
        }
        
        protected VPNode<T> getFartherNode() {
            return this.farther;
        }
        
        public boolean addAll(Collection<? extends T> points) {
            HashSet<VPNode<T>> nodesToPartition = new HashSet<VPNode<T>>();
            
            for(T point : points) {
                this.add(point, true, nodesToPartition);
            }
            
            // Resolve all of the deferred partitioning
            for(VPNode<T> node : nodesToPartition) {
                try {
                    node.partition();
                } catch(PartitionException e) {
                    // Nothing to do here; this just means some nodes are bigger
                    // than they want to be.
                }
            }
            
            // The tree was definitely modified as long as we were given a
            // non-empty collection of points to add.
            return !points.isEmpty();
        }
        
        public boolean add(T point) {
            return this.add(point, false, null);
        }
        
        protected boolean add(T point, boolean deferPartitioning, Set<VPNode<T>> nodesToPartition) {
            if(this.isLeafNode()) {
                this.points.add(point);
                
                // Should we try to repartition?
                if(this.points.size() > this.binSize) {
                    if(deferPartitioning) {
                        nodesToPartition.add(this);
                    } else {
                        try {
                            this.partition();
                        } catch(PartitionException e) {
                            // Nothing to do here; just hold on to all of our
                            // points.
                        }
                    }
                }
            } else {
                if(this.center.getDistanceTo(point) <= this.threshold) {
                    return this.closer.add(point);
                } else {
                    return this.farther.add(point);
                }
            }
            
            return true;
        }
        
        public boolean contains(T point) {
            if(this.isLeafNode()) {
                return this.points.contains(point);
            } else {
                if(this.center.getDistanceTo(point) <= this.threshold) {
                    return this.closer.contains(point);
                } else {
                    return this.farther.contains(point);
                }
            }
        }
        
        public int size() {
            if(this.isLeafNode()) {
                return this.points.size();
            } else {
                return this.closer.size() + this.farther.size();
            }
        }
        
        private void storePoints(T[] points, int fromIndex, int toIndex) {
            this.points = new Vector<T>(this.binSize);
            
            for(int i = fromIndex; i < toIndex; i++) {
                this.points.add(points[i]);
            }
            
            this.closer = null;
            this.farther = null;
        }
        
        protected void partition() throws PartitionException {
            if(!this.isLeafNode()) {
                throw new PartitionException("Cannot partition a non-leaf node.");
            }
            
            if(!this.isEmpty()) {
                @SuppressWarnings("unchecked")
                T[] pointArray = this.points.toArray((T[])Array.newInstance(points.iterator().next().getClass(), 0));
                
                this.partition(pointArray, 0, pointArray.length);
            }
        }
        
        protected void partition(T[] points, int fromIndex, int toIndex) throws PartitionException {
            // We start by choosing a center point at random and a distance
            // threshold; the median distance from our center to points in our
            // set is a safe bet.
            Random r = new Random();
            this.center = new CachingGeospatialPoint(points[r.nextInt(points.length)]);

            // TODO Consider optimizing this
            java.util.Arrays.sort(points, fromIndex, toIndex,
                    new GeospatialDistanceComparator<T>(this.center));
            
            int medianIndex = (fromIndex + toIndex - 1) / 2;
            double medianDistance = this.center.getDistanceTo(points[medianIndex]);
            
            // Since we're picking a definite median value from the list, we're
            // guaranteed to have at least one point that is closer to or EQUAL TO
            // (via identity) the threshold; what we want to do now is find the
            // first point that's farther away and use that as our partitioning
            // point.
            int partitionIndex = -1;
            
            for(int i = medianIndex + 1; i < toIndex; i++) {
                if(this.center.getDistanceTo(points[i]) > medianDistance) {
                    partitionIndex = i;
                    this.threshold = medianDistance;
                    
                    break;
                }
            }
            
            // Did we find a point that's farther away than the median distance? If
            // so, great! If not, move the threshold closer in and see if we can
            // find a distance that partitions our point set.
            if(partitionIndex == -1) {
                for(int i = medianIndex; i > fromIndex; i--) {
                    if(this.center.getDistanceTo(points[i]) < medianDistance) {
                        partitionIndex = i;
                        this.threshold = this.center.getDistanceTo(points[i]);
                        
                        break;
                    }
                }
            }
            
            // Still nothing? Bail out.
            if(partitionIndex == -1) {
                throw new PartitionException(
                    "No viable partition threshold found (all points have equal distance from center).");
            }
            
            // Okay! Now actually use that partition index.
            this.closer = new VPNode<T>(points, fromIndex, partitionIndex, this.binSize);
            this.farther = new VPNode<T>(points, partitionIndex, toIndex, this.binSize);
            
            // We're definitely not a leaf node now, so clear out our internal
            // point vector (if we had one).
            this.points = null;
        }
        
        public boolean isLeafNode() {
            return this.closer == null;
        }
        
        public boolean isEmpty() {
            if(this.isLeafNode()) {
                return this.points.isEmpty();
            } else {
                return (this.closer.isEmpty() && this.farther.isEmpty());
            }
        }

        protected void getNearestNeighbors(final GeospatialPoint queryPoint, final SearchResults<T> results) {
            // If this is a leaf node, our job is easy. Offer all of our points
            // to the result set and bail out.
            if(this.isLeafNode()) {
                results.addAll(this.points);
            } else {
                // Descend through the tree recursively.
                boolean searchedCloserFirst;
                double distanceToCenter = this.center.getDistanceTo(queryPoint);
                
                if(distanceToCenter <= this.threshold) {
                    this.closer.getNearestNeighbors(queryPoint, results);
                    searchedCloserFirst = true;
                } else {
                    this.farther.getNearestNeighbors(queryPoint, results);
                    searchedCloserFirst = false;
                }
                
                // ...and now we're on our way back up. Decide if we need to search
                // whichever child we didn't search on the way down.
                if(searchedCloserFirst) {
                    // We want to search the farther node if it's easier to get from
                    // the query point to our threshold than it is to get to our own
                    // center point.
                    double distanceToThreshold = this.threshold - distanceToCenter;
                    
                    if(distanceToThreshold < distanceToCenter) {
                        this.farther.getNearestNeighbors(queryPoint, results);
                    }
                } else {
                    // We want to search the closer node if any part of our region
                    // is closer to the query point than the worst match in the
                    // result set.
                    double distanceToRegion = distanceToCenter - this.threshold;
                    
                    if(distanceToRegion <= results.getLongestDistanceFromQueryPoint()) {
                        this.closer.getNearestNeighbors(queryPoint, results);
                    }
                }
            }
        }
        
        protected void getAllWithinRange(final CachingGeospatialPoint queryPoint, final double maxDistance, final SearchCriteria<T> criteria, final Vector<T> results) {
            // If this is a leaf node, just add all of our points to the list if
            // they fall within range and meet the search criteria (if any).
            if(this.isLeafNode()) {
                for(T point : this.points) {
                    if(queryPoint.getDistanceTo(point) <= maxDistance) {
                        if(criteria == null || criteria.matches(point)) {
                            results.add(point);
                        }
                    }
                }
            } else {
                // We want to search whichever of our nodes intersect with the
                // query region, which remains static throughout an
                // "all within range" search.
                double distanceToQueryPoint = this.center.getDistanceTo(queryPoint);
                
                // Does any part of the query region fall within our threshold?
                if(distanceToQueryPoint <= this.threshold + maxDistance) {
                    this.closer.getAllWithinRange(queryPoint, maxDistance, criteria, results);
                }
                
                // Does any part of the query region fall outside of our
                // threshold? Or, put differently, does our region fail to
                // completely enclose the query region?
                if(distanceToQueryPoint + maxDistance > this.threshold) {
                    this.farther.getAllWithinRange(queryPoint, maxDistance, criteria, results);
                }
            }
        }
    }
    
    private static final int DEFAULT_BIN_SIZE = 32;
    private final int binSize;
    
    private VPNode<E> root;
    
    /**
     * Constructs a new, empty vp-tree with a default node capacity.
     */
    public VPTree() {
        this(DEFAULT_BIN_SIZE);
    }
    
    /**
     * Constructs a new, empty vp-tree with the specified node capacity.
     * 
     * @param nodeCapacity
     *            the maximum number of points to store in a leaf node of the
     *            tree
     */
    public VPTree(int nodeCapacity) {
        this.binSize = nodeCapacity;
        this.root = new VPNode<E>(this.binSize);
    }
    
    /**
     * Constructs a new vp-tree that contains (and indexes) all of the points in
     * the given collection. Nodes of the tree are created with a default
     * capacity.
     * 
     * @param points
     *            the points to use to populate this tree
     */
    public VPTree(Collection<E> points) {
        this(points, DEFAULT_BIN_SIZE);
    }
    
    public VPTree(Collection<E> points, int nodeCapacity) {
        this.binSize = nodeCapacity;
        
        if(!points.isEmpty()) {
            @SuppressWarnings("unchecked")
            E[] pointArray = points.toArray((E[])Array.newInstance(points.iterator().next().getClass(), 0));
            
            this.root = new VPNode<E>(pointArray, 0, pointArray.length, this.binSize);
        } else {
            this.root = new VPNode<E>(this.binSize);
        }
    }

    @Override
    public boolean add(E point) {
        return this.root.add(point);
    }

    @Override
    public boolean addAll(Collection<? extends E> points) {
        return this.root.addAll(points);
    }

    @Override
    public void clear() {
        this.root = new VPNode<E>(this.binSize);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        try {
            return this.root.contains((E)o);
        } catch(ClassCastException e) {
            return false;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object o : c) {
            if(!this.contains(o)) { return false; }
        }
        
        return true;
    }

    @Override
    public boolean isEmpty() {
        return this.root.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean remove(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object[] toArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint,
            int maxResults) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint,
            int maxResults, double maxDistance) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint,
            int maxResults, SearchCriteria<E> searchCriteria) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint,
            int maxResults, double maxDistance, SearchCriteria<E> searchCriteria) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<E> getAllNeighborsWithinDistance(GeospatialPoint queryPoint,
            double maxDistance) {
        return this.getAllNeighborsWithinDistance(queryPoint, maxDistance, null);
    }

    @Override
    public List<E> getAllNeighborsWithinDistance(GeospatialPoint queryPoint,
            double maxDistance, SearchCriteria<E> searchCriteria) {
        Vector<E> results = new Vector<E>(this.binSize);
        this.root.getAllWithinRange(new CachingGeospatialPoint(queryPoint), maxDistance, searchCriteria, results);
        
        java.util.Collections.sort(results, new GeospatialDistanceComparator<E>(queryPoint));
        return results;
    }

    @Override
    public void movePoint(E point, double latitude, double longitude) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void movePoint(E point, GeospatialPoint destination) {
        this.movePoint(point, destination.getLatitude(), destination.getLongitude());
    }
}
