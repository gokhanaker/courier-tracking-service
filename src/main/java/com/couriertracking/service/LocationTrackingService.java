package com.couriertracking.service;

import com.couriertracking.dto.LocationUpdateRequest;
import com.couriertracking.dto.LocationUpdateResponse;
import com.couriertracking.exception.CourierNotFoundException;
import com.couriertracking.exception.InvalidLocationException;
import com.couriertracking.exception.LocationTrackingException;
import com.couriertracking.model.Courier;
import com.couriertracking.model.Location;
import com.couriertracking.model.StoreEntrance;
import com.couriertracking.repository.CourierRepository;
import com.couriertracking.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationTrackingService {
    
    private final LocationRepository locationRepository;
    private final CourierRepository courierRepository;
    private final StoreEntranceService storeEntranceService;
    private final DistanceCalculationService distanceCalculationService;
    
    @Transactional
    public LocationUpdateResponse updateCourierLocation(LocationUpdateRequest request) {        
        
        Courier courier = courierRepository.findById(request.getCourierId())
            .orElseThrow(() -> new CourierNotFoundException(
                "Courier not found with ID: " + request.getCourierId()));
        
        try {
            Location location = new Location();
            location.setCourier(courier);
            location.setLatitude(request.getLatitude());
            location.setLongitude(request.getLongitude());
            location.setTimestamp(request.getTimestamp());
            
            Location savedLocation = locationRepository.save(location);
            
            // Update distance incrementally
            distanceCalculationService.updateDistanceForNewLocation(courier.getId(), savedLocation);
            
            // Check for store entrances
            StoreEntrance storeEntrance = storeEntranceService.checkAndLogStoreEntrance(
                courier, 
                request.getLatitude(), 
                request.getLongitude(), 
                request.getTimestamp()
            );
            
            String message = storeEntrance != null 
                ? "Location updated successfully. Store entrance detected at: " + storeEntrance.getStore().getName()
                : "Location updated successfully";
            
            log.info("Location successfully updated for courier {}: ({}, {}) at {}", 
                courier.getId(), request.getLatitude(), request.getLongitude(), request.getTimestamp());
            
            return new LocationUpdateResponse(
                savedLocation.getId(),
                courier.getId(),
                request.getLatitude(),
                request.getLongitude(),
                request.getTimestamp(),
                message
            );
            
        } catch (Exception e) {
            if (e instanceof CourierNotFoundException || e instanceof InvalidLocationException) {
                throw e;
            }
            log.error("Error saving location for courier {}: {}", request.getCourierId(), e.getMessage());
            throw new LocationTrackingException("Failed to update courier location");
        }
    }
}