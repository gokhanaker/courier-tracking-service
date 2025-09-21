package com.couriertracking.service;

import com.couriertracking.dto.LocationUpdateRequest;
import com.couriertracking.dto.LocationUpdateResponse;
import com.couriertracking.exception.CourierNotFoundException;
import com.couriertracking.exception.LocationTrackingException;
import com.couriertracking.model.Courier;
import com.couriertracking.model.Location;
import com.couriertracking.model.Store;
import com.couriertracking.model.StoreEntrance;
import com.couriertracking.repository.CourierRepository;
import com.couriertracking.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationTrackingService Tests")
class LocationTrackingServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private StoreEntranceService storeEntranceService;

    @Mock
    private DistanceCalculationService distanceCalculationService;

    @InjectMocks
    private LocationTrackingService locationTrackingService;

    private LocationUpdateRequest validRequest;
    private Courier courier;
    private Location savedLocation;
    private UUID courierId;
    private LocalDateTime timestamp;

    @BeforeEach
    void setUp() {
        courierId = UUID.randomUUID();
        timestamp = LocalDateTime.now();

        validRequest = new LocationUpdateRequest();
        validRequest.setCourierId(courierId);
        validRequest.setLatitude(40.9923307);
        validRequest.setLongitude(29.1244229);
        validRequest.setTimestamp(timestamp);

        courier = new Courier();
        courier.setId(courierId);
        courier.setName("Test Courier");
        courier.setEmail("test@courier.com");
        courier.setPhoneNumber("+905551234567");

        savedLocation = new Location();
        savedLocation.setId(UUID.randomUUID());
        savedLocation.setCourier(courier);
        savedLocation.setLatitude(40.9923307);
        savedLocation.setLongitude(29.1244229);
        savedLocation.setTimestamp(timestamp);
    }

    @Test
    @DisplayName("Should update courier location successfully without store entrance")
    void shouldUpdateLocationSuccessfullyWithoutStoreEntrance() {
        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));
        when(locationRepository.save(any(Location.class))).thenReturn(savedLocation);
        when(storeEntranceService.checkAndLogStoreEntrance(
                eq(courier), eq(40.9923307), eq(29.1244229), eq(timestamp)))
                .thenReturn(null); // No store entrance

        LocationUpdateResponse response = locationTrackingService.updateCourierLocation(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getLocationId()).isEqualTo(savedLocation.getId());
        assertThat(response.getCourierId()).isEqualTo(courierId);
        assertThat(response.getLatitude()).isEqualTo(40.9923307);
        assertThat(response.getLongitude()).isEqualTo(29.1244229);
        assertThat(response.getTimestamp()).isEqualTo(timestamp);
        assertThat(response.getMessage()).isEqualTo("Location updated successfully");

        verify(courierRepository).findById(courierId);
        verify(locationRepository).save(any(Location.class));
        verify(distanceCalculationService).updateDistanceForNewLocation(courierId, savedLocation);
        verify(storeEntranceService).checkAndLogStoreEntrance(
                courier, 40.9923307, 29.1244229, timestamp);
    }

    @Test
    @DisplayName("Should update courier location successfully with store entrance")
    void shouldUpdateLocationSuccessfullyWithStoreEntrance() {
        Store store = new Store();
        store.setId(UUID.randomUUID());
        store.setName("Ataşehir MMM Migros");

        StoreEntrance storeEntrance = new StoreEntrance();
        storeEntrance.setId(UUID.randomUUID());
        storeEntrance.setCourier(courier);
        storeEntrance.setStore(store);
        storeEntrance.setEntranceTime(timestamp);

        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));
        when(locationRepository.save(any(Location.class))).thenReturn(savedLocation);
        when(storeEntranceService.checkAndLogStoreEntrance(
                eq(courier), eq(40.9923307), eq(29.1244229), eq(timestamp)))
                .thenReturn(storeEntrance);

        LocationUpdateResponse response = locationTrackingService.updateCourierLocation(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getLocationId()).isEqualTo(savedLocation.getId());
        assertThat(response.getCourierId()).isEqualTo(courierId);
        assertThat(response.getMessage())
                .isEqualTo("Location updated successfully. Store entrance detected at: Ataşehir MMM Migros");

        verify(courierRepository).findById(courierId);
        verify(locationRepository).save(any(Location.class));
        verify(distanceCalculationService).updateDistanceForNewLocation(courierId, savedLocation);
        verify(storeEntranceService).checkAndLogStoreEntrance(
                courier, 40.9923307, 29.1244229, timestamp);
    }

    @Test
    @DisplayName("Should throw CourierNotFoundException when courier does not exist")
    void shouldThrowExceptionWhenCourierNotFound() {
        when(courierRepository.findById(courierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationTrackingService.updateCourierLocation(validRequest))
                .isInstanceOf(CourierNotFoundException.class)
                .hasMessageContaining("Courier not found with ID: " + courierId);

        verify(courierRepository).findById(courierId);
        verifyNoInteractions(locationRepository);
        verifyNoInteractions(distanceCalculationService);
        verifyNoInteractions(storeEntranceService);
    }

    @Test
    @DisplayName("Should create location entity with correct properties")
    void shouldCreateLocationEntityWithCorrectProperties() {
        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> {
            Location location = invocation.getArgument(0);
            assertThat(location.getCourier()).isEqualTo(courier);
            assertThat(location.getLatitude()).isEqualTo(40.9923307);
            assertThat(location.getLongitude()).isEqualTo(29.1244229);
            assertThat(location.getTimestamp()).isEqualTo(timestamp);
            return savedLocation;
        });
        when(storeEntranceService.checkAndLogStoreEntrance(
                eq(courier), eq(40.9923307), eq(29.1244229), eq(timestamp)))
                .thenReturn(null);

        LocationUpdateResponse response = locationTrackingService.updateCourierLocation(validRequest);

        assertThat(response).isNotNull();
        verify(locationRepository).save(any(Location.class));
    }
}