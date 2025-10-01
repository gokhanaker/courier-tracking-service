package com.couriertracking.util;

import com.couriertracking.strategy.DistanceCalculationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j // Using Lombok for logging
public class DistanceUtils {
    
    private final DistanceCalculationContext distanceCalculationContext;
    
    public double calculateDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        double distanceKm = calculateDistanceInKilometers(lat1, lon1, lat2, lon2);
        return distanceKm * 1000.0;
    }
    
    public double calculateDistanceInKilometers(double lat1, double lon1, double lat2, double lon2) {
        log.debug("Calculating distance using {}: ({}, {}) to ({}, {})", 
            distanceCalculationContext.getCurrentAlgorithmName(), lat1, lon1, lat2, lon2);
        
        double distance = distanceCalculationContext.calculateDistance(lat1, lon1, lat2, lon2);
        
        log.debug("Calculated distance: {:.3f} km using {}", distance, distanceCalculationContext.getCurrentAlgorithmName());
        return distance;
    }

    public String getCurrentAlgorithm() {
        return distanceCalculationContext.getCurrentAlgorithmName();
    }
}