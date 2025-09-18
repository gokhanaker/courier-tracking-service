package com.couriertracking.controller;

import com.couriertracking.service.DistanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/couriers")
@RequiredArgsConstructor
@Slf4j
public class CourierController {
    
    private final DistanceService distanceService;
    
    @GetMapping("/{courierId}/total-travel-distance")
    public ResponseEntity<Double> getTotalTravelDistance(@PathVariable UUID courierId) {
        
        log.info("Getting total travel distance for courier: {}", courierId);
        
        try {
            Double totalDistance = distanceService.getTotalTravelDistance(courierId);
            return ResponseEntity.ok(totalDistance);
            
        } catch (RuntimeException e) {
            log.error("Error calculating distance for courier {}: {}", courierId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}