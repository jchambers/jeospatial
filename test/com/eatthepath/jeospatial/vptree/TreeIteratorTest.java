package com.eatthepath.jeospatial.vptree;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import com.eatthepath.jeospatial.SimpleGeospatialPoint;

public class TreeIteratorTest {
    private static final int DEFAULT_BIN_SIZE = 2;
    
    private VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint> testNode;
    private Vector<SimpleGeospatialPoint> cities;
    
    @Before
    public void setUp() throws Exception {
        this.cities = new Vector<SimpleGeospatialPoint>();
        
        this.cities.add(new SimpleGeospatialPoint(42.338947, -70.919635));
        this.cities.add(new SimpleGeospatialPoint(40.780751, -73.977182));
        this.cities.add(new SimpleGeospatialPoint(37.766529, -122.39577));
        this.cities.add(new SimpleGeospatialPoint(34.048411, -118.34015));
        this.cities.add(new SimpleGeospatialPoint(32.787629, -96.79941));
        this.cities.add(new SimpleGeospatialPoint(41.904667, -87.62504));
        this.cities.add(new SimpleGeospatialPoint(35.169255, -89.990415));
        this.cities.add(new SimpleGeospatialPoint(36.145303, -115.18358));
        this.cities.add(new SimpleGeospatialPoint(42.348937, -83.08994));
        
        VPTree<SimpleGeospatialPoint> tree = new VPTree<SimpleGeospatialPoint>();
        this.testNode = tree.new VPNode<SimpleGeospatialPoint>(DEFAULT_BIN_SIZE);
    }
    
    @Test
    public void testHasNext() {
        TreeIterator<SimpleGeospatialPoint> i = new TreeIterator<SimpleGeospatialPoint>(this.testNode);
        assertFalse(i.hasNext());
        
        this.testNode.addAll(this.cities);
        
        i = new TreeIterator<SimpleGeospatialPoint>(this.testNode);
        assertTrue(i.hasNext());
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testNextEmptyIterator() {
        TreeIterator<SimpleGeospatialPoint> i = new TreeIterator<SimpleGeospatialPoint>(this.testNode);
        
        assertFalse(i.hasNext());
        i.next();
    }
    
    @Test
    public void testNext() {
        this.testNode.addAll(this.cities);
        TreeIterator<SimpleGeospatialPoint> i = new TreeIterator<SimpleGeospatialPoint>(this.testNode);
        
        Vector<SimpleGeospatialPoint> returnedPoints = new Vector<SimpleGeospatialPoint>();
        
        while(i.hasNext()) {
            returnedPoints.add(i.next());
        }
        
        assertTrue(returnedPoints.containsAll(this.cities));
        assertEquals(this.cities.size(), returnedPoints.size());
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        TreeIterator<SimpleGeospatialPoint> i = new TreeIterator<SimpleGeospatialPoint>(this.testNode);
        i.remove();
    }
}
