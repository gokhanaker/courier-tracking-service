package com.couriertracking.service;

import com.couriertracking.exception.CourierNotFoundException;
import com.couriertracking.model.Courier;
import com.couriertracking.model.CourierDistance;
import com.couriertracking.model.Location;
import com.couriertracking.repository.CourierDistanceRepository;
import com.couriertracking.repository.CourierRepository;
import com.couriertracking.repository.LocationRepository;
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
    private final CourierDistanceRepository courierDistanceRepository;
    private final CourierRepository courierRepository;
    
    @Transactional(readOnly = true)
    public Double getTotalTravelDistance(UUID courierId) {        
        if (courierId == null || courierId.toString().isEmpty()) {
            throw new CourierNotFoundException("Courier ID cannot be null or empty");
        }
        
        // Try to get cached distance first
        var cachedDistance = courierDistanceRepository.findByCourierId(courierId);
        if (cachedDistance.isPresent()) {
            log.debug("Returning cached distance for courier {}: {} km", courierId, cachedDistance.get().getTotalDistance());
            return cachedDistance.get().getTotalDistance();
        }
        
        // If no cached distance, calculate from scratch
        return calculateAndCacheTotalDistance(courierId);
    }
    
    @Transactional
    public void updateDistanceForNewLocation(UUID courierId, Location newLocation) {
        var existingDistance = courierDistanceRepository.findByCourierId(courierId);
        
        if (existingDistance.isPresent()) {
            updateIncrementalDistance(existingDistance.get(), newLocation);
        } else {
            // First location for this courier, initialize with 0 distance
            initializeCourierDistance(courierId, newLocation);
        }
    }
    
    @Transactional
    public Double calculateAndCacheTotalDistance(UUID courierId) {
        // Get all locations for the courier ordered by timestamp
        List<Location> locations = locationRepository.findByCourierIdOrderByTimestampAsc(courierId);
        
        if (locations.isEmpty()) {
            throw new CourierNotFoundException("No location data found for courier: " + courierId);
        }
        
        if (locations.size() < 2) {
            // Initialize with 0 distance for first location
            initializeCourierDistance(courierId, locations.get(0));
            return 0.0;
        }
        
        double totalDistance = 0.0;
        
        // Calculate distance between consecutive points
        for (int i = 1; i < locations.size(); i++) {
            Location previousLocation = locations.get(i - 1);
            Location currentLocation = locations.get(i);
            
            double segmentDistance = calculateEuclideanDistance(
                previousLocation.getLatitude(), previousLocation.getLongitude(),
                currentLocation.getLatitude(), currentLocation.getLongitude()
            );
            
            totalDistance += segmentDistance;
        }
        
        // Cache the calculated distance
        cacheDistance(courierId, totalDistance);
        
        log.debug("Calculated and cached total travel distance for courier {}: {} km", courierId, totalDistance);
        return totalDistance;
    }
    
    private void updateIncrementalDistance(CourierDistance courierDistance, Location newLocation) {
        // Get the previous location by finding locations sorted by timestamp desc, limit 2
        List<Location> recentLocations = locationRepository.findTop2ByCourierIdOrderByTimestampDesc(
            courierDistance.getCourier().getId()
        );
        
        if (recentLocations.size() >= 2) {
            Location previousLocation = recentLocations.get(1); // Second most recent (previous location)
            
            double additionalDistance = calculateEuclideanDistance(
                previousLocation.getLatitude(), previousLocation.getLongitude(),
                newLocation.getLatitude(), newLocation.getLongitude()
            );
            
            courierDistance.setTotalDistance(courierDistance.getTotalDistance() + additionalDistance);
            courierDistanceRepository.save(courierDistance);
            
            log.debug("Updated incremental distance for courier {}: +{} km, total: {} km", 
                courierDistance.getCourier().getId(), additionalDistance, courierDistance.getTotalDistance());
        }
    }
    
    private void initializeCourierDistance(UUID courierId, Location location) {
        Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException("Courier not found: " + courierId));
        
        CourierDistance courierDistance = new CourierDistance();
        courierDistance.setCourier(courier);
        courierDistance.setTotalDistance(0.0);
        
        courierDistanceRepository.save(courierDistance);
        log.debug("Initialized distance tracking for courier {}", courierId);
    }
    
    private void cacheDistance(UUID courierId, Double totalDistance) {
        var existingDistance = courierDistanceRepository.findByCourierId(courierId);
        
        if (existingDistance.isPresent()) {
            CourierDistance courierDistance = existingDistance.get();
            courierDistance.setTotalDistance(totalDistance);
            courierDistanceRepository.save(courierDistance);
        } else {
            Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException("Courier not found: " + courierId));
            
            CourierDistance courierDistance = new CourierDistance();
            courierDistance.setCourier(courier);
            courierDistance.setTotalDistance(totalDistance);
            courierDistanceRepository.save(courierDistance);
        }
    }
    
    /**
     * Simple Euclidean distance calculation for case study purposes
     * Uses basic geometry with lat/lon to meter conversion for Turkey region
     * @return distance in kilometers
     */
    private double calculateEuclideanDistance(double lat1, double lon1, double lat2, double lon2) {
        // Calculate differences in degrees
        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;
        
        // Convert to approximate meters (for Turkey/Istanbul region ~40° latitude)
        double latMeters = deltaLat * 111000; // 1 degree latitude ≈ 111km everywhere
        double lonMeters = deltaLon * 85000;  // 1 degree longitude ≈ 85km at Turkey's latitude
        
        // Apply Euclidean distance formula: √((x2-x1)² + (y2-y1)²)
        double distanceMeters = Math.sqrt(latMeters * latMeters + lonMeters * lonMeters);
        
        // Convert to kilometers
        return distanceMeters / 1000.0;
    }
}