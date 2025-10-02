package com.couriertracking.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.couriertracking.model.Courier;
import com.couriertracking.model.Store;
import com.couriertracking.model.StoreEntrance;

@Repository
public interface StoreEntranceRepository extends JpaRepository<StoreEntrance, UUID> {

     // Check if a courier has entered a specific store within time threshold
     boolean existsByCourierAndStoreAndEntranceTimeAfter(
        Courier courier, 
        Store store, 
        LocalDateTime timeThreshold
    );
}
