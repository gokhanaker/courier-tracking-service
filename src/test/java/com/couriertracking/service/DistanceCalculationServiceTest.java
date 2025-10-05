package com.couriertracking.service;

import com.couriertracking.cache.CourierDistanceCache;
import com.couriertracking.exception.CourierNotFoundException;
import com.couriertracking.model.Courier;
import com.couriertracking.model.CourierDistance;
import com.couriertracking.model.Location;
import com.couriertracking.repository.CourierDistanceRepository;
import com.couriertracking.repository.CourierRepository;
import com.couriertracking.repository.LocationRepository;
import com.couriertracking.util.DistanceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DistanceCalculationService Tests")
class DistanceCalculationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private CourierDistanceRepository courierDistanceRepository;

    @Mock
    private DistanceUtils distanceUtils;

    @Mock
    private CourierDistanceCache courierDistanceCache;

    @InjectMocks
    private DistanceCalculationService distanceCalculationService;

    private UUID courierId;
    private Courier courier;
    private CourierDistance courierDistance;
    private Location location1;
    private Location location2;

    @BeforeEach
    void setUp() {
        courierId = UUID.randomUUID();
        
        courier = new Courier();
        courier.setId(courierId);
        courier.setName("Test Courier");
        courier.setEmail("test@courier.com");
        courier.setPhoneNumber("+905551234567");

        courierDistance = new CourierDistance();
        courierDistance.setId(UUID.randomUUID());
        courierDistance.setCourier(courier);
        courierDistance.setTotalDistance(5.0);
        courierDistance.setUpdatedAt(LocalDateTime.now());

        location1 = new Location();
        location1.setId(UUID.randomUUID());
        location1.setCourier(courier);
        location1.setLatitude(40.9923307);
        location1.setLongitude(29.1244229);
        location1.setTimestamp(LocalDateTime.now().minusMinutes(10));

        location2 = new Location();
        location2.setId(UUID.randomUUID());
        location2.setCourier(courier);
        location2.setLatitude(40.986106);
        location2.setLongitude(29.1161293);
        location2.setTimestamp(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return total distance when courier distance exists")
    void shouldReturnTotalDistanceWhenExists() {
        when(courierRepository.existsById(courierId)).thenReturn(true);
        when(courierDistanceCache.getFromCache(courierId)).thenReturn(Optional.empty()); // Cache miss
        when(courierDistanceRepository.findByCourierId(courierId))
                .thenReturn(Optional.of(courierDistance));

        Double result = distanceCalculationService.getTotalTravelDistance(courierId);

        assertThat(result).isEqualTo(5.0);
        verify(courierRepository).existsById(courierId);
        verify(courierDistanceCache).getFromCache(courierId);
        verify(courierDistanceRepository).findByCourierId(courierId);
        verify(courierDistanceCache).saveToCache(courierId, 5.0); // Verify cache backfill
    }

    @Test
    @DisplayName("Should return 0.0 when no distance record exists")
    void shouldReturnZeroWhenNoDistanceRecord() {
        when(courierRepository.existsById(courierId)).thenReturn(true);
        when(courierDistanceCache.getFromCache(courierId)).thenReturn(Optional.empty()); // Cache miss
        when(courierDistanceRepository.findByCourierId(courierId))
                .thenReturn(Optional.empty());

        Double result = distanceCalculationService.getTotalTravelDistance(courierId);

        assertThat(result).isEqualTo(0.0);
        verify(courierRepository).existsById(courierId);
        verify(courierDistanceCache).getFromCache(courierId);
        verify(courierDistanceRepository).findByCourierId(courierId);
    }

    @Test
    @DisplayName("Should return cached distance when available in cache")
    void shouldReturnCachedDistanceWhenAvailable() {
        when(courierRepository.existsById(courierId)).thenReturn(true);
        when(courierDistanceCache.getFromCache(courierId)).thenReturn(Optional.of(7.5)); // Cache hit

        Double result = distanceCalculationService.getTotalTravelDistance(courierId);

        assertThat(result).isEqualTo(7.5);
        verify(courierRepository).existsById(courierId);
        verify(courierDistanceCache).getFromCache(courierId);
        verifyNoInteractions(courierDistanceRepository); // Should not query database
    }

    @Test
    @DisplayName("Should throw exception when courier does not exist")
    void shouldThrowExceptionWhenCourierDoesNotExist() {
        when(courierRepository.existsById(courierId)).thenReturn(false);

        assertThatThrownBy(() -> distanceCalculationService.getTotalTravelDistance(courierId))
                .isInstanceOf(CourierNotFoundException.class)
                .hasMessageContaining("Courier not found with ID: " + courierId);

        verify(courierRepository).existsById(courierId);
        verifyNoInteractions(courierDistanceRepository);
    }

    @Test
    @DisplayName("Should update distance for new location with existing locations")
    void shouldUpdateDistanceForNewLocationWithExistingLocations() {
        List<Location> recentLocations = Arrays.asList(location2, location1); // Most recent first
        double segmentDistance = 1.5;
        double expectedNewTotal = 6.5; // 5.0 + 1.5

        when(courierDistanceRepository.findByCourierId(courierId))
                .thenReturn(Optional.of(courierDistance));
        when(locationRepository.findTop2ByCourierIdOrderByTimestampDesc(courierId))
                .thenReturn(recentLocations);
        when(distanceUtils.calculateDistanceInKilometers(
                location1.getLatitude(), location1.getLongitude(),
                location2.getLatitude(), location2.getLongitude()))
                .thenReturn(segmentDistance);

        distanceCalculationService.updateDistanceForNewLocation(courierId, location2);

        verify(courierDistanceRepository).save(courierDistance);
        verify(courierDistanceCache).saveToCache(courierId, expectedNewTotal);
        assertThat(courierDistance.getTotalDistance()).isEqualTo(expectedNewTotal);
    }

    @Test
    @DisplayName("Should not update distance for first location")
    void shouldNotUpdateDistanceForFirstLocation() {
        List<Location> recentLocations = Arrays.asList(location1); // Only one location

        when(courierDistanceRepository.findByCourierId(courierId))
                .thenReturn(Optional.of(courierDistance));
        when(locationRepository.findTop2ByCourierIdOrderByTimestampDesc(courierId))
                .thenReturn(recentLocations);

        distanceCalculationService.updateDistanceForNewLocation(courierId, location1);


        verify(courierDistanceRepository, never()).save(any());
        assertThat(courierDistance.getTotalDistance()).isEqualTo(5.0);
    }
}