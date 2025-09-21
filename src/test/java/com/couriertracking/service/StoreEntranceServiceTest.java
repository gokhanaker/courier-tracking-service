package com.couriertracking.service;

import com.couriertracking.model.Courier;
import com.couriertracking.model.Store;
import com.couriertracking.model.StoreEntrance;
import com.couriertracking.repository.StoreEntranceRepository;
import com.couriertracking.repository.StoreRepository;
import com.couriertracking.util.DistanceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreEntranceService Tests")
class StoreEntranceServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreEntranceRepository storeEntranceRepository;

    @Mock
    private DistanceUtils distanceUtils;

    @InjectMocks
    private StoreEntranceService storeEntranceService;

    private Courier courier;
    private Store nearbyStore;
    private Store farStore;
    private LocalDateTime timestamp;
    private double courierLatitude = 40.9923307;
    private double courierLongitude = 29.1244229;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storeEntranceService, "entranceRadiusMeters", 100);
        ReflectionTestUtils.setField(storeEntranceService, "entranceCooldownMinutes", 1);

        courier = new Courier();
        courier.setId(UUID.randomUUID());
        courier.setName("Test Courier");
        courier.setEmail("test@courier.com");
        courier.setPhoneNumber("+905551234567");

        // Store within radius (50 meters away)
        nearbyStore = new Store();
        nearbyStore.setId(UUID.randomUUID());
        nearbyStore.setName("Nearby Migros");
        nearbyStore.setLatitude(40.9923307);
        nearbyStore.setLongitude(29.1244229);

        // Store outside radius (200 meters away)
        farStore = new Store();
        farStore.setId(UUID.randomUUID());
        farStore.setName("Far Migros");
        farStore.setLatitude(40.9943307);
        farStore.setLongitude(29.1264229);

        timestamp = LocalDateTime.now();
    }

    @Test
    @DisplayName("Should log store entrance when courier is within radius and no recent entrance")
    void shouldLogStoreEntranceWhenWithinRadiusAndNoCooldown() {
        List<Store> stores = Arrays.asList(nearbyStore, farStore);
        when(storeRepository.findAll()).thenReturn(stores);
        when(distanceUtils.calculateDistanceInMeters(
                courierLatitude, courierLongitude,
                nearbyStore.getLatitude(), nearbyStore.getLongitude()))
                .thenReturn(50.0); // Within 100m radius

        when(storeEntranceRepository.existsByCourierAndStoreAndEntranceTimeAfter(
                eq(courier), eq(nearbyStore), any(LocalDateTime.class)))
                .thenReturn(false); // No recent entrance

        StoreEntrance savedEntrance = new StoreEntrance();
        savedEntrance.setId(UUID.randomUUID());
        savedEntrance.setCourier(courier);
        savedEntrance.setStore(nearbyStore);
        savedEntrance.setEntranceTime(timestamp);
        when(storeEntranceRepository.save(any(StoreEntrance.class)))
                .thenReturn(savedEntrance);

        StoreEntrance result = storeEntranceService.checkAndLogStoreEntrance(
                courier, courierLatitude, courierLongitude, timestamp);

        assertThat(result).isNotNull();
        assertThat(result.getCourier()).isEqualTo(courier);
        assertThat(result.getStore()).isEqualTo(nearbyStore);
        assertThat(result.getEntranceTime()).isEqualTo(timestamp);

        verify(storeRepository).findAll();
        verify(distanceUtils).calculateDistanceInMeters(
                courierLatitude, courierLongitude,
                nearbyStore.getLatitude(), nearbyStore.getLongitude());
        verify(storeEntranceRepository).existsByCourierAndStoreAndEntranceTimeAfter(
                eq(courier), eq(nearbyStore), any(LocalDateTime.class));
        verify(storeEntranceRepository).save(any(StoreEntrance.class));
    }

    @Test
    @DisplayName("Should not log entrance when courier is outside radius")
    void shouldNotLogEntranceWhenOutsideRadius() {
        List<Store> stores = Arrays.asList(farStore);
        when(storeRepository.findAll()).thenReturn(stores);
        when(distanceUtils.calculateDistanceInMeters(
                courierLatitude, courierLongitude,
                farStore.getLatitude(), farStore.getLongitude()))
                .thenReturn(200.0); // Outside 100m radius

        StoreEntrance result = storeEntranceService.checkAndLogStoreEntrance(
                courier, courierLatitude, courierLongitude, timestamp);

        assertThat(result).isNull();
        verifyNoInteractions(storeEntranceRepository);
    }

    @Test
    @DisplayName("Should not log entrance when recent entrance exists (cooldown period)")
    void shouldNotLogEntranceWhenInCooldownPeriod() {
        List<Store> stores = Arrays.asList(nearbyStore);
        when(storeRepository.findAll()).thenReturn(stores);
        when(distanceUtils.calculateDistanceInMeters(
                courierLatitude, courierLongitude,
                nearbyStore.getLatitude(), nearbyStore.getLongitude()))
                .thenReturn(50.0); // Within 100m radius
        when(storeEntranceRepository.existsByCourierAndStoreAndEntranceTimeAfter(
                eq(courier), eq(nearbyStore), any(LocalDateTime.class)))
                .thenReturn(true); // Recent entrance exists

        StoreEntrance result = storeEntranceService.checkAndLogStoreEntrance(
                courier, courierLatitude, courierLongitude, timestamp);

        assertThat(result).isNull();

        verify(storeRepository).findAll();
        verify(storeEntranceRepository, never()).save(any());
    }
}