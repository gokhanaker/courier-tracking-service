package com.couriertracking.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration class for setting up Redis connection and templates.
 * Provides StringRedisTemplate bean for cache operations.
 */
@Configuration
public class RedisConfig {

    /**
     * Creates a StringRedisTemplate bean for string-based Redis operations.
     * This template is used by CourierDistanceCache for storing courier distances.
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        
        // Set serializers for consistent key-value serialization
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        
        return template;
    }

    /**
     * Creates a generic RedisTemplate bean for more complex Redis operations.
     * This can be used for future Redis operations that require different serialization.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use string serializers for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        // Use default serializer for values
        template.setDefaultSerializer(new StringRedisSerializer());
        
        return template;
    }
}
