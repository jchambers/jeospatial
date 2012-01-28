package com.eatthepath.jeospatial.vptree;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

import com.eatthepath.jeospatial.GeospatialPointDatabase;
import com.eatthepath.jeospatial.GeospatialPointDatabaseTest;
import com.eatthepath.jeospatial.SimpleGeospatialPoint;

public class VPTreeTest extends GeospatialPointDatabaseTest {
    @Override
    public GeospatialPointDatabase<SimpleGeospatialPoint> getDatabase() {
        return new VPTree<SimpleGeospatialPoint>();
    }
    
    @Test
    public void testVPTree() {
        new VPTree<SimpleGeospatialPoint>();
    }
    
    @Test
    public void testVPTreeInt() {
        assertEquals(7, new VPTree<SimpleGeospatialPoint>(7).getBinSize());
    }
    
    @Test
    public void testVPTreeCollectionOfE() {
        VPTree<SimpleGeospatialPoint> tree = new VPTree<SimpleGeospatialPoint>(
                GeospatialPointDatabaseTest.cities.values());
        
        assertTrue(tree.containsAll(GeospatialPointDatabaseTest.cities.values()));
        assertEquals(GeospatialPointDatabaseTest.cities.size(), tree.size());
    }
    
    @Test
    public void testVPTreeCollectionOfEInt() {
        VPTree<SimpleGeospatialPoint> tree = new VPTree<SimpleGeospatialPoint>(
                GeospatialPointDatabaseTest.cities.values(), 7);
        
        assertTrue(tree.containsAll(GeospatialPointDatabaseTest.cities.values()));
        assertEquals(GeospatialPointDatabaseTest.cities.size(), tree.size());
        assertEquals(7, tree.getBinSize());
    }
    
    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAll() {
        this.getDatabase().retainAll(GeospatialPointDatabaseTest.cities.values());
    }

    @Test
    public void testPruneEmptyNode() {
        VPTree<SimpleGeospatialPoint> tree = new VPTree<SimpleGeospatialPoint>(
                GeospatialPointDatabaseTest.cities.values(), 1);
        
        // We know at this point that, based on the number of points added and
        // the bin size of the tree, we have at least one child of the root that
        // has its own children. Remove all of the points from this tree, but
        // defer pruning so we can observe and test the pruning process.
        HashSet<VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint>> nodesToPrune =
                new HashSet<VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint>>();
        
        for(SimpleGeospatialPoint p : GeospatialPointDatabaseTest.cities.values()) {
            assertTrue(tree.remove(p, true, nodesToPrune));
        }
        
        VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint> nodeToPrune = 
                !tree.getRoot().getCloserNode().isLeafNode() ? tree.getRoot().getCloserNode() : tree.getRoot().getFartherNode();
        
        assertFalse(nodeToPrune.isLeafNode());
        assertTrue(nodeToPrune.isEmpty());
        
        tree.pruneEmptyNode(nodeToPrune);
        
        assertTrue(tree.getRoot().isLeafNode());
        assertTrue(tree.getRoot().isEmpty());
        
        // Now test the case where only one node of a root is empty
        tree = new VPTree<SimpleGeospatialPoint>(1);
        tree.add(GeospatialPointDatabaseTest.cities.get("Boston"));
        tree.add(GeospatialPointDatabaseTest.cities.get("New York"));
        
        assertFalse(tree.getRoot().isLeafNode());
        assertTrue(tree.getRoot().getCloserNode().isLeafNode());
        assertTrue(tree.getRoot().getFartherNode().isLeafNode());
        assertTrue(tree.remove(GeospatialPointDatabaseTest.cities.get("Boston"), true, nodesToPrune));
        
        // At this point, one of the children should be empty, but not both
        assertTrue(tree.getRoot().getCloserNode().isEmpty() || tree.getRoot().getFartherNode().isEmpty());
        assertFalse(tree.getRoot().getCloserNode().isEmpty() && tree.getRoot().getFartherNode().isEmpty());
        
        nodeToPrune = tree.getRoot().getCloserNode().isEmpty() ? tree.getRoot().getCloserNode() : tree.getRoot().getFartherNode();
        tree.pruneEmptyNode(nodeToPrune);
        
        assertFalse(tree.getRoot().isEmpty());
        assertTrue(tree.getRoot().isLeafNode());
        assertEquals(1, tree.size());
        assertTrue(tree.contains(GeospatialPointDatabaseTest.cities.get("New York")));
    }
}
