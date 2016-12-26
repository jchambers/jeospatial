package com.eatthepath.jeospatial.example;

import com.eatthepath.jeospatial.GeospatialPoint;


/**
 * <p>A {@code ZipCode} represents a point at the approximate center of a United
 * States zip code. In addition to the properties of its {@code GeospatialPoint}
 * superclass, a {@code ZipCode} also contains the numeric five-digit zip code
 * (as an integer that may be missing leading zeroes) of the region, as well as
 * the name of the city in state in which the zip code is located.</p>
 *
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class ZipCode implements GeospatialPoint {
    public static final String DEFAULT_DATA_FILE = "data/zips.csv";

    private final int code;
    private final String city;
    private final String state;

    private final double latitude;
    private final double longitude;

    /**
     * Constructs a new {@code ZipCode} with the given numeric zip code, city
     * name, state abbreviation, latitude, and longitude.
     *
     * @param code
     *            the five-digit numeric zip code for the region
     * @param city
     *            the name of the city in which this zip code is located
     * @param state
     *            the two-letter abbreviation of the state in which this zip
     *            code is located
     * @param latitude
     *            the latitude of the approximate center of this region
     * @param longitude
     *            the longitude of the approximate center of this region
     */
    public ZipCode(int code, String city, String state, double latitude, double longitude) {
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

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ZipCode [code=" + String.format("%05d", code) + ", city=" + city + ", state="
                + state + "]";
    }
}
