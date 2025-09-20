package com.couriertracking.service;

import com.couriertracking.exception.CourierNotFoundException;
import com.couriertracking.model.Location;
import com.couriertracking.repository.CourierRepository;
import com.couriertracking.repository.LocationRepository;
import com.couriertracking.util.DistanceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistanceCalculationService {
    
    private final LocationRepository locationRepository;
    private final CourierRepository courierRepository;
    
    @Transactional(readOnly = true)
    public Double getTotalTravelDistance(UUID courierId) {
        log.debug("Calculating total travel distance for courier: {}", courierId);
        
        // Validate courier exists
        if (!courierRepository.existsById(courierId)) {
            throw new CourierNotFoundException("Courier not found with ID: " + courierId);
        }
        
        // Get all locations ordered by timestamp
        List<Location> locations = locationRepository.findByCourierIdOrderByTimestampAsc(courierId);
        
        if (locations.size() < 2) {
            log.debug("Less than 2 locations found for courier {}, returning 0.0", courierId);
            return 0.0;
        }
        
        double totalDistance = 0.0;
        
        // Calculate distance between consecutive points
        for (int i = 1; i < locations.size(); i++) {
            Location previous = locations.get(i - 1);
            Location current = locations.get(i);
            
            double segmentDistance = DistanceUtils.calculateDistanceInKilometers(
                previous.getLatitude(), previous.getLongitude(),
                current.getLatitude(), current.getLongitude()
            );
            
            totalDistance += segmentDistance;
        }
        
        log.debug("Calculated total distance for courier {}: {:.3f} km", courierId, totalDistance);
        return totalDistance;
    }
}