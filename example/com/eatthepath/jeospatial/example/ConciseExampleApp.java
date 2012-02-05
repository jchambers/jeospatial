package com.eatthepath.jeospatial.example;

import java.io.IOException;
import java.util.List;

import com.eatthepath.jeospatial.GeospatialPointDatabase;
import com.eatthepath.jeospatial.SearchCriteria;
import com.eatthepath.jeospatial.util.SimpleGeospatialPoint;
import com.eatthepath.jeospatial.vptree.VPTree;

/**
 * A very simple test application that shows the very basics of using a
 * geospatial point database.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 */
public class ConciseExampleApp {
    public static void main(String[] args) throws IOException {
        ConciseExampleApp.getTenClosestNeighbors();
        ConciseExampleApp.getAllWithinRange();
        ConciseExampleApp.getNearestOutsideMassachusetts();
        ConciseExampleApp.getAllInBoundingBox();
    }
    
    public static void getTenClosestNeighbors() throws IOException {
        // Load a bunch of zip codes from a file and construct a vp-tree from
        // those points
        List<ZipCode> zipCodes = ZipCode.loadAllFromCsvFile();
        GeospatialPointDatabase<ZipCode> database = new VPTree<ZipCode>(zipCodes);
        
        // Pick a query point (Davis Square in Somerville, MA, USA)
        SimpleGeospatialPoint davisSquare = new SimpleGeospatialPoint(42.396745, -71.122479);
        
        // Find the ten nearest zip codes to Davis Square
        List<ZipCode> nearestZipCodes = database.getNearestNeighbors(davisSquare, 10);
        
        ConciseExampleApp.printZipCodeList("Ten nearest zip codes to Davis Square:", nearestZipCodes);
    }
    
    public static void getAllWithinRange() throws IOException {
        // Load a bunch of zip codes from a file and construct a vp-tree from
        // those points
        List<ZipCode> zipCodes = ZipCode.loadAllFromCsvFile();
        GeospatialPointDatabase<ZipCode> database = new VPTree<ZipCode>(zipCodes);
        
        // Pick a query point (Davis Square in Somerville, MA, USA)
        SimpleGeospatialPoint davisSquare = new SimpleGeospatialPoint(42.396745, -71.122479);
        
        // Find all zip codes within ten kilometers of Davis Square
        List<ZipCode> zipCodesWithinRange = database.getAllNeighborsWithinDistance(davisSquare, 10 * 1000);
        
        ConciseExampleApp.printZipCodeList("All zip codes within 10km of Davis Square:", zipCodesWithinRange);
    }
    
    public static void getNearestOutsideMassachusetts() throws IOException {
        // Load a bunch of zip codes from a file and construct a vp-tree from
        // those points
        List<ZipCode> zipCodes = ZipCode.loadAllFromCsvFile();
        GeospatialPointDatabase<ZipCode> database = new VPTree<ZipCode>(zipCodes);
        
        // Pick a query point (Davis Square in Somerville, MA, USA)
        SimpleGeospatialPoint davisSquare = new SimpleGeospatialPoint(42.396745, -71.122479);
        
        // Specify search criteria that matches anything outside of the state of
        // Massachusetts
        SearchCriteria<ZipCode> searchCriteria = new SearchCriteria<ZipCode>() {
            @Override
            public boolean matches(ZipCode zipCode) {
                return !zipCode.getState().equals("MA");
            }
        };
        
        // Find the ten closest zip codes to Davis Square that are outside of
        // Massachusetts
        List<ZipCode> closestOutsideMA = database.getNearestNeighbors(davisSquare, 10, searchCriteria);
        
        ConciseExampleApp.printZipCodeList("Ten nearest zip codes to Davis Square outside Massachusetts:", closestOutsideMA);
    }
    
    public static void getAllInBoundingBox() throws IOException {
        // Load a bunch of zip codes from a file and construct a vp-tree from
        // those points
        List<ZipCode> zipCodes = ZipCode.loadAllFromCsvFile();
        GeospatialPointDatabase<ZipCode> database = new VPTree<ZipCode>(zipCodes);
        
        // Pick a query point (Davis Square in Somerville, MA, USA)
        SimpleGeospatialPoint davisSquare = new SimpleGeospatialPoint(42.396745, -71.122479);
        
        // Find all of the zip codes in a bounding "box"
        List<ZipCode> inBoundingBox = database.getAllPointsInBoundingBox(-75, -70, 43, 42);
        
        ConciseExampleApp.printZipCodeList("Zip codes inside bounding box:", inBoundingBox);
    }
    
    public static void printZipCodeList(String header, List<ZipCode> zipCodes) {
        System.out.println(header);
        
        for(ZipCode z : zipCodes) {
            System.out.format("\t%s%n", z);
        }
    }
}
