package com.couriertracking.util;

import com.couriertracking.strategy.DistanceCalculationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DistanceUtils Tests")
class DistanceUtilsTest {

    @Mock
    private DistanceCalculationContext distanceCalculationContext;

    @InjectMocks
    private DistanceUtils distanceUtils;

    private double lat1 = 40.9923307; // Ataşehir MMM Migros
    private double lon1 = 29.1244229;
    private double lat2 = 40.986106;  // Novada MMM Migros
    private double lon2 = 29.1161293;

    @BeforeEach
    void setUp() {
        // Mock context responses
        when(distanceCalculationContext.getCurrentAlgorithmName())
                .thenReturn("Euclidean Distance");
    }

    @Test
    @DisplayName("Should calculate distance in kilometers correctly")
    void shouldCalculateDistanceInKilometers() {
        double expectedDistanceKm = 1.5;
        when(distanceCalculationContext.calculateDistance(lat1, lon1, lat2, lon2))
                .thenReturn(expectedDistanceKm);

        double result = distanceUtils.calculateDistanceInKilometers(lat1, lon1, lat2, lon2);

        assertThat(result).isEqualTo(expectedDistanceKm);
        verify(distanceCalculationContext).calculateDistance(lat1, lon1, lat2, lon2);
        verify(distanceCalculationContext, times(2)).getCurrentAlgorithmName(); // Called twice for logging
    }

    @Test
    @DisplayName("Should calculate distance in meters correctly")
    void shouldCalculateDistanceInMeters() {
        double distanceKm = 1.5;
        double expectedDistanceMeters = 1500.0;
        when(distanceCalculationContext.calculateDistance(lat1, lon1, lat2, lon2))
                .thenReturn(distanceKm);

        double result = distanceUtils.calculateDistanceInMeters(lat1, lon1, lat2, lon2);

        assertThat(result).isEqualTo(expectedDistanceMeters);
        verify(distanceCalculationContext).calculateDistance(lat1, lon1, lat2, lon2);
    }

    @Test
    @DisplayName("Should return current algorithm name")
    void shouldReturnCurrentAlgorithmName() {
        String algorithmName = distanceUtils.getCurrentAlgorithm();

        assertThat(algorithmName).isEqualTo("Euclidean Distance");
        verify(distanceCalculationContext).getCurrentAlgorithmName();
    }
}