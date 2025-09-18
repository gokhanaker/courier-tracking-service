package com.couriertracking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import com.couriertracking.model.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {
}