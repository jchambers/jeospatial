package com.eatthepath.jeospatial.vptree;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

import com.eatthepath.jeospatial.GeospatialIndex;
import com.eatthepath.jeospatial.GeospatialPointDatabaseTest;
import com.eatthepath.jeospatial.util.SimpleGeospatialPoint;

/**
 * Test suite for the VPTree class.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class VPTreeTest extends GeospatialPointDatabaseTest {
    /*
     * (non-Javadoc)
     * @see com.eatthepath.jeospatial.GeospatialPointDatabaseTest#createEmptyDatabase()
     */
    @Override
    public GeospatialIndex<SimpleGeospatialPoint> createEmptyDatabase() {
        return new VPTree<SimpleGeospatialPoint>(2);
    }
    
    @Test
    public void testVPTree() {
        // Just make sure nothing blows up here
        new VPTree<SimpleGeospatialPoint>();
    }
    
    @Test
    public void testVPTreeInt() {
        // Make sure bin size is getting set as expected
        assertEquals(7, new VPTree<SimpleGeospatialPoint>(7).getBinSize());
    }
    
    @Test
    public void testVPTreeCollectionOfE() {
        // Make sure trees created from an existing collection actually contain
        // all of the points in that collection
        VPTree<SimpleGeospatialPoint> tree = new VPTree<SimpleGeospatialPoint>(
                GeospatialPointDatabaseTest.cities.values());
        
        assertTrue(tree.containsAll(GeospatialPointDatabaseTest.cities.values()));
        assertEquals(GeospatialPointDatabaseTest.cities.size(), tree.size());
    }
    
    @Test
    public void testVPTreeCollectionOfEInt() {
        // Make sure trees created from an existing collection actually contain
        // all of the points in that collection and have the expected bin size
        VPTree<SimpleGeospatialPoint> tree = new VPTree<SimpleGeospatialPoint>(
                GeospatialPointDatabaseTest.cities.values(), 7);
        
        assertTrue(tree.containsAll(GeospatialPointDatabaseTest.cities.values()));
        assertEquals(GeospatialPointDatabaseTest.cities.size(), tree.size());
        assertEquals(7, tree.getBinSize());
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
        
        // Figure out which child node (it may be both, but we just need one) to
        // prune
        VPTree<SimpleGeospatialPoint>.VPNode<SimpleGeospatialPoint> nodeToPrune = 
                !tree.getRoot().getCloserNode().isLeafNode() ? tree.getRoot().getCloserNode() : tree.getRoot().getFartherNode();
        
        // The root node should become an empty leaf node after the pruning, but
        // not before
        assertFalse(nodeToPrune.isLeafNode());
        assertTrue(nodeToPrune.isEmpty());
        
        tree.pruneEmptyNode(nodeToPrune);
        
        assertTrue(tree.getRoot().isLeafNode());
        assertTrue(tree.getRoot().isEmpty());
        
        // Now test the case where only one node of a root is empty
        tree = new VPTree<SimpleGeospatialPoint>(1);
        tree.add(GeospatialPointDatabaseTest.cities.get("Boston"));
        tree.add(GeospatialPointDatabaseTest.cities.get("New York"));
        
        // Make sure the tree is in the state we'd expect
        assertFalse(tree.getRoot().isLeafNode());
        assertTrue(tree.getRoot().getCloserNode().isLeafNode());
        assertTrue(tree.getRoot().getFartherNode().isLeafNode());
        assertTrue(tree.remove(GeospatialPointDatabaseTest.cities.get("Boston"), true, nodesToPrune));
        
        // At this point, one of the children should be empty, but not both
        assertTrue(tree.getRoot().getCloserNode().isEmpty() || tree.getRoot().getFartherNode().isEmpty());
        assertFalse(tree.getRoot().getCloserNode().isEmpty() && tree.getRoot().getFartherNode().isEmpty());
        
        // Find the empty child node to prune
        nodeToPrune = tree.getRoot().getCloserNode().isEmpty() ? tree.getRoot().getCloserNode() : tree.getRoot().getFartherNode();
        tree.pruneEmptyNode(nodeToPrune);
        
        // The root node should now be a leaf node that contains the points from
        // the non-empty child
        assertFalse(tree.getRoot().isEmpty());
        assertTrue(tree.getRoot().isLeafNode());
        assertEquals(1, tree.size());
        assertTrue(tree.contains(GeospatialPointDatabaseTest.cities.get("New York")));
    }
}
