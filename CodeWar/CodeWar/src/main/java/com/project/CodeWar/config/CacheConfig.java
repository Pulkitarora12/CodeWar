package com.project.CodeWar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        GenericJacksonJsonRedisSerializer jsonSerializer =
                GenericJacksonJsonRedisSerializer.builder()
                        .enableUnsafeDefaultTyping()
                        .build();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                );

        Map<String, RedisCacheConfiguration> initialCacheConfigs = new HashMap<>();
        initialCacheConfigs.put("users", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        initialCacheConfigs.put("userDTOs", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        initialCacheConfigs.put("rooms", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        initialCacheConfigs.put("roles", defaultConfig.entryTtl(Duration.ofHours(24)));
        initialCacheConfigs.put("cfRatings", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        initialCacheConfigs.put("cfProblemset", defaultConfig.entryTtl(Duration.ofHours(12)));
        initialCacheConfigs.put("contests", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        initialCacheConfigs.put("leaderboards", defaultConfig.entryTtl(Duration.ofMinutes(2)));
        initialCacheConfigs.put("scores", defaultConfig.entryTtl(Duration.ofMinutes(2)));
        initialCacheConfigs.put("submissions", defaultConfig.entryTtl(Duration.ofMinutes(2)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(initialCacheConfigs)
                .build();
    }
}