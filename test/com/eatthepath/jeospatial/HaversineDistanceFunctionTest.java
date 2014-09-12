package com.eatthepath.jeospatial;

import static org.junit.Assert.*;

import org.junit.Test;

public class HaversineDistanceFunctionTest {

    @Test
    public void testGetDistance() {
        final HaversineDistanceFunction<SimpleGeospatialPoint> distanceFunction =
                new HaversineDistanceFunction<SimpleGeospatialPoint>();

        final SimpleGeospatialPoint BOS = new SimpleGeospatialPoint(42.3631, -71.0064);
        final SimpleGeospatialPoint LAX = new SimpleGeospatialPoint(33.9425, -118.4072);

        assertEquals("Distance from point to self must be zero.",
                0, distanceFunction.getDistance(BOS, BOS), 0);

        assertEquals("Distance from A to B must be equal to distance from B to A.",
                distanceFunction.getDistance(BOS, LAX), distanceFunction.getDistance(LAX, BOS), 0);

        assertEquals("Distance between BOS and LAX should be within 1km of 4,193km.",
                4193000, distanceFunction.getDistance(BOS, LAX), 1000);

        final SimpleGeospatialPoint a = new SimpleGeospatialPoint(0, 0);
        final SimpleGeospatialPoint b = new SimpleGeospatialPoint(0, 180);

        assertEquals("Distance between diametrically opposed points should be within 1m of 2,001,5086m.",
                20015086, distanceFunction.getDistance(a, b), 1);
    }

}
