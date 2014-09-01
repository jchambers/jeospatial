package com.eatthepath.jeospatial.util;

import com.eatthepath.jeospatial.GeospatialPoint;
import com.eatthepath.jeospatial.GeospatialIndex;
import com.eatthepath.jeospatial.SearchCriteria;

/**
 * A {@link SearchCriteria} implementation that matches all points within a
 * bounding "box." The {@code BoundingBoxSearchCriteria} class is designed to
 * assist in implementing bounding box search methods specified by the
 * {@link GeospatialIndex} interface.
 * 
 * @author <a href="mailto:jon.chambers@gmail.com">Jon Chambers</a>
 * 
 * @see GeospatialIndex#getAllPointsInBoundingBox(double, double, double, double)
 * @see GeospatialIndex#getAllPointsInBoundingBox(double, double, double, double, GeospatialPoint)
 * @see GeospatialIndex#getAllPointsInBoundingBox(double, double, double, double, SearchCriteria)
 * @see GeospatialIndex#getAllPointsInBoundingBox(double, double, double, double, SearchCriteria, GeospatialPoint)
 */
public class BoundingBoxSearchCriteria<T extends GeospatialPoint> implements SearchCriteria<T> {
    private final double north;
    private final double south;
    private final double east;
    private final double west;
    
    private final SearchCriteria<T> otherCriteria;
    
    /**
     * Constructs a new set of bounding box search criteria with the given box
     * limits.
     * 
     * @param west the western limit of the bounding box in degrees
     * @param east the eastern limit of the bounding box in degrees
     * @param north the northern limit of the bounding box in degrees
     * @param south the southern limit of the bounding box in degrees
     */
    public BoundingBoxSearchCriteria(double west, double east, double north, double south) {
        this(west, east, north, south, null);
    }
    
    /**
     * Constructs a new set of bounding box search criteria with the given box
     * limits and additional search criteria. The additional search criteria are
     * applied after points are tested for presence inside the bounding box.
     * 
     * @param west the western limit of the bounding box in degrees
     * @param east the eastern limit of the bounding box in degrees
     * @param north the northern limit of the bounding box in degrees
     * @param south the southern limit of the bounding box in degrees
     * @param otherCriteria
     *              additional criteria to apply after testing a point
     *              for presence inside the bounding box limits; may be
     *              {@code null}
     */
    public BoundingBoxSearchCriteria(double west, double east, double north, double south, SearchCriteria<T> otherCriteria) {
        this.west = ((west + 180) % 360) - 180;
        this.east = ((east + 180) % 360) - 180;
        
        if(north < -90 || north > 90) {
            throw new IllegalArgumentException("Northern bound must be between -90 and +90 degrees (inclusive).");
        }
        
        if(south < -90 || south > 90) {
            throw new IllegalArgumentException("Southern bound must be between -90 and +90 degrees (inclusive).");
        }
        
        if(south > north) {
            throw new IllegalArgumentException("Northern bound must be north of or coincident with southern bound.");
        }
        
        this.north = north;
        this.south = south;
        
        this.otherCriteria = otherCriteria;
    }
    
    /**
     * Calculates the minimum eastward angle traveled from a line of longitude
     * to a point. If the point is coincident with the line, this method returns
     * 360 degrees.
     * 
     * @param longitude
     *            the line of longitude at which to begin travel
     * @param point
     *            the point to which to travel
     * 
     * @return the eastward-traveling distance between the line and the point in
     *         degrees
     */
    private double degreesEast(double longitude, T point) {
        return point.getLongitude() > longitude ?
                point.getLongitude() - longitude :
                Math.abs(360 - (point.getLongitude() - longitude));
    }
    
    /**
     * Calculates the minimum westward angle traveled from a line of longitude
     * to a point. If the point is coincident with the line, this method returns
     * 360 degrees.
     * 
     * @param longitude
     *            the line of longitude at which to begin travel
     * @param point
     *            the point to which to travel
     * 
     * @return the westward-traveling distance between the line and the point in
     *         degrees
     */
    private double degreesWest(double longitude, T point) {
        return point.getLongitude() < longitude ?
                longitude - point.getLongitude() :
                Math.abs(360 - (longitude - point.getLongitude()));
    }
    
    /**
     * Tests whether the given point falls within the bounds of the box given at
     * construction time and satisfies the given additional search criteria (if
     * any).
     * 
     * @param point
     *            the point to test for presence inside the bounding box
     * 
     * @return {@code true} if the point is inside the bounding box and
     *         satisfies the additional search criteria (if not {@code null}) or
     *         {@code false} otherwise
     */
    @Override
    public boolean matches(T point) {
        // Checking for latitude bounds is easy.
        if(point.getLatitude() > this.north || point.getLatitude() < this.south) {
            return false;
        }
        
        // If the point is inside our bounding box, it will be shorter to get to
        // the point by traveling east from our western boundary than by
        // traveling east from the eastern boundary.
        if(this.degreesEast(this.west, point) > this.degreesEast(this.east, point)) {
            return false;
        }
        
        // Similarly, it should be shorter to get to the point by traveling west
        // from our eastern boundary than by traveling west from the western
        // boundary.
        if(this.degreesWest(this.east, point) > this.degreesWest(this.west, point)) {
            return false;
        }
        
        // The point's inside our bounding box. If we have other search
        // criteria, apply them now.
        if(this.otherCriteria != null) {
            return this.otherCriteria.matches(point);
        } else {
            return true;
        }
    }
}
