package com.eatthepath.jeospatial;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class BoundingBoxPointFilterTest {

    private static final double SOUTH = 0;
    private static final double WEST = -20;
    private static final double NORTH = 40;
    private static final double EAST = 30;

    private static final BoundingBoxPointFilter BOUNDING_BOX_POINT_FILTER =
            new BoundingBoxPointFilter(SOUTH, WEST, NORTH, EAST);

    @Test
    @Parameters(method = "getParametersForTestAllowPoint")
    public void testAllowPoint(final GeospatialPoint point, final boolean expectAllowed) {
        if (expectAllowed) {
            assertTrue(BOUNDING_BOX_POINT_FILTER.allowPoint(point));
        } else {
            assertFalse(BOUNDING_BOX_POINT_FILTER.allowPoint(point));
        }
    }

    private Object getParametersForTestAllowPoint() {
        return new Object[][] {
                // Southwest corner
                { new SimpleGeospatialPoint(SOUTH, WEST), true },

                // Northwest corner
                { new SimpleGeospatialPoint(NORTH, WEST), true },

                // Northeast corner
                { new SimpleGeospatialPoint(NORTH, EAST), true },

                // Southeast corner
                { new SimpleGeospatialPoint(SOUTH, EAST), true },

                // Middle-ish
                { new SimpleGeospatialPoint(10, -15), true },

                // Too far east
                { new SimpleGeospatialPoint(NORTH, EAST + 1), false },

                // Too far west
                { new SimpleGeospatialPoint(NORTH, WEST - 1), false },

                // Too far north
                { new SimpleGeospatialPoint(NORTH + 1, EAST), false },

                // Too far south
                { new SimpleGeospatialPoint(SOUTH - 1, EAST), false },
        };
    }
}