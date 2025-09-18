package com.couriertracking.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.couriertracking.model.Courier;

@Repository
public interface CourierRepository extends JpaRepository<Courier, UUID> {
}
