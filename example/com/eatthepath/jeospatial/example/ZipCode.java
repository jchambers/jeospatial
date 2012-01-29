package com.eatthepath.jeospatial.example;

import com.eatthepath.jeospatial.SimpleGeospatialPoint;

public class ZipCode extends SimpleGeospatialPoint {
    private final String zipCode;
    private final String city;
    private final String state;
    
    public ZipCode(String zipCode, String city, String state, double latitude, double longitude) {
        super(latitude, longitude);
        
        this.zipCode = zipCode;
        this.city = city;
        this.state = state;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ZipCode [zipCode=" + zipCode + ", city=" + city + ", state="
                + state + "]";
    }
}
