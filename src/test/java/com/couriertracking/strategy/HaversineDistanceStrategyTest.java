package com.couriertracking.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Haversine Distance Strategy Tests")
class HaversineDistanceStrategyTest {

    private final HaversineDistanceStrategy strategy = new HaversineDistanceStrategy();

    @Test
    @DisplayName("Should calculate Haversine distance correctly")
    void shouldCalculateHaversineDistanceCorrectly() {
        double lat1 = 40.9923307;  // Ataşehir MMM Migros
        double lon1 = 29.1244229;
        double lat2 = 40.986106;   // Novada MMM Migros
        double lon2 = 29.1161293;

        double distance = strategy.calculateDistance(lat1, lon1, lat2, lon2);

        assertThat(distance).isPositive();
        assertThat(distance).isBetween(0.5, 1.5);
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

        assertThat(algorithmName).isEqualTo("Haversine Distance");
    }
}