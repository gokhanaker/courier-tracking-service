package com.couriertracking.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Euclidean Distance Strategy Tests")
class EuclideanDistanceStrategyTest {

    private final EuclideanDistanceStrategy strategy = new EuclideanDistanceStrategy();

    @Test
    @DisplayName("Should calculate Euclidean distance correctly")
    void shouldCalculateEuclideanDistanceCorrectly() {
        double lat1 = 40.9923307;  // Ataşehir MMM Migros
        double lon1 = 29.1244229;
        double lat2 = 40.986106;   // Novada MMM Migros  
        double lon2 = 29.1161293;

        double distance = strategy.calculateDistance(lat1, lon1, lat2, lon2);

        assertThat(distance).isPositive();
        assertThat(distance).isBetween(0.0, 2.0);
    }

    @Test
    @DisplayName("Should return zero for same coordinates")
    void shouldReturnZeroForSameCoordinates() {
        double lat = 40.9923307;
        double lon = 29.1244229;

        double distance = strategy.calculateDistance(lat, lon, lat, lon);

        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should return correct algorithm name")
    void shouldReturnCorrectAlgorithmName() {
        String algorithmName = strategy.getAlgorithmName();

        assertThat(algorithmName).isEqualTo("Euclidean Distance");
    }
}