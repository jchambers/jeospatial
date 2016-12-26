package com.eatthepath.jeospatial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.eatthepath.jeospatial.example.ZipCode;
import com.eatthepath.jeospatial.example.ZipCodeLoader;
import com.eatthepath.jvptree.DistanceComparator;

/**
 * A crude test app that loads a bunch of zip codes, builds a vp-tree and
 * performs some searches. The point here is to use a dataset that's larger
 * than would be practical to use during normal unit tests.
 *
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class LoadTestApp {
    public static void main(String[] args) throws IOException {

        final HaversineDistanceFunction distanceFunction = new HaversineDistanceFunction();

        long start = System.currentTimeMillis();
        final List<ZipCode> zipCodes = ZipCodeLoader.loadAllZipCodes();
        long end = System.currentTimeMillis();

        System.out.format("Loaded %d zip codes in %d milliseconds.%n", zipCodes.size(), end - start);

        // Now put all of those zip codes into a vp-tree
        start = System.currentTimeMillis();

        final VPTreeGeospatialIndex<ZipCode> zipCodeTree =
                new VPTreeGeospatialIndex<ZipCode>(zipCodes);

        end = System.currentTimeMillis();

        System.out.format("Built vp-tree from zip code list in %d milliseconds.%n", end - start);

        // Now let's compare search performance! We'll start by picking a query
        // point which, for lack of a better idea, will be where I live.
        ZipCode davisSquare = null;

        for (final ZipCode z : zipCodes) {
            if (z.getCode() == 2144) {
                davisSquare = z;
                break;
            }
        }

        // Dumb nearest neighbor search method: sort the entire list of zip codes!
        start = System.currentTimeMillis();
        java.util.Collections.sort(zipCodes, new DistanceComparator<ZipCode>(davisSquare, distanceFunction));
        end = System.currentTimeMillis();

        System.out.format("Sorted list of zip codes by distance from Ana's Tacqueria in %d milliseconds.%n", end - start);
        System.out.println("Ten closest zip codes by list sort approach:");

        for(int i = 0; i < 10; i++) {
            final ZipCode z = zipCodes.get(i);
            System.out.format("\t%.1f meters - %s%n", distanceFunction.getDistance(davisSquare, z), z);
        }

        // Now let's do OVER NINE THOUSAND searches using the vp-tree. We
        // pre-generate random test points to remove random number generation
        // from the time measurement.
        final ZipCode[] testPoints = new ZipCode[10000];
        final Random r = new Random();

        for(int i = 0; i < testPoints.length; i++) {
            double longitude = -70d - (r.nextDouble() * 50d);
            double latitude = 28d + (r.nextDouble() * 14d);

            testPoints[i] = new ZipCode(0, "Test", "Test", latitude, longitude);
        }

        start = System.currentTimeMillis();

        for (final ZipCode p : testPoints) {
            zipCodeTree.getNearestNeighbors(p, 10);
        }

        end = System.currentTimeMillis();

        System.out.format("Performed %d vp-tree searches in %d milliseconds.%n", testPoints.length, end - start);
        System.out.println("Ten closest zip codes by vp-tree search approach:");

        for(final ZipCode z : zipCodeTree.getNearestNeighbors(davisSquare, 10)) {
            System.out.format("\t%.1f meters - %s%n", distanceFunction.getDistance(davisSquare, z), z);
        }

        // Let's see how performance compares with a thread-safe locking tree.
        /* LockingVPTree<ZipCode> lockingZipCodeTree = new LockingVPTree<ZipCode>(zipCodes, 20);

        start = System.currentTimeMillis();

        for(SimpleGeospatialPoint p : testPoints) {
            lockingZipCodeTree.getNearestNeighbors(p, 10);
        }

        end = System.currentTimeMillis();

        System.out.format("Performed %d locking vp-tree searches in %d milliseconds.%n", testPoints.length, end - start); */

        // Just to be difficult, let's remove all of the zip codes from states
        // in the northeast and try the search again.
        ArrayList<String> statesToClobber = new ArrayList<String>();
        statesToClobber.add("MA");
        statesToClobber.add("ME");
        statesToClobber.add("NH");
        statesToClobber.add("VT");
        statesToClobber.add("RI");
        statesToClobber.add("NY");
        statesToClobber.add("CT");

        ArrayList<ZipCode> zipCodesToRemove = new ArrayList<ZipCode>();

        for(ZipCode z : zipCodes) {
            if(statesToClobber.contains(z.getState())) {
                zipCodesToRemove.add(z);
            }
        }

        start = System.currentTimeMillis();
        zipCodeTree.removeAll(zipCodesToRemove);
        end = System.currentTimeMillis();

        System.out.format("Removed %d zip codes in %d milliseconds.%n", zipCodesToRemove.size(), end - start);

        List<ZipCode> closestAfterRemoval = zipCodeTree.getNearestNeighbors(davisSquare, 10);

        System.out.println("Ten closest zip codes after removal of points in the northeast:");

        for(ZipCode z : closestAfterRemoval) {
            System.out.format("\t%.1f meters - %s%n", distanceFunction.getDistance(davisSquare, z), z);
        }
    }
}
