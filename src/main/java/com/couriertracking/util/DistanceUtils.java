package com.couriertracking.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for distance calculations
 * Provides methods for calculating distances between geographical coordinates
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DistanceUtils {
    
    // Conversion factors for Turkey/Istanbul region (~40° latitude)
    private static final double METERS_PER_DEGREE_LATITUDE = 111000.0; // 1 degree latitude ≈ 111km everywhere
    private static final double METERS_PER_DEGREE_LONGITUDE = 85000.0;  // 1 degree longitude ≈ 85km at Turkey's latitude
    private static final double METERS_TO_KILOMETERS = 1000.0;
    
    public static double calculateDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        // Calculate differences in degrees
        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;
        
        // Convert to approximate meters
        double latMeters = deltaLat * METERS_PER_DEGREE_LATITUDE;
        double lonMeters = deltaLon * METERS_PER_DEGREE_LONGITUDE;
        
        // Apply Euclidean distance formula: √((x2-x1)² + (y2-y1)²)
        return Math.sqrt(latMeters * latMeters + lonMeters * lonMeters);
    }
    
    public static double calculateDistanceInKilometers(double lat1, double lon1, double lat2, double lon2) {
        return calculateDistanceInMeters(lat1, lon1, lat2, lon2) / METERS_TO_KILOMETERS;
    }
}