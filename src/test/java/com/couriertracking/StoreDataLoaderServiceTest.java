package com.couriertracking;

import com.couriertracking.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StoreDataLoaderServiceTest {

    @Autowired
    private StoreRepository storeRepository;

    @Test
    void shouldLoadStoresOnStartup() {
        // Given/When - Application starts and CommandLineRunner executes
        
        // Then
        assertEquals(5, storeRepository.count());
        
        // Verify specific store exists
        assertTrue(storeRepository.findAll().stream()
            .anyMatch(store -> "Ataşehir MMM Migros".equals(store.getName())));
    }
}