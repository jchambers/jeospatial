package com.eatthepath.jeospatial.example;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;

import com.eatthepath.jeospatial.SimpleGeospatialPoint;

/**
 * <p>A {@code ZipCode} represents a point at the approximate center of a United
 * States zip code. In addition to the properties of its {@code GeospatialPoint}
 * superclass, a {@code ZipCode} also contains the numeric five-digit zip code
 * (as an integer that may be missing leading zeroes) of the region, as well as
 * the name of the city in state in which the zip code is located.</p>
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class ZipCode extends SimpleGeospatialPoint {
    public static final String DEFAULT_DATA_FILE = "data/zips.csv";
    
    private final int code;
    private final String city;
    private final String state;
    
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
        super(latitude, longitude);
        
        this.code = code;
        this.city = city;
        this.state = state;
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
    
    /**
     * Loads all zip codes from a default CSV file ({@value DEFAULT_DATA_FILE}).
     * 
     * @return a list of all zip codes contained in the default data file
     * 
     * @throws IOException
     *             in the event of any kind of error in loading zip codes from
     *             the data file
     */
    public static List<ZipCode> loadAllFromCsvFile() throws IOException {
        return ZipCode.loadAllFromCsvFile("data/zips.csv");
    }
    
    /**
     * Loads all zip codes from the file at the given path.
     * 
     * @param path the path to the file containing zip code data
     * 
     * @return a list of all zip codes contained in the file at the given path
     * 
     * @throws IOException
     *             in the event of any kind of error in loading zip codes from
     *             the data file
     */
    public static List<ZipCode> loadAllFromCsvFile(String path) throws IOException {
        return ZipCode.loadAllFromCsvFile(new File(path));
    }
    
    /**
     * Returns a list of all zip codes contained in the given file.
     * 
     * @param file the file from which to load zip codes
     * 
     * @return a list of all zip codes contained in the given file
     * 
     * @throws IOException
     *             in the event of any kind of error in loading zip codes from
     *             the data file
     */
    public static List<ZipCode> loadAllFromCsvFile(File file) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(file));
        
        Vector<ZipCode> zipCodes = new Vector<ZipCode>();
        
        try {
            String[] row = reader.readNext();
            
            while(row != null) {
                double longitude = -Double.parseDouble(row[4]);
                double latitude = Double.parseDouble(row[5]);
                
                zipCodes.add(new ZipCode(Integer.parseInt(row[1]), row[3], row[2], latitude, longitude));
                
                row = reader.readNext();
            }
        } finally {
            reader.close();
        }
        
        return zipCodes;
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
