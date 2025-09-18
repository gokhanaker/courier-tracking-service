package com.couriertracking.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateResponse {
    
    private UUID locationId;
    private UUID courierId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private String message;
}
