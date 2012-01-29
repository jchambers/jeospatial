package com.eatthepath.jeospatial.vptree;

import static org.junit.Assert.*;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eatthepath.jeospatial.SearchCriteria;
import com.eatthepath.jeospatial.util.CachingGeospatialPoint;
import com.eatthepath.jeospatial.util.GeospatialDistanceComparator;
import com.eatthepath.jeospatial.util.SearchResults;
import com.eatthepath.jeospatial.util.SimpleGeospatialPoint;

/**
 * Test suite for the VPNode class.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
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
        
        // New empty nodes should be empty leaf nodes
        assertTrue(node.isLeafNode());
        assertTrue(node.isEmpty());
    }
    
    @Test
    public void testVPNodeTArrayIntIntInt() {
        SimpleGeospatialPoint[] points = VPNodeTest.cities.values().toArray(new SimpleGeospatialPoint[0]);
        
        VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint> node =
                this.tree.new VPNode<SimpleGeospatialPoint>(points, 0, points.length, 5);
        
        // New empty nodes should be empty leaf nodes
        assertFalse(node.isLeafNode());
        assertFalse(node.isEmpty());
    }
    
    @Test
    public void testAddAll() {
        assertTrue(this.testNode.isLeafNode());
        assertTrue(this.testNode.isEmpty());
        
        this.testNode.addAll(VPNodeTest.cities.values());
        
        // Node should be non-empty, non-leaf node with all of the cities in the
        // master list
        assertFalse(this.testNode.isLeafNode());
        assertFalse(this.testNode.isEmpty());
        assertEquals(VPNodeTest.cities.size(), this.testNode.size());
    }
    
    @Test
    public void testAdd() {
        assertTrue(this.testNode.isLeafNode());
        assertTrue(this.testNode.isEmpty());
        
        this.testNode.add(VPNodeTest.cities.get("Boston"));
        
        // Node should be a non-empty leaf node with a single point after adding
        // one point
        assertTrue(this.testNode.isLeafNode());
        assertFalse(this.testNode.isEmpty());
        assertEquals(1, this.testNode.size());
        
        this.testNode.add(VPNodeTest.cities.get("Los Angeles"));
        this.testNode.add(VPNodeTest.cities.get("Dallas"));
        this.testNode.add(VPNodeTest.cities.get("Chicago"));
        this.testNode.add(VPNodeTest.cities.get("Detroit"));
        
        // Node should be a non-empty non-leaf node after adding multiple points
        assertFalse(this.testNode.isLeafNode());
        assertFalse(this.testNode.isEmpty());
        assertEquals(5, this.testNode.size());
    }
    
    @Test
    public void testSize() {
        this.testNode.addAll(VPNodeTest.cities.values());
        assertEquals(VPNodeTest.cities.size(), this.testNode.size());
    }
    
    @Test
    public void testContains() {
        assertFalse(this.testNode.contains(VPNodeTest.cities.get("Boston")));
        this.testNode.addAll(VPNodeTest.cities.values());
        assertTrue(this.testNode.contains(VPNodeTest.cities.get("Boston")));
    }
    
    @Test
    public void testGetPoints() {
        assertTrue(this.testNode.isLeafNode());
        assertNotNull(this.testNode.getPoints());
        assertTrue(this.testNode.getPoints().isEmpty());
        
        this.testNode.add(VPNodeTest.cities.get("Boston"));
        
        // Node should return a non-null, non-empty vector after receiving point
        assertTrue(this.testNode.isLeafNode());
        assertNotNull(this.testNode.getPoints());
        assertFalse(this.testNode.getPoints().isEmpty());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testGetPointsNonLeafNode() {
        this.testNode.addAll(VPNodeTest.cities.values());
        
        assertFalse(this.testNode.isLeafNode());
        
        // Non-leaf node should throw an exception
        this.testNode.getPoints();
    }
    
    @Test(expected = PartitionException.class)
    public void testPartitionNonLeaf() throws PartitionException {
        this.testNode.addAll(VPNodeTest.cities.values());
        
        assertFalse(this.testNode.isLeafNode());
        
        // Non-leaf node should throw an exception
        this.testNode.partition();
    }
    
    @Test(expected = PartitionException.class)
    public void testPartitionEmpty() throws PartitionException {
        assertTrue(this.testNode.isLeafNode());
        assertTrue(this.testNode.isEmpty());
        
        // Empty node should throw an exception
        this.testNode.partition();
    }
    
    @Test(expected = PartitionException.class)
    public void testPartitionSinglePoint() throws PartitionException {
        assertTrue(this.testNode.isLeafNode());
        assertTrue(this.testNode.isEmpty());
        
        this.testNode.add(VPNodeTest.cities.get("Boston"));
        
        // Node containing fewer than two points should throw an exeption
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
        
        // We deferred partitioning when adding points; the root node should
        // still be a leaf, but be over capacity.
        assertTrue(this.testNode.isLeafNode());
        assertFalse(this.testNode.isEmpty());
        assertTrue(this.testNode.isOverloaded());
        assertEquals(VPNodeTest.cities.size(), this.testNode.size());
        
        // We should only have a single node to partition
        assertEquals(1, nodesToPartition.size());
        assertTrue(nodesToPartition.contains(this.testNode));
        
        // Nothing should explode here
        this.testNode.partition();
        
        // After partitioning, the node should no longer be a leaf node, but the
        // tree should still contain all of the original points
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
        
        // Make sure partitioning worked as expected and we didn't include the
        // first element of the array
        assertFalse(this.testNode.isLeafNode());
        assertFalse(this.testNode.isEmpty());
        assertFalse(this.testNode.contains(VPNodeTest.cities.get("Boston")));
        assertTrue(this.testNode.contains(VPNodeTest.cities.get("New York")));
        assertEquals(5, this.testNode.size());
    }
    
    @Test(expected = PartitionException.class)
    public void testPartitionIdenticalPoints() throws PartitionException {
        SimpleGeospatialPoint[] points = new SimpleGeospatialPoint[] {
            new SimpleGeospatialPoint(VPNodeTest.cities.get("Boston")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("Boston")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("Boston")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("Boston")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("Boston")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("Boston"))
        };
        
        // Partitioning should fail if all points are coincident
        this.testNode.partition(points, 0, 6);
    }
    
    @Test
    public void testPartitionThresholdAfterMedian() throws PartitionException {
        SimpleGeospatialPoint[] points = new SimpleGeospatialPoint[] {
            new SimpleGeospatialPoint(VPNodeTest.cities.get("Boston")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("Boston")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("Boston")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("Boston")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("Boston")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("New York"))
        };
        
        this.testNode.partition(points, 0, 6);
        
        // We're cheating a little here; we know the node will choose the first
        // point in the array as its center and 0 as its median distance; it
        // should correctly move the distance threshold out to cut off New York
        // and place it in its own node.
        assertFalse(this.testNode.isLeafNode());
        assertFalse(this.testNode.getCloserNode().isEmpty());
        assertFalse(this.testNode.getFartherNode().isEmpty());
    }
    
    @Test
    public void testPartitionThresholdBeforeMedian() throws PartitionException {
        SimpleGeospatialPoint[] points = new SimpleGeospatialPoint[] {
            new SimpleGeospatialPoint(VPNodeTest.cities.get("Boston")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("New York")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("New York")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("New York")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("New York")),
            new SimpleGeospatialPoint(VPNodeTest.cities.get("New York"))
        };
        
        // Again, we're cheating. We know that the median distance will be at
        // New York, and the node should pull the threshold in until it cuts off
        // Boston into its own node.
        this.testNode.partition(points, 0, 6);
        
        assertFalse(this.testNode.isLeafNode());
        assertFalse(this.testNode.getCloserNode().isEmpty());
        assertFalse(this.testNode.getFartherNode().isEmpty());
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
        
        Vector<SimpleGeospatialPoint> expectedResults =
                new Vector<SimpleGeospatialPoint>(VPNodeTest.cities.values());
        
        java.util.Collections.sort(expectedResults,
                new GeospatialDistanceComparator<SimpleGeospatialPoint>(somerville));
        
        // Make sure we're honoring the limit on the number of results and that
        // they're in the expected order
        assertEquals(3, sortedResults.size());
        assertEquals(expectedResults.subList(0, 3), sortedResults);
        
        results = new SearchResults<SimpleGeospatialPoint>(somerville, 8, 1000 * 1000);
        this.testNode.getNearestNeighbors(somerville, results);
        sortedResults = results.toSortedList();
        
        // Make sure we're only finding points within range even though we're
        // allowed to return more results than will be found; also make sure
        // they're actually the right results.
        assertEquals(3, sortedResults.size());
        assertEquals(expectedResults.subList(0, 3), sortedResults);
        
        SearchCriteria<SimpleGeospatialPoint> criteria = new SearchCriteria<SimpleGeospatialPoint>() {
            @Override
            public boolean matches(SimpleGeospatialPoint point) {
                return VPNodeTest.cities.get("Boston").equals(point);
            }
        };
        
        results = new SearchResults<SimpleGeospatialPoint>(somerville, 8, 1000 * 1000, criteria);
        this.testNode.getNearestNeighbors(somerville, results);
        sortedResults = results.toSortedList();
        
        // Make sure we only find one node and that it's the node we expected.
        assertEquals(1, sortedResults.size());
        assertEquals(VPNodeTest.cities.get("Boston"), sortedResults.get(0));
        
        results = new SearchResults<SimpleGeospatialPoint>(somerville, VPNodeTest.cities.size());
        this.testNode.getNearestNeighbors(somerville, results);
        sortedResults = results.toSortedList();
        
        // Finally, make sure a "get everything" search returns all of the
        // results in the right order
        assertEquals(expectedResults, sortedResults);
    }
    
    @Test
    public void testGetAllWithinRange() {
        this.testNode.addAll(VPNodeTest.cities.values());
        
        CachingGeospatialPoint somerville = new CachingGeospatialPoint(42.387597, -71.099497);
        
        Vector<SimpleGeospatialPoint> results = new Vector<SimpleGeospatialPoint>();
        this.testNode.getAllWithinRange(somerville, 1000 * 1000, null, results);
        
        Vector<SimpleGeospatialPoint> expectedResults =
                new Vector<SimpleGeospatialPoint>(VPNodeTest.cities.values());
        
        java.util.Collections.sort(expectedResults,
                new GeospatialDistanceComparator<SimpleGeospatialPoint>(somerville));
        
        // Make sure we found everything in range
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
        
        // Make sure search criteria are being applied as expected
        assertEquals(1, results.size());
        assertTrue(results.contains(VPNodeTest.cities.get("Boston")));
    }
    
    @Test
    public void testAddPointsToArray() {
        SimpleGeospatialPoint[] points = new SimpleGeospatialPoint[VPNodeTest.cities.size()];
        
        this.testNode.addAll(VPNodeTest.cities.values());
        this.testNode.addPointsToArray(points, 0);
        
        for(int i = 0; i < points.length; i++) {
            assertNotNull(points[i]);
        }
        
        Vector<SimpleGeospatialPoint> pointList = new Vector<SimpleGeospatialPoint>(points.length);
        
        for(int i = 0; i < points.length; i++) {
            pointList.add(points[i]);
        }
        
        for(SimpleGeospatialPoint p : VPNodeTest.cities.values()) {
            assertTrue(pointList.contains(p));
        }
    }
    
    @Test
    public void testFindNodeContainingPoint() {
        this.testNode.addAll(VPNodeTest.cities.values());
        
        ArrayDeque<VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint>> stack =
                new ArrayDeque<VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint>>();
        
        SimpleGeospatialPoint memphis = VPNodeTest.cities.get("Memphis");
        
        this.testNode.findNodeContainingPoint(memphis, stack);
        
        assertTrue(stack.peek().contains(memphis));
    }
    
    @Test
    public void testRemove() {
        this.testNode.add(VPNodeTest.cities.get("Boston"));
        
        assertFalse(this.testNode.isEmpty());
        assertTrue(this.testNode.isLeafNode());
        
        assertTrue(this.testNode.remove(VPNodeTest.cities.get("Boston")));
        
        assertTrue(this.testNode.isEmpty());
        assertTrue(this.testNode.isLeafNode());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testRemoveNonLeafNode() {
        this.testNode.addAll(VPNodeTest.cities.values());
        
        assertFalse(this.testNode.isLeafNode());
        
        // Removing nodes from a non-leaf node should throw an exception
        this.testNode.remove(VPNodeTest.cities.get("Boston"));
    }
    
    @Test
    public void testAbsorbChildren() {
        this.testNode.addAll(VPNodeTest.cities.values());
        
        assertFalse(this.testNode.isLeafNode());
        
        this.testNode.absorbChildren();
        
        assertTrue(this.testNode.isLeafNode());
        assertEquals(VPNodeTest.cities.size(), this.testNode.getPoints().size());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testAbsorbChildrenNonLeafNode() {
        assertTrue(this.testNode.isLeafNode());
        
        // Asking a leaf node to absorb its non-existent children should throw
        // an exception
        this.testNode.absorbChildren();
    }
    
    @Test
    public void testGatherLeafNodes() {
        Vector<VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint>> nodes =
                new Vector<VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint>>();
        
        assertTrue(this.testNode.isLeafNode());
        
        this.testNode.gatherLeafNodes(nodes);
        
        assertEquals(1, nodes.size());
        assertTrue(nodes.contains(this.testNode));
        
        nodes = new Vector<VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint>>();
        
        this.testNode.addAll(VPNodeTest.cities.values());
        
        assertFalse(this.testNode.isLeafNode());
        
        this.testNode.gatherLeafNodes(nodes);
        
        assertFalse(nodes.contains(this.testNode));
        
        for(VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint> node : nodes) {
            assertTrue(node.isLeafNode());
        }
    }
}
