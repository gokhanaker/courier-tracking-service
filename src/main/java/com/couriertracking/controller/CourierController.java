package com.couriertracking.controller;

import com.couriertracking.service.DistanceCalculationService;
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
    
    private final DistanceCalculationService distanceCalculationService;
    
    @GetMapping("/{courierId}/total-travel-distance")
    public ResponseEntity<Double> getTotalTravelDistance(@PathVariable UUID courierId) {
        
        log.info("Getting total travel distance for courier: {}", courierId);
        
        Double totalDistance = distanceCalculationService.getTotalTravelDistance(courierId);
        return ResponseEntity.ok(totalDistance);
    }
}