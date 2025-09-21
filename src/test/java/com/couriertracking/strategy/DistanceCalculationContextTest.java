package com.couriertracking.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Distance Calculation Context Tests")
class DistanceCalculationContextTest {

    @Test
    @DisplayName("Should initialize with Euclidean strategy by default")
    void shouldInitializeWithEuclideanStrategyByDefault() {
        EuclideanDistanceStrategy euclideanStrategy = new EuclideanDistanceStrategy();
        HaversineDistanceStrategy haversineStrategy = new HaversineDistanceStrategy();

        DistanceCalculationContext context = new DistanceCalculationContext(
                "euclidean", euclideanStrategy, haversineStrategy);

        String algorithmName = context.getCurrentAlgorithmName();
        assertThat(algorithmName).isEqualTo("Euclidean Distance");
    }

    @Test
    @DisplayName("Should initialize with Haversine strategy when configured")
    void shouldInitializeWithHaversineStrategyWhenConfigured() {
        EuclideanDistanceStrategy euclideanStrategy = new EuclideanDistanceStrategy();
        HaversineDistanceStrategy haversineStrategy = new HaversineDistanceStrategy();

        DistanceCalculationContext context = new DistanceCalculationContext(
                "haversine", euclideanStrategy, haversineStrategy);

        String algorithmName = context.getCurrentAlgorithmName();
        assertThat(algorithmName).isEqualTo("Haversine Distance");
    }
}