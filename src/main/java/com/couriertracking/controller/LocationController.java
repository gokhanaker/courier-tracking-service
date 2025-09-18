package com.couriertracking.controller;

import com.couriertracking.dto.LocationUpdateRequest;
import com.couriertracking.dto.LocationUpdateResponse;
import com.couriertracking.service.LocationTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {
    
    private final LocationTrackingService locationTrackingService;
    
    @PostMapping
    public ResponseEntity<LocationUpdateResponse> updateLocation(
            @Valid @RequestBody LocationUpdateRequest request) {
                
        LocationUpdateResponse response = locationTrackingService.updateCourierLocation(request);
        return ResponseEntity.ok(response);
    }
}