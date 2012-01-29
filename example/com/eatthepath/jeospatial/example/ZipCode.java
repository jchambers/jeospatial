package com.eatthepath.jeospatial.example;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;

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
    
    public static List<ZipCode> loadAllFromCsvFile(File file) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(file));
        
        Vector<ZipCode> zipCodes = new Vector<ZipCode>();
        
        try {
            String[] row = reader.readNext();
            
            while(row != null) {
                double longitude = -Double.parseDouble(row[4]);
                double latitude = Double.parseDouble(row[5]);
                
                zipCodes.add(new ZipCode(row[1], row[3], row[2], latitude, longitude));
                
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
        return "ZipCode [zipCode=" + zipCode + ", city=" + city + ", state="
                + state + "]";
    }
}
