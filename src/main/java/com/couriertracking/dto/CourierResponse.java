package com.couriertracking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourierResponse {
    
    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;
}