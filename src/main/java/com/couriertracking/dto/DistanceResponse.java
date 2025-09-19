package com.couriertracking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanceResponse {
    
    private Double distance;
    private String unit;
    
    public static DistanceResponse kilometers(Double distance) {
        return new DistanceResponse(distance, "km");
    }
}