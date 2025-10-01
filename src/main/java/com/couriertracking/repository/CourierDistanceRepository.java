package com.couriertracking.repository;

import com.couriertracking.model.CourierDistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourierDistanceRepository extends JpaRepository<CourierDistance, UUID> {
    
    Optional<CourierDistance> findByCourierId(UUID courierId);
}