package com.couriertracking.service;

import com.couriertracking.dto.StoreDto;
import com.couriertracking.model.Store;
import com.couriertracking.repository.StoreRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreDataLoaderService implements CommandLineRunner {

    private final StoreRepository storeRepository;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @Value("${courier-tracking.store.data-file}")
    private String storeDataFile;

    @Override
    public void run(String... args) throws Exception {
        loadStoreData();
    }

    private void loadStoreData() {
        try {
            // Check if stores already exist
            if (storeRepository.count() > 0) {
                log.info("Store data already exists. Skipping data load.");
                return;
            }

            log.info("Loading store data from: {}", storeDataFile);

            Resource resource = resourceLoader.getResource(storeDataFile);
            InputStream inputStream = resource.getInputStream();

            List<StoreDto> storeDtos = objectMapper.readValue(
                inputStream, 
                new TypeReference<List<StoreDto>>() {}
            );

            List<Store> stores = storeDtos.stream()
                .map(this::convertToStore)
                .toList();

            storeRepository.saveAll(stores);
            
            log.info("Successfully loaded {} stores into database", stores.size());

        } catch (IOException e) {
            log.error("Failed to load store data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize store data", e);
        }
    }

    private Store convertToStore(StoreDto dto) {
        Store store = new Store();
        store.setName(dto.getName());
        store.setLatitude(dto.getLat());
        store.setLongitude(dto.getLng());
        return store;
    }
}