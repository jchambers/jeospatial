package com.eatthepath.jeospatial.vptree;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.eatthepath.jeospatial.GeospatialPoint;
import com.eatthepath.jeospatial.GeospatialIndex;
import com.eatthepath.jeospatial.GeospatialPointDatabaseTest;
import com.eatthepath.jeospatial.example.ZipCode;
import com.eatthepath.jeospatial.util.GeospatialDistanceComparator;
import com.eatthepath.jeospatial.util.SimpleGeospatialPoint;

/**
 * Test suite for the {@link LockingVPTree} class.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class LockingVPTreeTest extends GeospatialPointDatabaseTest {
    private static List<ZipCode> unsortedZipCodes;
    private static List<ZipCode> sortedZipCodes;
    
    private static SimpleGeospatialPoint somerville = new SimpleGeospatialPoint(42.387597, -71.099497);
    
    private class PointAdder implements Runnable {
        private final VPTree<ZipCode> tree;
        private final List<ZipCode> points;
        
        public PointAdder(VPTree<ZipCode> tree, List<ZipCode> points) {
            this.tree = tree;
            this.points = points;
        }

        @Override
        public void run() {
            this.tree.addAll(this.points);
        }
    }
    
    private class NeighborGetter implements Runnable {
        private final VPTree<ZipCode> tree;
        private final GeospatialPoint queryPoint;
        private final int maxResults;
        
        private List<ZipCode> nearestNeighbors;
        
        public NeighborGetter(VPTree<ZipCode> tree, GeospatialPoint queryPoint, int maxResults) {
            this.tree = tree;
            this.queryPoint = queryPoint;
            this.maxResults = maxResults;
        }
        
        @Override
        public void run() {
            this.nearestNeighbors = this.tree.getNearestNeighbors(this.queryPoint, this.maxResults);
        }
        
        public List<ZipCode> getNearestNeighbors() {
            return this.nearestNeighbors;
        }
    }
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        GeospatialPointDatabaseTest.setUpBeforeClass();
        
        LockingVPTreeTest.unsortedZipCodes = ZipCode.loadAllFromCsvFile();
        LockingVPTreeTest.sortedZipCodes = ZipCode.loadAllFromCsvFile();
        
        java.util.Collections.sort(LockingVPTreeTest.sortedZipCodes,
                new GeospatialDistanceComparator<SimpleGeospatialPoint>(somerville));
    }
    
    @Override
    public GeospatialIndex<SimpleGeospatialPoint> createEmptyDatabase() {
        return new LockingVPTree<SimpleGeospatialPoint>(2, true);
    }
    
    @Test
    public void testLockingVPTree() {
        // Just make sure nothing explodes here
        new LockingVPTree<SimpleGeospatialPoint>();
    }
    
    @Test
    public void testLockingVPTreeBoolean() {
        // Make sure fairness settings are carrying through as expected
        assertFalse(new LockingVPTree<SimpleGeospatialPoint>(false).isFair());
        assertTrue(new LockingVPTree<SimpleGeospatialPoint>(true).isFair());
    }
    
    @Test
    public void testLockingVPTreeInt() {
        // Make sure bin size settings are carrying through through as expected
        assertEquals(17, new LockingVPTree<SimpleGeospatialPoint>(17).getBinSize());
    }
    
    @Test
    public void testLockingVPTreeIntBoolean() {
        LockingVPTree<SimpleGeospatialPoint> tree = new LockingVPTree<SimpleGeospatialPoint>(17, true);
        assertEquals(17, tree.getBinSize());
        assertTrue(tree.isFair());
        
        tree = new LockingVPTree<SimpleGeospatialPoint>(31, false);
        assertEquals(31, tree.getBinSize());
        assertFalse(tree.isFair());
    }
    
    @Test
    public void testLockingVPTreeCollectionOfE() {
        LockingVPTree<SimpleGeospatialPoint> tree =
                new LockingVPTree<SimpleGeospatialPoint>(GeospatialPointDatabaseTest.cities.values());
        
        // Make sure everything winds up in the tree
        assertEquals(GeospatialPointDatabaseTest.cities.size(), tree.size());
        assertTrue(tree.containsAll(GeospatialPointDatabaseTest.cities.values()));
    }
    
    @Test
    public void testLockingVPTreeCollectionOfEBoolean() {
        LockingVPTree<SimpleGeospatialPoint> tree =
                new LockingVPTree<SimpleGeospatialPoint>(GeospatialPointDatabaseTest.cities.values(), true);
        
        // Make sure everything winds up in the tree and the fairness policy is
        // applied
        assertEquals(GeospatialPointDatabaseTest.cities.size(), tree.size());
        assertTrue(tree.containsAll(GeospatialPointDatabaseTest.cities.values()));
        assertTrue(tree.isFair());
    }
    
    @Test
    public void testLockingVPTreeCollectionOfEInt() {
        LockingVPTree<SimpleGeospatialPoint> tree =
                new LockingVPTree<SimpleGeospatialPoint>(GeospatialPointDatabaseTest.cities.values(), 17);
        
        // Make sure everything winds up in the tree and the node capacity is
        // applied
        assertEquals(GeospatialPointDatabaseTest.cities.size(), tree.size());
        assertTrue(tree.containsAll(GeospatialPointDatabaseTest.cities.values()));
        assertEquals(17, tree.getBinSize());
    }
    
    @Test
    public void testLockingVPTreeCollectionOfEIntBoolean() {
        LockingVPTree<SimpleGeospatialPoint> tree =
                new LockingVPTree<SimpleGeospatialPoint>(GeospatialPointDatabaseTest.cities.values(), 17, true);
        
        // Make sure everything winds up in the tree and the node capacity is
        // applied
        assertEquals(GeospatialPointDatabaseTest.cities.size(), tree.size());
        assertTrue(tree.containsAll(GeospatialPointDatabaseTest.cities.values()));
        assertEquals(17, tree.getBinSize());
        assertTrue(tree.isFair());
    }
    
    @Test
    public void testLocking() throws InterruptedException {
        // Locking should make sure the getter waits until the adding operation
        // is done before getting results.
        LockingVPTree<ZipCode> lockingTree = new LockingVPTree<ZipCode>(true);
        
        NeighborGetter getter = new NeighborGetter(lockingTree, LockingVPTreeTest.somerville, 100);
        
        Thread adderThread = new Thread(new PointAdder(lockingTree, LockingVPTreeTest.unsortedZipCodes));
        Thread getterThread = new Thread(getter);
        
        adderThread.start();
        
        // This is totally cheating, but we're doing it anyhow. We know the
        // adder thread is going to take about two seconds to get through its
        // task, so we wait a few milliseconds here to make sure it gets started
        // (and locks the tree) before starting the getter thread.
        Thread.sleep(100);
        
        getterThread.start();
        
        adderThread.join();
        getterThread.join();
        
        assertEquals(LockingVPTreeTest.sortedZipCodes.subList(0, 100), getter.getNearestNeighbors());
    }
}
