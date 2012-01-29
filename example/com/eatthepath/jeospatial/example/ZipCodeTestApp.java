package com.eatthepath.jeospatial.example;

import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import com.eatthepath.jeospatial.SimpleGeospatialPoint;
import com.eatthepath.jeospatial.util.GeospatialDistanceComparator;
import com.eatthepath.jeospatial.vptree.VPTree;

import au.com.bytecode.opencsv.CSVReader;

public class ZipCodeTestApp {
    public static void main(String[] args) throws IOException {
        Vector<ZipCode> zipCodes = new Vector<ZipCode>();
        
        CSVReader reader = new CSVReader(new FileReader("data/zips.csv"));
        int rowsRead = 0;
        
        long start = System.currentTimeMillis();
        
        try {
            String[] row = reader.readNext();
            
            while(row != null) {
                rowsRead += 1;
                
                double longitude = -Double.parseDouble(row[4]);
                double latitude = Double.parseDouble(row[5]);
                
                zipCodes.add(new ZipCode(row[1], row[3], row[2], latitude, longitude));
                
                row = reader.readNext();
            }
        } finally {
            reader.close();
        }
        
        long end = System.currentTimeMillis();
        
        System.out.format("Loaded %d zip codes in %d milliseconds.\n", rowsRead, end - start);
        
        // Now put all of those zip codes into a vp-tree
        start = System.currentTimeMillis();
        VPTree<ZipCode> zipCodeTree = new VPTree<ZipCode>(zipCodes, 20);
        end = System.currentTimeMillis();
        
        System.out.format("Build vp-tree from zip code list in %d milliseconds.\n", end - start);
        
        // Now let's compare search performance! We'll start by picking a query
        // point which, for lack of a better idea, will be the position of the
        // local burrito place.
        SimpleGeospatialPoint anasTacqueria = new SimpleGeospatialPoint(42.394923,-71.121728);
        
        // Dumb nearest neighbor search method: sort the entire list of zip codes!
        start = System.currentTimeMillis();
        java.util.Collections.sort(zipCodes, new GeospatialDistanceComparator<ZipCode>(anasTacqueria));
        end = System.currentTimeMillis();
        
        System.out.format("Sorted list of zip codes by distance from Ana's Tacqueria in %d milliseconds.\n", end - start);
        System.out.println("Ten closest zip codes by list sort approach:");
        
        for(int i = 0; i < 10; i++) {
            ZipCode z = zipCodes.get(i);
            System.out.format("\t%.1f meters - %s\n", anasTacqueria.getDistanceTo(z), z);
        }
        
        // Now let's do OVER NINE THOUSAND searches using the vp-tree. We
        // pre-generate random test points to remove random number generation
        // from the time measurement.
        SimpleGeospatialPoint[] testPoints = new SimpleGeospatialPoint[10000];
        Random r = new Random();
        
        for(int i = 0; i < testPoints.length; i++) {
            double latitude = -70d - (r.nextDouble() * 50d);
            double longitude = 28d + (r.nextDouble() * 14d);
            
            testPoints[i] = new SimpleGeospatialPoint(latitude, longitude);
        }
        
        start = System.currentTimeMillis();
        
        for(SimpleGeospatialPoint p : testPoints) {
            zipCodeTree.getNearestNeighbors(p, 10);
        }
        
        end = System.currentTimeMillis();
        
        System.out.format("Performed %d vp-tree searches in %d milliseconds.\n", testPoints.length, end - start);
        System.out.println("Ten closest zip codes by vp-tree search approach:");
        
        for(ZipCode z : zipCodeTree.getNearestNeighbors(anasTacqueria, 10)) {
            System.out.format("\t%.1f meters - %s\n", anasTacqueria.getDistanceTo(z), z);
        }
    }
}
