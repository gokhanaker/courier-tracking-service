package com.couriertracking.service;

import com.couriertracking.exception.CourierNotFoundException;
import com.couriertracking.model.Courier;
import com.couriertracking.model.CourierDistance;
import com.couriertracking.model.Location;
import com.couriertracking.repository.CourierDistanceRepository;
import com.couriertracking.repository.CourierRepository;
import com.couriertracking.repository.LocationRepository;
import com.couriertracking.util.DistanceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistanceCalculationService {
    
    private final LocationRepository locationRepository;
    private final CourierRepository courierRepository;
    private final CourierDistanceRepository courierDistanceRepository;
    
    @Transactional(readOnly = true)
    public Double getTotalTravelDistance(UUID courierId) {        
        if (!courierRepository.existsById(courierId)) {
            throw new CourierNotFoundException("Courier not found with ID: " + courierId);
        }
        
        Optional<CourierDistance> courierDistance = courierDistanceRepository.findByCourierId(courierId);
        
        if (courierDistance.isPresent()) {
            Double totalDistance = courierDistance.get().getTotalDistance();
            log.info("Retrieved distance for courier {}: {:.3f} km", courierId, totalDistance);
            return totalDistance;
        }

        log.info("No distance found for courier {} - no locations submitted yet", courierId);
        return 0.0;
    }
    
    @Transactional
    public void updateDistanceForNewLocation(UUID courierId, Location newLocation) {        
        CourierDistance courierDistance = courierDistanceRepository.findByCourierId(courierId)
            .orElseGet(() -> initializeNewCourierDistance(courierId));
        
        List<Location> recentLocations = locationRepository.findTop2ByCourierIdOrderByTimestampDesc(courierId);
        
        if (recentLocations.size() >= 2) {
            // We have at least 2 locations: [newest, previous]
            Location previousLocation = recentLocations.get(1); // Second most recent
            Location currentLocation = recentLocations.get(0);  // Most recent (the new one)
            
            // Calculate distance for this segment only
            double segmentDistance = DistanceUtils.calculateDistanceInKilometers(
                previousLocation.getLatitude(), previousLocation.getLongitude(),
                currentLocation.getLatitude(), currentLocation.getLongitude()
            );
            
            // Add to existing total distance
            double newTotalDistance = courierDistance.getTotalDistance() + segmentDistance;
            courierDistance.setTotalDistance(newTotalDistance);
            courierDistanceRepository.save(courierDistance);
            
            log.debug("Updated distance for courier {}: +{:.3f} km, total: {:.3f} km", 
                courierId, segmentDistance, newTotalDistance);
        } else {
            log.debug("First location for courier {}, distance remains 0.0", courierId);
        }
    }
    
    private CourierDistance initializeNewCourierDistance(UUID courierId) {
        Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException("Courier not found with ID: " + courierId));
        
        CourierDistance courierDistance = new CourierDistance();
        courierDistance.setCourier(courier);
        courierDistance.setTotalDistance(0.0);
        
        CourierDistance saved = courierDistanceRepository.save(courierDistance);
        log.debug("Initialized distance tracking for courier {}", courierId);
        return saved;
    }
}