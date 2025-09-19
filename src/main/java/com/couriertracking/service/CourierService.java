package com.couriertracking.service;

import com.couriertracking.dto.CourierCreateRequest;
import com.couriertracking.dto.CourierResponse;
import com.couriertracking.exception.CourierNotFoundException;
import com.couriertracking.model.Courier;
import com.couriertracking.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourierService {
    
    private final CourierRepository courierRepository;
    
    public CourierResponse createCourier(CourierCreateRequest request) {        
        Courier courier = new Courier();
        courier.setName(request.getName());
        courier.setEmail(request.getEmail());
        courier.setPhoneNumber(request.getPhoneNumber());
        
        Courier savedCourier = courierRepository.save(courier);
        
        log.info("Successfully created courier with ID: {}", savedCourier.getId());
        return mapToResponse(savedCourier);
    }
    
    public CourierResponse getCourierById(UUID courierId) {        
        Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException("Courier not found with ID: " + courierId));
        
        log.info("Retrieved courier with ID: {}", courierId);
        return mapToResponse(courier);
    }
    
    private CourierResponse mapToResponse(Courier courier) {
        return new CourierResponse(
            courier.getId(),
            courier.getName(),
            courier.getEmail(),
            courier.getPhoneNumber(),
            courier.getCreatedAt(),
            courier.getUpdatedAt()
        );
    }
}