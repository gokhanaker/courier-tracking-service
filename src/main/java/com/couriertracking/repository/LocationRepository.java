package com.couriertracking.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.couriertracking.model.Location;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    @Query("SELECT l FROM Location l WHERE l.courier.id = :courierId ORDER BY l.timestamp DESC LIMIT 2")
    List<Location> findTop2ByCourierIdOrderByTimestampDesc(@Param("courierId") UUID courierId);
}
