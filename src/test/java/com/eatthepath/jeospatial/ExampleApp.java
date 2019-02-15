package com.eatthepath.jeospatial;

import java.util.List;

import com.eatthepath.jeospatial.GeospatialIndex;
import com.eatthepath.jeospatial.VPTreeGeospatialIndex;

/**
 * A very simple test application that shows the very basics of using a
 * geospatial point database.
 *
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class ExampleApp {
    private static class ZipCode implements GeospatialPoint {
        private final int code;
        private final String city;
        private final String state;

        private final double latitude;
        private final double longitude;

        /**
         * Constructs a new {@code ZipCode} with the given numeric zip code, city name, state abbreviation, latitude, and
         * longitude.
         *
         * @param code the five-digit numeric zip code for the region
         * @param city the name of the city in which this zip code is located
         * @param state the two-letter abbreviation of the state in which this zip code is located
         * @param latitude the latitude of the approximate center of this region
         * @param longitude the longitude of the approximate center of this region
         */
        public ZipCode(final int code, final String city, final String state, final double latitude, final double longitude) {
            this.code = code;
            this.city = city;
            this.state = state;

            this.latitude = latitude;
            this.longitude = longitude;
        }

        /**
         * Returns the numeric five-digit code associated with this region.
         *
         * @return the numeric zip code of this region
         */
        public int getCode() {
            return this.code;
        }

        /**
         * Returns the name of the city in which this zip code is located.
         *
         * @return the name of the city in which this zip code is located
         */
        public String getCity() {
            return this.city;
        }

        /**
         * Returns the two-letter abbreviation of the state in which this zip code
         * is located.
         *
         * @return the two-letter abbreviation of the state in which this zip code
         *         is located
         */
        public String getState() {
            return this.state;
        }

        @Override
        public double getLatitude() {
            return this.latitude;
        }

        @Override
        public double getLongitude() {
            return this.longitude;
        }
    }

    private static final GeospatialPoint DAVIS_SQUARE =
            new SimpleGeospatialPoint(42.396745, -71.122479);

    @SuppressWarnings("unused")
    public static void getTenClosestNeighbors(final List<ZipCode> zipCodes) {
        final GeospatialIndex<ZipCode> index = new VPTreeGeospatialIndex<>(zipCodes);

        // Find the ten nearest zip codes to Davis Square
        final List<ZipCode> nearestZipCodes = index.getNearestNeighbors(DAVIS_SQUARE, 10);
    }

    @SuppressWarnings("unused")
    public static void getAllWithinRange(final List<ZipCode> zipCodes) {
        final GeospatialIndex<ZipCode> index = new VPTreeGeospatialIndex<>(zipCodes);

        // Find all zip codes within ten kilometers of Davis Square
        final List<ZipCode> zipCodesWithinRange = index.getAllWithinDistance(DAVIS_SQUARE, 10e3);
    }

    @SuppressWarnings("unused")
    public static void getAllInBoundingBox(final List<ZipCode> zipCodes) {
        final GeospatialIndex<ZipCode> index = new VPTreeGeospatialIndex<>(zipCodes);

        // Find all of the zip codes in a bounding "box"
        final List<ZipCode> inBoundingBox = index.getAllPointsInBoundingBox(-75, -70, 43, 42);
    }
}
