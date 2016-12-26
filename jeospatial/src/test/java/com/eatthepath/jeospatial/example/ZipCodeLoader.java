package com.eatthepath.jeospatial.example;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;

public class ZipCodeLoader {

    public static List<ZipCode> loadAllZipCodes() throws IOException {

        final CSVReader reader = new CSVReader(new InputStreamReader(ZipCodeLoader.class.getResourceAsStream("/zips.csv")));
        final ArrayList<ZipCode> zipCodes = new ArrayList<>();

        try {
            String[] row = reader.readNext();

            while (row != null) {
                final double longitude = -Double.parseDouble(row[4]);
                final double latitude = Double.parseDouble(row[5]);

                zipCodes.add(new ZipCode(Integer.parseInt(row[1]), row[3], row[2], latitude, longitude));

                row = reader.readNext();
            }
        } finally {
            reader.close();
        }

        return zipCodes;
    }
}
