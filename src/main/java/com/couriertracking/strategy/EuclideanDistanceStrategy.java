package com.couriertracking.strategy;

import org.springframework.stereotype.Component;

/**
 * Euclidean distance calculation strategy
 * Simple straight-line distance calculation
 */
@Component("euclidean")
public class EuclideanDistanceStrategy implements DistanceCalculationStrategy {
    
    // Conversion factors for Turkey/Istanbul region (~40° latitude)
    private static final double METERS_PER_DEGREE_LATITUDE = 111000.0; // 1 degree latitude ≈ 111km everywhere
    private static final double METERS_PER_DEGREE_LONGITUDE = 85000.0;  // 1 degree longitude ≈ 85km at Turkey's latitude
    private static final double METERS_TO_KILOMETERS = 1000.0;
    
    @Override
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Calculate differences in degrees
        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;
        
        // Convert to approximate meters
        double latMeters = deltaLat * METERS_PER_DEGREE_LATITUDE;
        double lonMeters = deltaLon * METERS_PER_DEGREE_LONGITUDE;
        
        // Apply Euclidean distance formula: √((x2-x1)² + (y2-y1)²)
        double distanceMeters = Math.sqrt(latMeters * latMeters + lonMeters * lonMeters);
        
        return distanceMeters / METERS_TO_KILOMETERS;
    }
    
    @Override
    public String getAlgorithmName() {
        return "Euclidean Distance";
    }
}