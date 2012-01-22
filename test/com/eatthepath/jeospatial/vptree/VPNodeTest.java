package com.eatthepath.jeospatial.vptree;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eatthepath.jeospatial.CachingGeospatialPoint;
import com.eatthepath.jeospatial.SearchCriteria;
import com.eatthepath.jeospatial.SimpleGeospatialPoint;
import com.eatthepath.jeospatial.util.SearchResults;

public class VPNodeTest {
    private static final int DEFAULT_BIN_SIZE = 2;
    
    private static Hashtable<String, SimpleGeospatialPoint> cities;
    
    private VPTree<SimpleGeospatialPoint> tree;
    private VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint> testNode;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        VPNodeTest.cities = new Hashtable<String, SimpleGeospatialPoint>();
        
        cities.put("Boston", new SimpleGeospatialPoint(42.338947, -70.919635));
        cities.put("New York", new SimpleGeospatialPoint(40.780751, -73.977182));
        cities.put("San Francisco", new SimpleGeospatialPoint(37.766529, -122.39577));
        cities.put("Los Angeles", new SimpleGeospatialPoint(34.048411, -118.34015));
        cities.put("Dallas", new SimpleGeospatialPoint(32.787629, -96.79941));
        cities.put("Chicago", new SimpleGeospatialPoint(41.904667, -87.62504));
        cities.put("Memphis", new SimpleGeospatialPoint(35.169255, -89.990415));
        cities.put("Las Vegas", new SimpleGeospatialPoint(36.145303, -115.18358));
        cities.put("Detroit", new SimpleGeospatialPoint(42.348937, -83.08994));
    }
    
    @Before
    public void setUp() throws Exception {
        this.tree = new VPTree<SimpleGeospatialPoint>();
        this.testNode = this.tree.new VPNode<SimpleGeospatialPoint>(DEFAULT_BIN_SIZE);
    }
    
    @Test
    public void testVPNodeInt() {
        VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint> node =
                this.tree.new VPNode<SimpleGeospatialPoint>(DEFAULT_BIN_SIZE);
        
        assertTrue(node.isLeafNode());
        assertTrue(node.isEmpty());
    }
    
    @Test
    public void testVPNodeTArrayIntIntInt() {
        SimpleGeospatialPoint[] points = VPNodeTest.cities.values().toArray(new SimpleGeospatialPoint[0]);
        
        VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint> node =
                this.tree.new VPNode<SimpleGeospatialPoint>(points, 0, points.length, 5);
        
        assertFalse(node.isLeafNode());
        assertFalse(node.isEmpty());
    }
    
    @Test
    public void testAddAll() {
        assertTrue(this.testNode.isLeafNode());
        assertTrue(this.testNode.isEmpty());
        
        this.testNode.addAll(VPNodeTest.cities.values());
        
        assertFalse(this.testNode.isLeafNode());
        assertFalse(this.testNode.isEmpty());
        
        assertEquals(VPNodeTest.cities.size(), this.testNode.size());
    }
    
    @Test
    public void testAdd() {
        assertTrue(this.testNode.isLeafNode());
        assertTrue(this.testNode.isEmpty());
        
        this.testNode.add(VPNodeTest.cities.get("Boston"));
        
        assertTrue(this.testNode.isLeafNode());
        assertFalse(this.testNode.isEmpty());
        assertEquals(1, this.testNode.size());
        
        this.testNode.add(VPNodeTest.cities.get("Los Angeles"));
        this.testNode.add(VPNodeTest.cities.get("Dallas"));
        this.testNode.add(VPNodeTest.cities.get("Chicago"));
        this.testNode.add(VPNodeTest.cities.get("Detroit"));
        
        assertFalse(this.testNode.isLeafNode());
        assertFalse(this.testNode.isEmpty());
        assertEquals(5, this.testNode.size());
    }
    
    @Test
    public void testContains() {
        assertTrue(this.testNode.isEmpty());
        assertFalse(this.testNode.contains(VPNodeTest.cities.get("Boston")));
        
        this.testNode.addAll(VPNodeTest.cities.values());
        
        assertTrue(this.testNode.contains(VPNodeTest.cities.get("Boston")));
    }
    
    @Test(expected = PartitionException.class)
    public void testPartitionNonLeaf() throws PartitionException {
        this.testNode.addAll(VPNodeTest.cities.values());
        
        assertFalse(this.testNode.isLeafNode());
        
        this.testNode.partition();
    }
    
    @Test
    public void testPartitionEmpty() throws PartitionException {
        assertTrue(this.testNode.isLeafNode());
        assertTrue(this.testNode.isEmpty());
        
        this.testNode.partition();
    }
    
    @Test
    public void testPartition() throws PartitionException {
        assertTrue(this.testNode.isLeafNode());
        assertTrue(this.testNode.isEmpty());
        
        HashSet<VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint>> nodesToPartition =
                new HashSet<VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint>>();
        
        for(SimpleGeospatialPoint city : VPNodeTest.cities.values()) {
            this.testNode.add(city, true, nodesToPartition);
        }
        
        assertTrue(this.testNode.isLeafNode());
        assertFalse(this.testNode.isEmpty());
        assertEquals(VPNodeTest.cities.size(), this.testNode.size());
        
        assertEquals(1, nodesToPartition.size());
        assertTrue(nodesToPartition.contains(this.testNode));
        
        this.testNode.partition();
        
        assertFalse(this.testNode.isLeafNode());
        assertFalse(this.testNode.isEmpty());
        assertEquals(VPNodeTest.cities.size(), this.testNode.size());
    }
    
    @Test
    public void testPartitionTArrayIntInt() throws PartitionException {
        SimpleGeospatialPoint[] points = new SimpleGeospatialPoint[] {
            VPNodeTest.cities.get("Boston"),
            VPNodeTest.cities.get("New York"),
            VPNodeTest.cities.get("Los Angeles"),
            VPNodeTest.cities.get("Memphis"),
            VPNodeTest.cities.get("San Francisco"),
            VPNodeTest.cities.get("Detroit")
        };
        
        assertTrue(this.testNode.isLeafNode());
        assertTrue(this.testNode.isEmpty());
        
        this.testNode.partition(points, 1, 6);
        
        assertFalse(this.testNode.isLeafNode());
        assertFalse(this.testNode.isEmpty());
        assertFalse(this.testNode.contains(VPNodeTest.cities.get("Boston")));
        assertTrue(this.testNode.contains(VPNodeTest.cities.get("New York")));
        assertEquals(5, this.testNode.size());
    }
    
    @Test
    public void testIsLeafNodeIsEmpty() {
        VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint> node =
                this.tree.new VPNode<SimpleGeospatialPoint>(1);
        
        assertTrue(node.isLeafNode());
        assertTrue(node.isEmpty());
        
        node.add(VPNodeTest.cities.get("Boston"));
        
        assertTrue(node.isLeafNode());
        assertFalse(node.isEmpty());
        
        node.add(VPNodeTest.cities.get("San Francisco"));
        
        assertFalse(node.isLeafNode());
        assertFalse(node.isEmpty());
    }
    
    @Test
    public void testGetNearestNeighbors() {
        this.testNode.addAll(VPNodeTest.cities.values());
        
        CachingGeospatialPoint somerville = new CachingGeospatialPoint(42.387597, -71.099497);
        
        SearchResults<SimpleGeospatialPoint> results = new SearchResults<SimpleGeospatialPoint>(somerville, 3);
        this.testNode.getNearestNeighbors(somerville, results);
        List<SimpleGeospatialPoint> sortedResults = results.toSortedList();
        
        for(SimpleGeospatialPoint p : sortedResults) {
            System.out.println(p);
        }
        
        assertEquals(3, sortedResults.size());
        assertEquals(VPNodeTest.cities.get("Boston"), sortedResults.get(0));
        assertEquals(VPNodeTest.cities.get("New York"), sortedResults.get(1));
        assertEquals(VPNodeTest.cities.get("Detroit"), sortedResults.get(2));
        
        results = new SearchResults<SimpleGeospatialPoint>(somerville, 8, 1000 * 1000);
        this.testNode.getNearestNeighbors(somerville, results);
        sortedResults = results.toSortedList();
        
        assertEquals(3, sortedResults.size());
        assertEquals(VPNodeTest.cities.get("Boston"), sortedResults.get(0));
        assertEquals(VPNodeTest.cities.get("New York"), sortedResults.get(1));
        assertEquals(VPNodeTest.cities.get("Detroit"), sortedResults.get(2));
        
        SearchCriteria<SimpleGeospatialPoint> criteria = new SearchCriteria<SimpleGeospatialPoint>() {
            @Override
            public boolean matches(SimpleGeospatialPoint point) {
                return VPNodeTest.cities.get("Boston").equals(point);
            }
        };
        
        results = new SearchResults<SimpleGeospatialPoint>(somerville, 8, 1000 * 1000, criteria);
        this.testNode.getNearestNeighbors(somerville, results);
        sortedResults = results.toSortedList();
        
        assertEquals(1, sortedResults.size());
        assertEquals(VPNodeTest.cities.get("Boston"), sortedResults.get(0));
    }
    
    @Test
    public void testGetAllWithinRange() {
        this.testNode.addAll(VPNodeTest.cities.values());
        
        CachingGeospatialPoint somerville = new CachingGeospatialPoint(42.387597, -71.099497);
        
        Vector<SimpleGeospatialPoint> results = new Vector<SimpleGeospatialPoint>();
        this.testNode.getAllWithinRange(somerville, 1000 * 1000, null, results);
        
        assertEquals(3, results.size());
        assertTrue(results.contains(VPNodeTest.cities.get("Boston")));
        assertTrue(results.contains(VPNodeTest.cities.get("New York")));
        assertTrue(results.contains(VPNodeTest.cities.get("Detroit")));
        
        SearchCriteria<SimpleGeospatialPoint> criteria = new SearchCriteria<SimpleGeospatialPoint>() {
            @Override
            public boolean matches(SimpleGeospatialPoint point) {
                return VPNodeTest.cities.get("Boston").equals(point);
            }
        };
        
        results = new Vector<SimpleGeospatialPoint>();
        this.testNode.getAllWithinRange(somerville, 1000 * 1000, criteria, results);
        
        assertEquals(1, results.size());
        assertTrue(results.contains(VPNodeTest.cities.get("Boston")));
    }
}
