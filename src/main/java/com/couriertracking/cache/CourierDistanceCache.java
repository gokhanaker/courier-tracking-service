package com.couriertracking.cache;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourierDistanceCache {

    private final StringRedisTemplate redis;
    private static final String DISTANCE_KEY_PREFIX = "courier:distance:";
    private static final long CACHE_TTL_HOURS = 24; // Cache expiry time in hours

    private String key(UUID courierId) {
        return DISTANCE_KEY_PREFIX + courierId.toString();
    }

    public Optional<Double> getFromCache(UUID courierId) {
        try {
            String v = redis.opsForValue().get(key(courierId));
            return v == null ? Optional.empty() : Optional.of(Double.parseDouble(v));
        } catch (Exception e) {
            log.error("Error retrieving distance from cache for courier {}: {}", courierId, e.getMessage());
            return Optional.empty();
        }
    }

    public void saveToCache(UUID courierId, Double distance) {
        try {
            redis.opsForValue().set(key(courierId), distance.toString());
            redis.expire(key(courierId), java.time.Duration.ofHours(CACHE_TTL_HOURS));
        } catch (Exception e) {
            log.error("Error saving distance to cache for courier {}: {}", courierId, e.getMessage());
        }
    }

    public void evictFromCache(UUID courierId) {
        try {
            redis.delete(key(courierId));
        } catch (Exception e) {
            log.error("Error evicting distance from cache for courier {}: {}", courierId, e.getMessage());
        }
    }

}
