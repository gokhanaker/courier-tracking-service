package com.couriertracking.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.couriertracking.model.Courier;
import com.couriertracking.model.Store;
import com.couriertracking.model.StoreEntrance;

@Repository
public interface StoreEntranceRepository extends JpaRepository<StoreEntrance, UUID> {
     boolean existsByCourierAndStoreAndEntranceTimeAfter(
        Courier courier, 
        Store store, 
        LocalDateTime timeThreshold
    );
    
    @Query("SELECT COUNT(se) > 0 FROM StoreEntrance se WHERE se.courier.id = :courierId " +
           "AND se.store.id = :storeId AND se.entranceTime > :timeThreshold")
    boolean existsRecentEntrance(@Param("courierId") UUID courierId, 
                                @Param("storeId") UUID storeId, 
                                @Param("timeThreshold") LocalDateTime timeThreshold);
}
