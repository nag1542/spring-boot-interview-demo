package com.interviewprep.platform.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class CacheDemoDtos {

    public record ProductIdRequest(@NotNull Long productId) {
    }

    public record UpdateProductNameRequest(
            @NotNull Long productId,
            @NotBlank String name) {
    }

    public record ProductSearchRequest(
            @NotNull @DecimalMin("0.0") BigDecimal minPrice,
            @NotNull @DecimalMin("0.0") BigDecimal maxPrice) {
    }

    public record ProductNameRequest(@NotBlank String name) {
    }

    public record ProductCachePayload(
            Long productId,
            String name,
            BigDecimal price,
            Integer stock,
            String cacheName,
            Object cacheKey,
            boolean methodExecuted,
            String explanation) {
    }

    public record ProductSearchPayload(
            String cacheName,
            Object cacheKey,
            int productCount,
            List<String> productNames,
            boolean methodExecuted,
            String explanation) {
    }

    public record CacheEvictPayload(
            String cacheName,
            Object cacheKey,
            boolean cacheEntryPresentAfterOperation,
            String explanation) {
    }

    public record NullCachePayload(
            String cacheName,
            Object cacheKey,
            boolean cacheEntryPresentAfterOperation,
            String explanation) {
    }

    public record CacheSelfInvocationPayload(
            Long productId,
            boolean cacheEntryPresentAfterOperation,
            boolean cacheProxyApplied,
            String explanation,
            String solution) {
    }
}
