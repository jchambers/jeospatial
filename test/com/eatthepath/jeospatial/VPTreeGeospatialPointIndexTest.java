package com.eatthepath.jeospatial;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class VPTreeGeospatialPointIndexTest {

    @Test
    public void testGetAllPointsInBoundingBox() {
        final List<SimpleGeospatialPoint> points = java.util.Arrays.asList(new SimpleGeospatialPoint[] {
                new SimpleGeospatialPoint(-5, -5),
                new SimpleGeospatialPoint(-4, -4),
                new SimpleGeospatialPoint(-3, -3),
                new SimpleGeospatialPoint(-2, -2),
                new SimpleGeospatialPoint(-1, -1),
                new SimpleGeospatialPoint(0, 0),
                new SimpleGeospatialPoint(1, 1),
                new SimpleGeospatialPoint(2, 2),
                new SimpleGeospatialPoint(3, 3),
                new SimpleGeospatialPoint(4, 4),
                new SimpleGeospatialPoint(5, 5),
                new SimpleGeospatialPoint(-2, 0),
                new SimpleGeospatialPoint(2, 0),
                new SimpleGeospatialPoint(0, -2),
                new SimpleGeospatialPoint(0, 2)
        });

        final VPTreeGeospatialPointIndex<SimpleGeospatialPoint> index =
                new VPTreeGeospatialPointIndex<SimpleGeospatialPoint>(points);

        final List<SimpleGeospatialPoint> pointsInBox = index.getAllPointsInBoundingBox(-2, -2, 2, 2);

        assertEquals(9, pointsInBox.size());

        for (final SimpleGeospatialPoint point : pointsInBox) {
            assertTrue(point.getLatitude() >= -2);
            assertTrue(point.getLatitude() <= 2);
            assertTrue(point.getLongitude() >= -2);
            assertTrue(point.getLongitude() <= 2);
        }
    }
}
