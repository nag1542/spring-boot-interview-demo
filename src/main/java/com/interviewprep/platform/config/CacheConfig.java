package com.interviewprep.platform.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "products",
                "productSearch",
                "productsByName");
    }

    @Bean
    public KeyGenerator productSearchKeyGenerator() {
        return (Object target, Method method, Object... params) -> method.getName() + "::" +
                Arrays.stream(params)
                        .map(String::valueOf)
                        .collect(Collectors.joining(":"));
    }
}
