package com.couriertracking.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreEntranceResponse {
    private UUID locationId;
    private UUID courierId;
    private Double latitude;
    private Double longitude;
    private UUID storeId;
    private String storeName;
    private LocalDateTime entranceTime;
}
