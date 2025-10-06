package com.hermes.config.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    private final ObjectMapper redisObjectMapper;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Redis 직렬화 설정
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        // 기본 캐시 설정 - 30분 TTL
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
            .disableCachingNullValues();

        // 캐시별 개별 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 메일 설정 캐시 - 1시간 TTL
        cacheConfigurations.put("mailSetting", defaultConfig
            .entryTtl(Duration.ofHours(1))
            .prefixCacheNameWith("hermes:cache:"));

        // 메일 설정 값 캐시 - 30분 TTL
        cacheConfigurations.put("mailSettingValue", defaultConfig
            .entryTtl(Duration.ofMinutes(30))
            .prefixCacheNameWith("hermes:cache:"));

        // 메일 템플릿 캐시 - 2시간 TTL
        cacheConfigurations.put("mailTemplate", defaultConfig
            .entryTtl(Duration.ofHours(2))
            .prefixCacheNameWith("hermes:cache:"));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}