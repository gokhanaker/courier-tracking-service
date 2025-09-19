package com.couriertracking.repository;

import com.couriertracking.model.Courier;
import com.couriertracking.model.CourierDistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourierDistanceRepository extends JpaRepository<CourierDistance, UUID> {
    
    Optional<CourierDistance> findByCourierId(UUID courierId);
    
    @Query("SELECT cd FROM CourierDistance cd WHERE cd.courier = :courier")
    Optional<CourierDistance> findByCourier(@Param("courier") Courier courier);
    
    boolean existsByCourierId(UUID courierId);
}