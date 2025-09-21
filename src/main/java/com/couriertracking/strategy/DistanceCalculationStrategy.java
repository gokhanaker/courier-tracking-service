package com.couriertracking.strategy;

public interface DistanceCalculationStrategy {
    
    double calculateDistance(double lat1, double lon1, double lat2, double lon2);    
    String getAlgorithmName();
}