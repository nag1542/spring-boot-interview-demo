package com.interviewprep.platform.web.controller;

import com.interviewprep.platform.service.CacheDemoService;
import com.interviewprep.platform.web.dto.ApiResponse;
import com.interviewprep.platform.web.dto.CacheDemoDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo/cache")
@RequiredArgsConstructor
public class CacheDemoController {
    private final CacheDemoService cacheDemoService;

    @PostMapping("/cacheable")
    public ApiResponse<CacheDemoDtos.ProductCachePayload> cacheable(
            @Valid @RequestBody CacheDemoDtos.ProductIdRequest request) {
        boolean cachedBeforeCall = cacheDemoService.isCacheEntryPresent("products", request.productId());
        CacheDemoDtos.ProductCachePayload payload = cacheDemoService.getProduct(request.productId());

        return ApiResponse.success(new CacheDemoDtos.ProductCachePayload(
                payload.productId(),
                payload.name(),
                payload.price(),
                payload.stock(),
                payload.cacheName(),
                payload.cacheKey(),
                !cachedBeforeCall,
                cachedBeforeCall
                        ? "Cache hit. The @Cacheable method body did not execute for this request."
                        : "Cache miss. The @Cacheable method body executed and stored the response."));
    }

    @PostMapping("/cache-put")
    public ApiResponse<CacheDemoDtos.ProductCachePayload> cachePut(
            @Valid @RequestBody CacheDemoDtos.UpdateProductNameRequest request) {
        return ApiResponse.success(cacheDemoService.updateProductName(request.productId(), request.name()));
    }

    @PostMapping("/cache-evict")
    public ApiResponse<CacheDemoDtos.CacheEvictPayload> cacheEvict(
            @Valid @RequestBody CacheDemoDtos.ProductIdRequest request) {
        cacheDemoService.evictProduct(request.productId());
        return ApiResponse.success(cacheDemoService.evictResult(
                request.productId(),
                "@CacheEvict removed the product entry after the method completed successfully."));
    }

    @PostMapping("/unique-key")
    public ApiResponse<CacheDemoDtos.ProductSearchPayload> uniqueKey(
            @Valid @RequestBody CacheDemoDtos.ProductSearchRequest request) {
        Object key = "searchByPrice::" + request.minPrice() + ":" + request.maxPrice();
        boolean cachedBeforeCall = cacheDemoService.isCacheEntryPresent("productSearch", key);
        CacheDemoDtos.ProductSearchPayload payload =
                cacheDemoService.searchByPrice(request.minPrice(), request.maxPrice());

        return ApiResponse.success(new CacheDemoDtos.ProductSearchPayload(
                payload.cacheName(),
                payload.cacheKey(),
                payload.productCount(),
                payload.productNames(),
                !cachedBeforeCall,
                cachedBeforeCall
                        ? "Cache hit using the custom generated key."
                        : "Cache miss. Custom KeyGenerator created a unique key from method name and arguments."));
    }

    @PostMapping("/unless-null")
    public ApiResponse<Object> unlessNull(@Valid @RequestBody CacheDemoDtos.ProductNameRequest request) {
        String key = request.name().toLowerCase();
        CacheDemoDtos.ProductCachePayload payload = cacheDemoService.findByNameUnlessNull(request.name());
        if (payload == null) {
            return ApiResponse.success(new CacheDemoDtos.NullCachePayload(
                    "productsByName",
                    key,
                    cacheDemoService.isCacheEntryPresent("productsByName", key),
                    "Product was not found. Because unless = '#result == null', the null result was not cached."));
        }

        return ApiResponse.success(payload);
    }

    @PostMapping("/self-invocation")
    public ApiResponse<CacheDemoDtos.CacheSelfInvocationPayload> selfInvocation(
            @Valid @RequestBody CacheDemoDtos.ProductIdRequest request) {
        return ApiResponse.success(cacheDemoService.selfInvocationProblem(request.productId()));
    }

    @PostMapping("/evict-before-invocation")
    public ApiResponse<CacheDemoDtos.CacheEvictPayload> evictBeforeInvocation(
            @Valid @RequestBody CacheDemoDtos.ProductIdRequest request) {
        try {
            cacheDemoService.evictBeforeInvocationAndFail(request.productId());
            throw new IllegalStateException("Expected cache eviction failure did not happen");
        } catch (RuntimeException ex) {
            return ApiResponse.success(cacheDemoService.evictResult(
                    request.productId(),
                    "@CacheEvict(beforeInvocation = true) removed the cache entry even though the method failed."));
        }
    }
}
