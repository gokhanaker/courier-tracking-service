package com.couriertracking.controller;

import com.couriertracking.dto.CourierCreateRequest;
import com.couriertracking.dto.CourierResponse;
import com.couriertracking.service.CourierService;
import com.couriertracking.service.DistanceCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/couriers")
@RequiredArgsConstructor
@Slf4j
public class CourierController {
    
    private final DistanceCalculationService distanceCalculationService;
    private final CourierService courierService;
    
    @PostMapping
    public ResponseEntity<CourierResponse> createCourier(
            @Valid @RequestBody CourierCreateRequest request) {
        
        CourierResponse response = courierService.createCourier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{courierId}")
    public ResponseEntity<CourierResponse> getCourierById(@PathVariable UUID courierId) {
        CourierResponse courier = courierService.getCourierById(courierId);
        return ResponseEntity.ok(courier);
    }
    
    
    @GetMapping("/{courierId}/total-travel-distance")
    public ResponseEntity<Double> getTotalTravelDistance(@PathVariable UUID courierId) {
                
        Double totalDistance = distanceCalculationService.getTotalTravelDistance(courierId);
        return ResponseEntity.ok(totalDistance);
    }
}