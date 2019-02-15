package com.eatthepath.jeospatial;

import com.eatthepath.jvptree.PointFilter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VPTreeGeospatialIndexTest {

    private static final double SOUTH = 0;
    private static final double WEST = -20;
    private static final double NORTH = 40;
    private static final double EAST = 30;

    private static class PossiblyFancyGeospatialPoint extends SimpleGeospatialPoint {

        private final boolean fancy;

        public PossiblyFancyGeospatialPoint(final double latitude, final double longitude, final boolean fancy) {
            super(latitude, longitude);

            this.fancy = fancy;
        }

        public boolean isFancy() {
            return fancy;
        }
    }

    @Test
    public void testGetAllPointsInBoundingBox() {
        final List<PossiblyFancyGeospatialPoint> pointsInBox = Arrays.asList(
                new PossiblyFancyGeospatialPoint(SOUTH, WEST, true),
                new PossiblyFancyGeospatialPoint(NORTH, WEST, true),
                new PossiblyFancyGeospatialPoint(NORTH, EAST, false),
                new PossiblyFancyGeospatialPoint(SOUTH, EAST, false));

        final List<PossiblyFancyGeospatialPoint> fancyPointsInBox = Arrays.asList(
                pointsInBox.get(0),
                pointsInBox.get(1));

        final List<PossiblyFancyGeospatialPoint> pointsOutsideOfBox = Arrays.asList(
                new PossiblyFancyGeospatialPoint(SOUTH - 1, WEST - 1, true),
                new PossiblyFancyGeospatialPoint(NORTH + 1, WEST - 1, false),
                new PossiblyFancyGeospatialPoint(NORTH + 1, EAST + 1, false),
                new PossiblyFancyGeospatialPoint(SOUTH - 1, EAST + 1, true));

        final VPTreeGeospatialIndex<PossiblyFancyGeospatialPoint> vpTree = new VPTreeGeospatialIndex<>();
        vpTree.addAll(pointsInBox);
        vpTree.addAll(pointsOutsideOfBox);

        {
            final List<PossiblyFancyGeospatialPoint> pointsFromQuery =
                    vpTree.getAllPointsInBoundingBox(SOUTH, WEST, NORTH, EAST);

            assertEquals(pointsInBox.size(), pointsFromQuery.size());
            assertTrue(pointsFromQuery.containsAll(pointsInBox));
        }

        {
            final PointFilter<PossiblyFancyGeospatialPoint> fancyFilter = new PointFilter<PossiblyFancyGeospatialPoint>() {

                @Override
                public boolean allowPoint(final PossiblyFancyGeospatialPoint point) {
                    return point.isFancy();
                }
            };

            final List<PossiblyFancyGeospatialPoint> pointsFromQuery =
                    vpTree.getAllPointsInBoundingBox(SOUTH, WEST, NORTH, EAST, fancyFilter);

            assertEquals(fancyPointsInBox.size(), pointsFromQuery.size());
            assertTrue(pointsFromQuery.containsAll(fancyPointsInBox));
        }
    }
}