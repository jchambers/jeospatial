package com.eatthepath.jeospatial.vptree;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.eatthepath.jeospatial.CachingGeospatialPoint;
import com.eatthepath.jeospatial.GeospatialPoint;
import com.eatthepath.jeospatial.GeospatialPointDatabase;
import com.eatthepath.jeospatial.SearchCriteria;
import com.eatthepath.jeospatial.SimpleGeospatialPoint;
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
 * @see <a href="http://pnylab.com/pny/papers/vptree/main.html">Yianilos, Peter
 *      N. "Data Structures and Algorithms for Nearest Neighbor Search in
 *      General Metric Spaces". Proceedings of the Fifth Annual ACM-SIAM
 *      Symposium on Discrete Algorithms (SODA). 1993.</a>
 */
public class VPTree<E extends GeospatialPoint> implements GeospatialPointDatabase<E> {
    protected class VPNode<T extends GeospatialPoint> {
        private CachingGeospatialPoint center;
        private double threshold;
        
        private VPNode<T> closer;
        private VPNode<T> farther;
        
        private Vector<T> points;
        private final int binSize;
        
        /**
         * Constructs a new, empty node with the given capacity.
         * 
         * @param binSize the largest number of points this node should hold
         */
        public VPNode(int binSize) {
            this.binSize = binSize;
            this.points = new Vector<T>(this.binSize);
            
            this.center = null;
        }
        
        /**
         * Constructs a new node that contains a subset of the given array of
         * points. If the subset of points is larger than the given bin
         * capacity, child nodes will be created recursively.
         * 
         * @param points
         *            the array of points from which to build this node
         * @param fromIndex
         *            the starting index (inclusive) of the subset of the array
         *            from which to build this node
         * @param toIndex
         *            the end index (exclusive) of the subset of the array from
         *            which to build this node
         * @param binSize
         *            the largest number of points this node should hold
         */
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
        
        /**
         * Returns a reference to this node's child that contains points that
         * are closer to this node's center than this node's distance threshold.
         * 
         * @return a reference to this node's "closer" child, or {@code null} if
         *         this is a leaf node
         * 
         * @see VPNode#isLeafNode()
         */
        protected VPNode<T> getCloserNode() {
            return this.closer;
        }
        
        /**
         * Returns a reference to this node's child that contains points that
         * are farther away from this node's center than this node's distance
         * threshold.
         * 
         * @return a reference to this node's "farther" child, or {@code null}
         *         if this is a leaf node
         * 
         * @see VPNode#isLeafNode()
         */
        protected VPNode<T> getFartherNode() {
            return this.farther;
        }
        
        /**
         * Returns a point that is coincident with this node's center point.
         * 
         * @return a point that is coincident with this node's center point
         */
        protected GeospatialPoint getCenter() {
            return new SimpleGeospatialPoint(this.center);
        }
        
        protected double getThreshold() {
            return this.threshold;
        }
        
        /**
         * <p>Adds all of the points in a collection to this node (if it is a
         * leaf node) or its children. If this node is a leaf node and the added
         * points push this node beyond its capacity, it is partitioned as
         * needed after all points have been added.</p>
         * 
         * <p>This method defers partitioning of child nodes until all points
         * have been added.</p>
         * 
         * @param points
         *            the collection of points to add to this node or its
         *            children
         * 
         * @return {@code true} if this node or its children were modified or
         *         {@code false} otherwise
         */
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
        
        /**
         * Adds a point to this node if it is a leaf node or one of its children
         * if not. If the node that ultimately holds the new point is loaded
         * beyond its capacity, it will be partitioned.
         * 
         * @param point
         *            the point to add to this node or one of its children
         * 
         * @return {@code true} if this node or one of its children was modified
         *         by the addition of this point or {@code false} otherwise
         */
        public boolean add(T point) {
            return this.add(point, false, null);
        }
        
        /**
         * <p>Adds a point to this node if it is a leaf node or one of its
         * children if not. If the node that ultimately holds the new point is
         * loaded beyond its capacity, it will be partitioned.</p>
         * 
         * <p>Partitioning may optionally be deferred, in which case it is the
         * responsibility of the caller to partition overloaded nodes.</p>
         * 
         * @param point
         *            the point to add to this node or one of its children
         * @param deferPartitioning
         *            if {@code true}, defer partitioning of overloaded nodes
         *            until the caller chooses to partition them; if
         *            {@code false}, overloaded nodes are partitioned
         *            immediately
         * @param nodesToPartition
         *            a {@code Set} that collects overloaded nodes in need of
         *            deferred partitioning; this may be {@code null} if
         *            {@code deferPartitioning} is {@code false}; callers must
         *            use this set to partition nodes later
         * 
         * @return {@code true} if this node or any of its children were
         *         modified by the addition of the new point or {@code false}
         *         otherwise; note that adding points always results in
         *         modification
         */
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
            
            // There's no way to add a point and not modify the tree.
            return true;
        }
        
        /**
         * Tests whether this node or one of its children contains the given
         * point.
         * 
         * @param point
         *            the point whose presence is to be tested
         * 
         * @return {@code true} if the given point is present in this node or
         *         one of its children or {@code false} otherwise
         */
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
        
        /**
         * Returns the number of points contained in this node and its child
         * nodes.
         * 
         * @return the number of points in this node and its children
         */
        public int size() {
            if(this.isLeafNode()) {
                return this.points.size();
            } else {
                return this.closer.size() + this.farther.size();
            }
        }
        
        /**
         * Stores a subset of an array of points in this node directly, making
         * this node a leaf node.
         * 
         * @param points
         *            the array of points from which to store a subset
         * @param fromIndex
         *            the starting index (inclusive) of the subset of the array
         *            to store
         * @param toIndex
         *            the end index (exclusive) of the subset of the array to
         *            store
         */
        private void storePoints(T[] points, int fromIndex, int toIndex) {
            this.points = new Vector<T>(this.binSize);
            
            for(int i = fromIndex; i < toIndex; i++) {
                this.points.add(points[i]);
            }
            
            // Always choose a center point if we don't already have one
            if(this.center == null && !this.points.isEmpty()) {
                this.center = new CachingGeospatialPoint(this.points.get(0));
            }
            
            this.closer = null;
            this.farther = null;
        }
        
        /**
         * Returns a collection of all the points stored directly in this node.
         * 
         * @return a collection of all the points stored directly in this node
         * 
         * @throws IllegalStateException if this node is not a leaf node
         */
        public Collection<T> getPoints() {
            if(!this.isLeafNode()) {
                throw new IllegalStateException("Cannot retrieve points from a non-leaf node.");
            }
            return new Vector<T>(this.points);
        }
        
        protected void partition() throws PartitionException {
            if(!this.isLeafNode()) {
                throw new PartitionException("Cannot partition a non-leaf node.");
            }
            
            if(!this.isEmpty()) {
                @SuppressWarnings("unchecked")
                T[] pointArray = this.points.toArray((T[])Array.newInstance(points.iterator().next().getClass(), 0));
                
                this.partition(pointArray, 0, pointArray.length);
            } else {
                throw new PartitionException("Cannot partition an empty node.");
            }
        }
        
        protected void partition(T[] points, int fromIndex, int toIndex) throws PartitionException {
            // We can't partition fewer then two points.
            if(toIndex - fromIndex < 2) {
                throw new PartitionException("Cannot partition fewer than two points.");
            }
            
            // We start by choosing a center point and a distance threshold; the
            // median distance from our center to points in our set is a safe
            // bet.
            if(this.center == null) {
                this.center = new CachingGeospatialPoint(points[fromIndex]);
            }

            // TODO Consider optimizing this
            java.util.Arrays.sort(points, fromIndex, toIndex,
                    new GeospatialDistanceComparator<T>(this.center));
            
            int medianIndex = (fromIndex + toIndex - 1) / 2;
            double medianDistance = this.center.getDistanceTo(points[medianIndex]);
            
            // Since we're picking a definite median value from the list, we're
            // guaranteed to have at least one point that is closer to or EQUAL TO
            // (via identity) the threshold; what we want to do now is make sure
            // there's at least one point that's farther away from the center
            // than the threshold.
            int partitionIndex = -1;
            
            for(int i = medianIndex + 1; i < toIndex; i++) {
                if(this.center.getDistanceTo(points[i]) > medianDistance) {
                    // We found at least one point farther away than the median
                    // distance. That means we can use the median as our
                    // distance threshold and everything after that point in the
                    // sorted array as members of the "farther" node.
                    partitionIndex = i;
                    this.threshold = medianDistance;
                    
                    break;
                }
            }
            
            // Did we find a point that's farther away than the median distance?
            // If so, great!
            //
            // If not, we know that all points after the median point in the
            // sorted array have the same distance from the center, and we need
            // to try to move the threshold back until we find a point that's
            // less distant than the median distance. If we find such a point,
            // we'll use its distance from the center as our distance threshold.
            // If the median distance is zero, though, and we've made it this
            // far, we know there's nothing MORE distant than that and shouldn't
            // spend time searching.
            if(partitionIndex == -1) {
                if(medianDistance != 0) {
                    for(int i = medianIndex; i > fromIndex; i--) {
                        if(this.center.getDistanceTo(points[i]) < medianDistance) {
                            partitionIndex = i;
                            this.threshold = this.center.getDistanceTo(points[i]);
                            
                            break;
                        }
                    }
                    
                    // Did we still fail to find anything? There's still one
                    // special case that can save us. If we've made it here, we
                    // know that everything except the center point has the same
                    // non-zero distance from the center. We can and should
                    // still partition by putting the center alone in the
                    // "closer" node and everything else in the "farther" node.
                    // This, of course, assumes there's more than one point to
                    // work with.
                    if(partitionIndex == -1) {
                        partitionIndex = fromIndex + 1;
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
        
        /**
         * Tests whether this is a leaf node.
         * 
         * @return {@code true} if this node is a leaf node or {@code false}
         *         otherwise
         */
        public boolean isLeafNode() {
            return this.closer == null;
        }
        
        /**
         * Tests whether this node and all of its children are empty.
         * 
         * @return {@code true} if this node and all of its children contain no
         *         points or {@code false} otherwise
         */
        public boolean isEmpty() {
            if(this.isLeafNode()) {
                return this.points.isEmpty();
            } else {
                return (this.closer.isEmpty() && this.farther.isEmpty());
            }
        }
        
        public boolean isOverloaded() {
            if(!this.isLeafNode()) {
                throw new IllegalStateException("Non-leaf nodes cannot be overloaded.");
            }
            
            return this.points.size() > this.binSize;
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
                    // We've already searched the node that contains points
                    // within our threshold (which also implies that the query
                    // point is inside our threshold); we also want to search
                    // the node beyond our threshold if the distance from the
                    // query point to the most distant match is longer than the
                    // distance from the query point to our threshold, since
                    // there could be a point outside our threshold that's
                    // closer than the most distant match.
                    double distanceToThreshold = this.threshold - distanceToCenter;
                    
                    if(results.getLongestDistanceFromQueryPoint() > distanceToThreshold) {
                        this.farther.getNearestNeighbors(queryPoint, results);
                    }
                } else {
                    // We've already searched the node that contains points
                    // beyond our threshold, and the query point itself is
                    // beyond our threshold. We want to search the
                    // within-threshold node if it's "easier" to get from the
                    // query point to our region than it is to get from the
                    // query point to the most distant match, since there could
                    // be a point within our threshold that's closer than the
                    // most distant match.
                    double distanceToThreshold = distanceToCenter - this.threshold;
                    
                    if(distanceToThreshold <= results.getLongestDistanceFromQueryPoint()) {
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
        
        protected void addPointsToArray(Object[] array) {
            this.addPointsToArray(array, 0);
        }
        
        protected int addPointsToArray(Object[] array, int offset) {
            if(this.isLeafNode()) {
                if(this.isEmpty()) { return 0; }
                
                System.arraycopy(this.points.toArray(), 0, array, offset, this.points.size());
                
                return this.points.size();
            } else {
                int nAddedFromCloser = this.closer.addPointsToArray(array, offset);
                int nAddedFromFarther = this.farther.addPointsToArray(array, offset + nAddedFromCloser);
                
                return nAddedFromCloser + nAddedFromFarther;
            }
        }
        
        public void findNodeContainingPoint(GeospatialPoint p, Deque<VPNode<T>> stack) {
            // First things first; add ourselves to the stack.
            stack.push(this);
            
            // If this is a leaf node, we don't need to do anything else. If
            // it's not a leaf node, recurse!
            if(!this.isLeafNode()) {
                if(this.center.getDistanceTo(p) <= this.threshold) {
                    this.closer.findNodeContainingPoint(p, stack);
                } else {
                    this.farther.findNodeContainingPoint(p, stack);
                }
            }
        }
        
        public boolean remove(T point) {
            if(this.isLeafNode()) {
                return this.points.remove(point);
            } else {
                throw new IllegalStateException("Cannot remove points from a non-leaf node.");
            }
        }
        
        public void absorbChildren() {
            if(this.isLeafNode()) {
                throw new IllegalStateException("Leaf nodes have no children.");
            }
            
            this.points = new Vector<T>(this.binSize);
            
            if(!this.closer.isLeafNode()) {
                this.closer.absorbChildren();
            }
            
            if(!this.farther.isLeafNode()) {
                this.farther.absorbChildren();
            }
            
            this.points.addAll(this.closer.getPoints());
            this.points.addAll(this.farther.getPoints());
            
            this.closer = null;
            this.farther = null;
        }
        
        public void gatherLeafNodes(List<VPNode<T>> leafNodes) {
            if(this.isLeafNode()) {
                leafNodes.add(this);
            } else {
                this.closer.gatherLeafNodes(leafNodes);
                this.farther.gatherLeafNodes(leafNodes);
            }
        }
        
        public boolean isAncestorOfNode(VPNode<T> node) {
            // Obviously, leaf nodes can't be the ancestors of anything
            if(this.isLeafNode()) { return false; }
            
            // Find a path to the center of the node we've been given
            ArrayDeque<VPNode<T>> stack = new ArrayDeque<VPNode<T>>();
            this.findNodeContainingPoint(node.getCenter(), stack);
            
            // We're an ancestor if we appear anywhere in the path to the given
            // node
            return stack.contains(this);
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
        if(nodeCapacity < 1) {
            throw new IllegalArgumentException("Node capacity must be greater than zero.");
        }
        
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
    
    /**
     * Constructs a new vp-tree that contains (and indexes) all of the points in
     * the given collection and has leaf nodes with the given point capacity.
     * 
     * @param points
     *            the points to use to populate this tree
     * @param nodeCapacity
     *            the largest number of points any leaf node of the tree should
     *            contain
     */
    public VPTree(Collection<E> points, int nodeCapacity) {
        if(nodeCapacity < 1) {
            throw new IllegalArgumentException("Node capacity must be greater than zero.");
        }
        
        this.binSize = nodeCapacity;
        
        if(!points.isEmpty()) {
            @SuppressWarnings("unchecked")
            E[] pointArray = points.toArray((E[])Array.newInstance(points.iterator().next().getClass(), 0));
            
            this.root = new VPNode<E>(pointArray, 0, pointArray.length, this.binSize);
        } else {
            this.root = new VPNode<E>(this.binSize);
        }
    }
    
    /**
     * Returns a reference to this tree's root node. This method is intended for
     * testing purposes only.
     * 
     * @return a reference to this tree's root node
     */
    protected VPNode<E> getRoot() {
        return this.root;
    }
    
    /**
     * Returns the maximum number of points any leaf node of this tree should
     * contain.
     * 
     * @return the maximum number of points any leaf node should contain
     */
    public int getBinSize() {
        return this.binSize;
    }
    
    /**
     * Adds a single point to this vp-tree. Addition of a point executes in
     * O(log n) time in the best case (where n is the number of points in the
     * tree), but may also trigger a node partition that takes additional time.
     * 
     * @param point
     *            the point to add to this tree
     * 
     * @return {@code true} if the tree was modified by the addition of this
     *         point; vp-trees are always modified by adding points, so this
     *         method always returns true
     */
    @Override
    public boolean add(E point) {
        return this.root.add(point);
    }
    
    /**
     * Adds all of the points in the given collection to this vp-tree.
     * 
     * @param points the points to add to this tree
     * 
     * @return {@code true} if the tree was modified by the addition of the
     *         points; vp-trees are always modified by adding points, so this
     *         method always returns true
     */
    @Override
    public boolean addAll(Collection<? extends E> points) {
        return this.root.addAll(points);
    }
    
    /**
     * Removes all points from this vp-tree. Clearing a vp-tree executes in O(1)
     * time.
     */
    @Override
    public void clear() {
        this.root = new VPNode<E>(this.binSize);
    }
    
    /**
     * Tests whether this vp-tree contains the given point. Membership tests
     * execute in O(log n) time, where n is the number of points in the tree.
     * 
     * @param o
     *            the object to test for membership in this tree
     * 
     * @return {@code true} if this tree contains the given point or
     *         {@code false} otherwise
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        try {
            return this.root.contains((E)o);
        } catch(ClassCastException e) {
            return false;
        }
    }
    
    /**
     * Tests whether this vp-tree contains all of the points in the given
     * collection. Group membership tests execute in O(m log n) time, where m is
     * the number of points in the given collection and n is the number of
     * points in the tree.
     * 
     * @param c
     *            the collection of points to test for membership in this tree
     * 
     * @return {@code true} if this tree contains all of the members of the
     *         given collection or {@code false} otherwise
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object o : c) {
            if(!this.contains(o)) { return false; }
        }
        
        return true;
    }
    
    /**
     * Tests whether this tree is empty.
     * 
     * @return {@code true} if this tree contains no points or {@code false}
     *         otherwise
     */
    @Override
    public boolean isEmpty() {
        return this.root.isEmpty();
    }
    
    /**
     * Returns an @{code Iterator} over all of the points contained in this
     * tree. The order of iteration is not defined, and the @{code Iterator}
     * returned by this method does not support the optional @{code remove}
     * method. The behavior of the returned {@code Iterator} is not defined if
     * the tree is modified after the {@code Iterator} is returned.
     * 
     * @return an {@code Iterator} over the points contained in this tree
     */
    @Override
    public Iterator<E> iterator() {
        return new TreeIterator<E>(this.root);
    }
    
    /**
     * Removes a point from this tree.
     * 
     * @param o
     *            the point to remove
     * 
     * @return {@code true} if the tree was modified by removing this point
     *         (i.e. if the point was present in the tree) or {@code false}
     *         otherwise
     */
    @Override
    public boolean remove(Object o) {
        try {
            @SuppressWarnings("unchecked")
            E point = (E)o;
            
            return this.remove(point, false, null);
        } catch(ClassCastException e) {
            // The object we were given wasn't the kind of thing we're storing,
            // so we definitely can't remove it.
            return false;
        }
    }
    
    /**
     * Removes a point from this tree and optionally defers pruning of nodes
     * left empty after the removal of their last point. If pruning is deferred,
     * it is the responsibility of the caller to prune nodes after this method
     * has executed.
     * 
     * @param point
     *            the point to remove
     * @param deferPruning
     *            if {@code true} and the removal of the given point would leave
     *            a node empty, pruning of the empty node is deferred until a
     *            time chosen by the caller; otherwise, empty nodes are pruned
     *            immediately
     * @param nodesToPrune
     *            a @{code Set} to be populated with nodes left empty by the
     *            removal of points; this may be {@code null} if
     *            {@code deferPruning} if {@code false}
     *            
     * @return {@code true} if the tree was modified by removing this point
     *         (i.e. if the point was present in the tree) or {@code false}
     *         otherwise
     */
    protected boolean remove(E point, boolean deferPruning, Set<VPNode<E>> nodesToPrune) {
        ArrayDeque<VPNode<E>> stack = new ArrayDeque<VPNode<E>>();
        this.root.findNodeContainingPoint(point, stack);
        
        VPNode<E> node = stack.pop();
        
        boolean pointRemoved = node.remove(point);
        
        if(node.isEmpty()) {
            if(deferPruning) {
                nodesToPrune.add(node);
            } else {
                this.pruneEmptyNode(node);
            }
        }
        
        return pointRemoved;
    }
    
    /**
     * Removes all of the points in the given collection from this tree.
     * 
     * @param c
     *            the collection of points to remove from this true
     * 
     * @return {@code true} if the tree was modified by removing the given
     *         points (i.e. if any of the points were present in the tree) or
     *         {@code false} otherwise
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean anyChanged = false;
        HashSet<VPNode<E>> nodesToPrune = new HashSet<VPNode<E>>();
        
        for(Object o : c) {
            try {
                @SuppressWarnings("unchecked")
                E point = (E)o;
                
                boolean pointRemoved = this.remove(point, true, nodesToPrune);
                anyChanged = anyChanged || pointRemoved;
            } catch(ClassCastException e) {
                // The object wasn't the kind of point we have in this tree;
                // just keep moving.
            }
        }
        
        // Avoid duplicating work by removing pruning targets that are children
        // of other pruning targets (since they would be implicitly pruned by
        // pruning the parent).
        HashSet<VPNode<E>> nodesToNotPrune = new HashSet<VPNode<E>>();
        
        for(VPNode<E> node : nodesToPrune) {
            for(VPNode<E> potentialAncestor : nodesToPrune) {
                if(potentialAncestor.isAncestorOfNode(node)) {
                    nodesToNotPrune.add(node);
                }
            }
        }
        
        nodesToPrune.removeAll(nodesToNotPrune);
        
        // Now the set of nodes to prune contains only the highest nodes in any
        // branch; prune (and potentially repartition) each of those
        // individually.
        for(VPNode<E> node : nodesToPrune) {
            this.pruneEmptyNode(node);
            
            if(node.isOverloaded()) {
                try {
                    node.partition();
                } catch(PartitionException e) {
                    // These things happen.
                }
            }
        }
        
        return anyChanged;
    }
    
    protected void pruneEmptyNode(VPNode<E> node) {
        // Only spend time working on this if the node is actually empty; it's
        // harmless to call this method on a non-empty node, though.
        if(node.isEmpty() && node != this.root) {
            ArrayDeque<VPNode<E>> stack = new ArrayDeque<VPNode<E>>();
            this.root.findNodeContainingPoint(node.getCenter(), stack);
            
            // Immediately pop the first node off the stack (since we know it's
            // the empty leaf node we were handed as an argument).
            stack.pop();
            
            // Work through the stack until we either have a non-empty parent or
            // we hit the root of the tree.
            while(stack.peek() != null) {
                VPNode<E> parent = stack.pop();
                parent.absorbChildren();
                
                // We're done as soon as we have a non-empty parent.
                if(!parent.isEmpty()) { break; }
            }
        }
    }
    
    /**
     * Retains only the points in this vp-tree that are contained in the
     * specified collection. In other words, removes from this tree all of its
     * points that are not contained in the specified collection.
     * 
     * @param c
     *            collection containing elements to be retained in this tree
     * 
     * @return {@code true} if this tree was modified by calling this method or
     *         {@code false} otherwise
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        Vector<E> pointsToRemove = new Vector<E>();
        
        for(E point : this) {
            if(!c.contains(point)) {
                pointsToRemove.add(point);
            }
        }
        
        return this.removeAll(pointsToRemove);
    }
    
    /**
     * Returns the total number of points stored in this vp-tree.
     * 
     * @return the number of points stored in this vp-tree
     */
    @Override
    public int size() {
        return this.root.size();
    }

    /**
     * Returns an array containing all of the points in this vp-tree. The order
     * of the points in the array is not defined.
     * 
     * @return an array containing all of the points in this vp-tree
     */
    @Override
    public Object[] toArray() {
        Object[] array = new Object[this.size()];
        this.root.addPointsToArray(array);
        
        return array;
    }
    
    /**
     * <p>Returns an array containing all of the points in this vp-tree; the
     * runtime type of the returned array is that of the specified array. If the
     * collection fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this collection.</p>
     * 
     * <p>If all of the points in this tree fit in the specified array with room
     * to spare (i.e., the array has more elements than this vp-tree), the
     * element in the array immediately following the end of the collection is
     * set to {@code null}.</p>
     * 
     * @param a
     *            the array into which the elements of this tree are to be
     *            stored, if it is big enough; otherwise, a new array of the
     *            same runtime type is allocated for this purpose
     * 
     * @return an array containing all of the points in this vp-tree
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        int size = this.size();
        
        if(a.length < this.size()) {
            return (T[])java.util.Arrays.copyOf(this.toArray(), size, a.getClass());
        } else {
            System.arraycopy(this.toArray(), 0, a, 0, size);
            
            if(a.length > size) { a[size] = null; }
            
            return a;
        }
    }

    @Override
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults) {
        SearchResults<E> results = new SearchResults<E>(queryPoint, maxResults);
        this.root.getNearestNeighbors(queryPoint, results);
        
        return results.toSortedList();
    }

    @Override
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults, double maxDistance) {
        SearchResults<E> results = new SearchResults<E>(queryPoint, maxResults, maxDistance);
        this.root.getNearestNeighbors(queryPoint, results);
        
        return results.toSortedList();
    }

    @Override
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults, SearchCriteria<E> searchCriteria) {
        SearchResults<E> results = new SearchResults<E>(queryPoint, maxResults, searchCriteria);
        this.root.getNearestNeighbors(queryPoint, results);
        
        return results.toSortedList();
    }

    @Override
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults, double maxDistance, SearchCriteria<E> searchCriteria) {
        SearchResults<E> results = new SearchResults<E>(queryPoint, maxResults, maxDistance, searchCriteria);
        this.root.getNearestNeighbors(queryPoint, results);
        
        return results.toSortedList();
    }

    @Override
    public List<E> getAllNeighborsWithinDistance(GeospatialPoint queryPoint, double maxDistance) {
        return this.getAllNeighborsWithinDistance(queryPoint, maxDistance, null);
    }

    @Override
    public List<E> getAllNeighborsWithinDistance(GeospatialPoint queryPoint, double maxDistance, SearchCriteria<E> searchCriteria) {
        Vector<E> results = new Vector<E>(this.binSize);
        this.root.getAllWithinRange(new CachingGeospatialPoint(queryPoint), maxDistance, searchCriteria, results);
        
        java.util.Collections.sort(results, new GeospatialDistanceComparator<E>(queryPoint));
        
        return results;
    }

    @Override
    public void movePoint(E point, double latitude, double longitude) {
        this.movePoint(point, new SimpleGeospatialPoint(latitude, longitude));
    }

    @Override
    public void movePoint(E point, GeospatialPoint destination) {
        // Moving points can trigger significant structural changes to a
        // tree. If a point's departure from a node would leave that node
        // empty, its parent needs to gather the nodes from its children and
        // potentially repartition itself. If the point's arrival in a node
        // would push that node over the bin size threshold, the node might
        // need to be partitioned. We want to avoid the case where we'd move
        // the point out of a node, regroup things in the parent, and then
        // put the node right back in the same place, so we do some work in
        // advance to see if the old and new positions would fall into the
        // same tree node.
        ArrayDeque<VPNode<E>> sourcePath = new ArrayDeque<VPNode<E>>();
        ArrayDeque<VPNode<E>> destinationPath = new ArrayDeque<VPNode<E>>();
        
        this.root.findNodeContainingPoint(point, sourcePath);
        this.root.findNodeContainingPoint(destination, destinationPath);
        
        if(sourcePath.equals(destinationPath)) {
            // Easy! We expect no structural changes, so we can modify the
            // point directly and immediately.
            point.setLatitude(destination.getLatitude());
            point.setLongitude(destination.getLongitude());
        } else {
            // We don't know that moving the point will cause structural
            // changes, but we have to assume it will.
            this.remove(point);
            
            point.setLatitude(destination.getLatitude());
            point.setLongitude(destination.getLongitude());
            
            this.add(point);
        }
    }
    
    @Override
    public E getNearestNeighbor(GeospatialPoint queryPoint) {
        SearchResults<E> results = new SearchResults<E>(queryPoint, 1);
        this.root.getNearestNeighbors(queryPoint, results);
        
        return results.peek();
    }

    @Override
    public E getNearestNeighbor(GeospatialPoint queryPoint, double maxDistance) {
        SearchResults<E> results = new SearchResults<E>(queryPoint, 1, maxDistance);
        this.root.getNearestNeighbors(queryPoint, results);
        
        return results.peek();
    }

    @Override
    public E getNearestNeighbor(GeospatialPoint queryPoint, SearchCriteria<E> searchCriteria) {
        SearchResults<E> results = new SearchResults<E>(queryPoint, 1, searchCriteria);
        this.root.getNearestNeighbors(queryPoint, results);
        
        return results.peek();
    }

    @Override
    public E getNearestNeighbor(GeospatialPoint queryPoint, double maxDistance, SearchCriteria<E> searchCriteria) {
        SearchResults<E> results = new SearchResults<E>(queryPoint, 1, maxDistance, searchCriteria);
        this.root.getNearestNeighbors(queryPoint, results);
        
        return results.peek();
    }
}
