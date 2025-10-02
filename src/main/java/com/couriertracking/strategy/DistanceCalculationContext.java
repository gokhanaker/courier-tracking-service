package com.couriertracking.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DistanceCalculationContext {
    
    private final DistanceCalculationStrategy strategy;
    
    public DistanceCalculationContext(
        // Select strategy based on yml configuration
        @Value("${courier-tracking.distance.calculation-algorithm:euclidean}") String algorithmName,
        
        // Inject all available strategies
        @Qualifier("euclidean") DistanceCalculationStrategy euclideanStrategy,
        @Qualifier("haversine") DistanceCalculationStrategy haversineStrategy
    ) {
        // Select strategy based on configuration
        switch (algorithmName.toLowerCase()) {
            case "haversine":
                this.strategy = haversineStrategy;
                log.info("🎯 Distance calculation strategy initialized: {}", 
                    haversineStrategy.getAlgorithmName());
                break;
            case "euclidean":
            default:
                this.strategy = euclideanStrategy;
                log.info("🎯 Distance calculation strategy initialized: {}", 
                    euclideanStrategy.getAlgorithmName());
                break;
        }
    }
    
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        return strategy.calculateDistance(lat1, lon1, lat2, lon2);
    }
    
    public String getCurrentAlgorithmName() {
        return strategy.getAlgorithmName();
    }
}