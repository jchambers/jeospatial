package com.eatthepath.jeospatial.vptree;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.eatthepath.jeospatial.GeospatialPoint;
import com.eatthepath.jeospatial.SearchCriteria;

/**
 * <p>A {@code LockingVPTree} is a thread-safe subclass of a {@link VPTree}. It
 * uses an internal {@link ReentrantReadWriteLock} to manage concurrent reading
 * and modification of its data points.</p>
 * 
 * <p>All operations that read the contents of this tree acquire the tree's
 * internal read lock, while operations that could modify the tree's contents or
 * structure acquire the tree's internal write lock.</p>
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 * 
 * @see ReentrantReadWriteLock
 */
public class LockingVPTree<E extends GeospatialPoint> extends VPTree<E> {
    private final ReentrantReadWriteLock lock;
    
    /**
     * Constructs a new, empty {@code LockingVPTree} with a default node
     * capacity and locking fairness policy.
     * 
     * @see ReentrantReadWriteLock#ReentrantReadWriteLock(boolean)
     */
    public LockingVPTree() {
        this(false);
    }
    
    /**
     * Constructs a new, empty {@code LockingVPTree} with a default node
     * capacity and the given fairness policy.
     * 
     * @param fair
     *            {@code true} if this tree's lock should use a fair ordering
     *            policy or {@code false} otherwise
     * 
     * @see ReentrantReadWriteLock#ReentrantReadWriteLock(boolean)
     */
    public LockingVPTree(boolean fair) {
        super();
        
        this.lock = new ReentrantReadWriteLock(fair);
    }
    
    /**
     * Constructs a new, empty {@code LockingVPTree} with the given node
     * capacity and a default locking fairness policy.
     * 
     * @param nodeCapacity
     *            the maximum number of points to store in a leaf node of the
     *            tree
     * 
     * @see ReentrantReadWriteLock#ReentrantReadWriteLock(boolean)
     */
    public LockingVPTree(int nodeCapacity) {
        this(nodeCapacity, false);
    }
    
    /**
     * Constructs a new, empty {@code LockingVPTree} with the given node
     * capacity and locking fairness policy.
     * 
     * @param nodeCapacity
     *            the maximum number of points to store in a leaf node of the
     *            tree
     * @param fair
     *            {@code true} if this tree's lock should use a fair ordering
     *            policy or {@code false} otherwise
     * 
     * @see ReentrantReadWriteLock#ReentrantReadWriteLock(boolean)
     */
    public LockingVPTree(int nodeCapacity, boolean fair) {
        super(nodeCapacity);
        
        this.lock = new ReentrantReadWriteLock(fair);
    }
    
    /**
     * Constructs a new {@code LockingVPTree} that contains (and indexes) all of
     * the points in the given collection with a default node capacity and
     * locking fairness policy.
     * 
     * @param points the points to use to populate this tree
     * 
     * @see ReentrantReadWriteLock#ReentrantReadWriteLock(boolean)
     */
    public LockingVPTree(Collection<E> points) {
        this(points, false);
    }
    
    /**
     * Constructs a new {@code LockingVPTree} that contains (and indexes) all of
     * the points in the given collection with a default node capacity and the
     * given locking fairness policy.
     * 
     * @param points
     *            the points to use to populate this tree
     * @param fair
     *            {@code true} if this tree's lock should use a fair ordering
     *            policy or {@code false} otherwise
     * 
     * @see ReentrantReadWriteLock#ReentrantReadWriteLock(boolean)
     */
    public LockingVPTree(Collection<E> points, boolean fair) {
        super(points);
        
        this.lock = new ReentrantReadWriteLock(fair);
    }
    
    /**
     * Constructs a new {@code LockingVPTree} that contains (and indexes) all of
     * the points in the given collection with the given node capacity and a
     * default locking fairness policy.
     * 
     * @param points
     *            the points to use to populate this tree
     * @param nodeCapacity
     *            the maximum number of points to store in a leaf node of the
     *            tree
     * 
     * @see ReentrantReadWriteLock#ReentrantReadWriteLock(boolean)
     */
    public LockingVPTree(Collection<E> points, int nodeCapacity) {
        this(points, nodeCapacity, false);
    }
    
    /**
     * Constructs a new {@code LockingVPTree} that contains (and indexes) all of
     * the points in the given collection with the given node capacity and
     * locking fairness policy.
     * 
     * @param points
     *            the points to use to populate this tree
     * @param nodeCapacity
     *            the maximum number of points to store in a leaf node of the
     *            tree
     * @param fair
     *            {@code true} if this tree's lock should use a fair ordering
     *            policy or {@code false} otherwise
     * 
     * @see ReentrantReadWriteLock#ReentrantReadWriteLock(boolean)
     */
    public LockingVPTree(Collection<E> points, int nodeCapacity, boolean fair) {
        super(points, nodeCapacity);
        
        this.lock = new ReentrantReadWriteLock(fair);
    }
    
    /**
     * Tests whether this tree's internal lock uses a "fair" locking policy.
     * 
     * @return {@code true} if this tree's internal lock uses a "fair" locking
     *         policy or {@code false} otherwise
     * 
     * @see ReentrantReadWriteLock#isFair()
     */
    public boolean isFair() {
        return this.lock.isFair();
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#add(com.eatthepath.jeospatial.GeospatialPoint)
     */
    @Override
    public boolean add(E point) {
        this.lock.writeLock().lock();
        
        try {
            return super.add(point);
        } finally {
            this.lock.writeLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(Collection<? extends E> points) {
        this.lock.writeLock().lock();
        
        try {
            return super.addAll(points);
        } finally {
            this.lock.writeLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#clear()
     */
    @Override
    public void clear() {
        this.lock.writeLock().lock();
        
        try {
            super.clear();
        } finally {
            this.lock.writeLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o) {
        this.lock.readLock().lock();
        
        try {
            return super.contains(o);
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#containsAll(java.util.Collection)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        this.lock.readLock().lock();
        
        try {
            return super.containsAll(c);
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        this.lock.readLock().lock();
        
        try {
            return super.isEmpty();
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#iterator()
     */
    @Override
    public Iterator<E> iterator() {
        this.lock.readLock().lock();
        
        try {
            return super.iterator();
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object o) {
        this.lock.writeLock().lock();
        
        try {
            return super.remove(o);
        } finally {
            this.lock.writeLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        this.lock.writeLock().lock();
        
        try {
            return super.removeAll(c);
        } finally {
            this.lock.writeLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#retainAll(java.util.Collection)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        this.lock.writeLock().lock();
        
        try {
            return super.retainAll(c);
        } finally {
            this.lock.writeLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#size()
     */
    @Override
    public int size() {
        this.lock.readLock().lock();
        
        try {
            return super.size();
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#toArray()
     */
    @Override
    public Object[] toArray() {
        this.lock.readLock().lock();
        
        try {
            return super.toArray();
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#toArray(T[])
     */
    @Override
    public <T> T[] toArray(T[] a) {
        this.lock.readLock().lock();
        
        try {
            return super.toArray(a);
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#getNearestNeighbors(com.eatthepath.jeospatial.GeospatialPoint, int)
     */
    @Override
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults) {
        this.lock.readLock().lock();
        
        try {
            return super.getNearestNeighbors(queryPoint, maxResults);
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#getNearestNeighbors(com.eatthepath.jeospatial.GeospatialPoint, int, double)
     */
    @Override
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults, double maxDistance) {
        this.lock.readLock().lock();
        
        try {
            return super.getNearestNeighbors(queryPoint, maxResults, maxDistance);
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#getNearestNeighbors(com.eatthepath.jeospatial.GeospatialPoint, int, com.eatthepath.jeospatial.SearchCriteria)
     */
    @Override
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults, SearchCriteria<E> searchCriteria) {
        this.lock.readLock().lock();
        
        try {
            return super.getNearestNeighbors(queryPoint, maxResults, searchCriteria);
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#getNearestNeighbors(com.eatthepath.jeospatial.GeospatialPoint, int, double, com.eatthepath.jeospatial.SearchCriteria)
     */
    @Override
    public List<E> getNearestNeighbors(GeospatialPoint queryPoint, int maxResults, double maxDistance, SearchCriteria<E> searchCriteria) {
        this.lock.readLock().lock();
        
        try {
            return super.getNearestNeighbors(queryPoint, maxResults, maxDistance, searchCriteria);
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#getAllNeighborsWithinDistance(com.eatthepath.jeospatial.GeospatialPoint, double)
     */
    @Override
    public List<E> getAllNeighborsWithinDistance(GeospatialPoint queryPoint, double maxDistance) {
        this.lock.readLock().lock();
        
        try {
            return super.getAllNeighborsWithinDistance(queryPoint, maxDistance);
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#getAllNeighborsWithinDistance(com.eatthepath.jeospatial.GeospatialPoint, double, com.eatthepath.jeospatial.SearchCriteria)
     */
    @Override
    public List<E> getAllNeighborsWithinDistance(GeospatialPoint queryPoint, double maxDistance, SearchCriteria<E> searchCriteria) {
        this.lock.readLock().lock();
        
        try {
            return super.getAllNeighborsWithinDistance(queryPoint, maxDistance, searchCriteria);
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#getNearestNeighbor(com.eatthepath.jeospatial.GeospatialPoint)
     */
    @Override
    public E getNearestNeighbor(GeospatialPoint queryPoint) {
        this.lock.readLock().lock();
        
        try {
            return super.getNearestNeighbor(queryPoint);
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#getNearestNeighbor(com.eatthepath.jeospatial.GeospatialPoint, double)
     */
    @Override
    public E getNearestNeighbor(GeospatialPoint queryPoint, double maxDistance) {
        this.lock.readLock().lock();
        
        try {
            return super.getNearestNeighbor(queryPoint, maxDistance);
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#getNearestNeighbor(com.eatthepath.jeospatial.GeospatialPoint, com.eatthepath.jeospatial.SearchCriteria)
     */
    @Override
    public E getNearestNeighbor(GeospatialPoint queryPoint, SearchCriteria<E> searchCriteria) {
        this.lock.readLock().lock();
        
        try {
            return super.getNearestNeighbor(queryPoint, searchCriteria);
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.vptree.VPTree#getNearestNeighbor(com.eatthepath.jeospatial.GeospatialPoint, double, com.eatthepath.jeospatial.SearchCriteria)
     */
    @Override
    public E getNearestNeighbor(GeospatialPoint queryPoint, double maxDistance, SearchCriteria<E> searchCriteria) {
        this.lock.readLock().lock();
        
        try {
            return super.getNearestNeighbor(queryPoint, maxDistance, searchCriteria);
        } finally {
            this.lock.readLock().unlock();
        }
    }
}
