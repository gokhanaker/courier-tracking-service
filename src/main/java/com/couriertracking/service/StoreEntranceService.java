package com.couriertracking.service;

import com.couriertracking.model.Courier;
import com.couriertracking.model.Store;
import com.couriertracking.model.StoreEntrance;
import com.couriertracking.repository.StoreRepository;
import com.couriertracking.repository.StoreEntranceRepository;
import com.couriertracking.util.DistanceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreEntranceService {
    
    private final StoreRepository storeRepository;
    private final StoreEntranceRepository storeEntranceRepository;
    private final DistanceUtils distanceUtils;
    
    @Value("${courier-tracking.store.entrance-radius-meters:100}")
    private int entranceRadiusMeters;
    
    @Value("${courier-tracking.store.entrance-cooldown-minutes:1}")
    private int entranceCooldownMinutes;
    
    /**
     * Check if courier is near any store and log entrance if applicable
     * @return StoreEntrance if entrance was logged, null otherwise
     */
    public StoreEntrance checkAndLogStoreEntrance(Courier courier, Double latitude, Double longitude, LocalDateTime timestamp) {
        List<Store> allStores = storeRepository.findAll();
        
        for (Store store : allStores) {
            double distanceMeters = distanceUtils.calculateDistanceInMeters(
                latitude, longitude,
                store.getLatitude(), store.getLongitude()
            );
            
            if (distanceMeters <= entranceRadiusMeters) {
                // Check if entrance should be logged (cooldown check)
                if (shouldLogEntrance(courier, store, timestamp)) {
                    StoreEntrance entrance = saveStoreEntrance(courier, store, timestamp);
                    log.info("Store entrance logged: Courier {} entered {}", courier.getId(), store.getName());
                    return entrance;
                }
            }
        }
        
        return null; // No entrance detected or logged
    }
    
    private boolean shouldLogEntrance(Courier courier, Store store, LocalDateTime timestamp) {
        LocalDateTime cooldownThreshold = timestamp.minusMinutes(entranceCooldownMinutes);
        
        return !storeEntranceRepository.existsByCourierAndStoreAndEntranceTimeAfter(
            courier, store, cooldownThreshold
        );
    }
    
    private StoreEntrance saveStoreEntrance(Courier courier, Store store, LocalDateTime timestamp) {
        StoreEntrance entrance = new StoreEntrance();
        entrance.setCourier(courier);
        entrance.setStore(store);
        entrance.setEntranceTime(timestamp);
        
        return storeEntranceRepository.save(entrance);
    }
}