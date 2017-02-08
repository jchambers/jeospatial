package com.eatthepath.jeospatial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import com.eatthepath.jvptree.DistanceComparator;

@State(Scope.Thread)
public class VPTreeQueryBenchmark {

    @Param({"100000"})
    public int pointCount;

    private List<GeospatialPoint> points;
    private VPTreeGeospatialIndex<GeospatialPoint> index;

    private final Random random = new Random();
    private final HaversineDistanceFunction distanceFunction = new HaversineDistanceFunction();

    private static final int RESULT_SET_SIZE = 32;

    @Setup
    public void setUp() {
        this.points = new ArrayList<>(this.pointCount);

        for (int i = 0; i < this.pointCount; i++) {
            this.points.add(this.createRandomPoint());
        }

        this.index = new VPTreeGeospatialIndex<>(this.points);
    }

    @Benchmark
    public List<GeospatialPoint> benchmarkNaiveSearch() {
        Collections.sort(this.points, new DistanceComparator<>(this.createRandomPoint(), this.distanceFunction));
        return this.points.subList(0, RESULT_SET_SIZE);
    }

    @Benchmark
    public List<GeospatialPoint> benchmarkQueryTree() {
        return this.index.getNearestNeighbors(this.createRandomPoint(), RESULT_SET_SIZE);
    }

    private GeospatialPoint createRandomPoint() {
        final double latitude = (this.random.nextDouble() * 180.0) - 90;
        final double longitude = (this.random.nextDouble() * 360.0) - 180;

        return new GeospatialPoint() {

            @Override
            public double getLongitude() {
                return longitude;
            }

            @Override
            public double getLatitude() {
                return latitude;
            }
        };
    }
}
