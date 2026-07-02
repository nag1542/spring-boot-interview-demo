package com.interviewprep.platform.service;

import com.interviewprep.platform.domain.Product;
import com.interviewprep.platform.repository.ProductRepository;
import com.interviewprep.platform.web.dto.CacheDemoDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CacheDemoService {
    private final ProductRepository productRepository;
    private final CacheManager cacheManager;

    @Cacheable(cacheNames = "products", key = "#productId")
    public CacheDemoDtos.ProductCachePayload getProduct(Long productId) {
        Product product = loadProduct(productId);
        System.out.printf("[CACHEABLE-MISS] cache=products key=%s thread=%s%n",
                productId,
                Thread.currentThread().getName());

        return productPayload(
                product,
                "products",
                productId,
                true,
                "Method executed because the value was not present in cache. Repeat the same request to get the cached response.");
    }

    @CachePut(cacheNames = "products", key = "#productId")
    public CacheDemoDtos.ProductCachePayload updateProductName(Long productId, String name) {
        Product product = loadProduct(productId);
        product.setName(name);
        Product saved = productRepository.saveAndFlush(product);
        System.out.printf("[CACHEPUT] cache=products key=%s thread=%s%n",
                productId,
                Thread.currentThread().getName());

        return productPayload(
                saved,
                "products",
                productId,
                true,
                "@CachePut always executes the method and then updates the cache with the returned value.");
    }

    @CacheEvict(cacheNames = "products", key = "#productId")
    public void evictProduct(Long productId) {
        System.out.printf("[CACHEEVICT] cache=products key=%s thread=%s%n",
                productId,
                Thread.currentThread().getName());
    }

    @Cacheable(cacheNames = "productSearch", keyGenerator = "productSearchKeyGenerator")
    public CacheDemoDtos.ProductSearchPayload searchByPrice(BigDecimal minPrice, BigDecimal maxPrice) {
        List<Product> products = productRepository.findAll().stream()
                .filter(product -> product.getPrice().compareTo(minPrice) >= 0)
                .filter(product -> product.getPrice().compareTo(maxPrice) <= 0)
                .toList();
        Object key = "searchByPrice::" + minPrice + ":" + maxPrice;
        System.out.printf("[CACHEABLE-UNIQUE-KEY-MISS] cache=productSearch key=%s thread=%s%n",
                key,
                Thread.currentThread().getName());

        return new CacheDemoDtos.ProductSearchPayload(
                "productSearch",
                key,
                products.size(),
                products.stream().map(Product::getName).toList(),
                true,
                "Custom KeyGenerator creates a stable key from method name and input arguments.");
    }

    @Cacheable(cacheNames = "productsByName", key = "#name.toLowerCase()", unless = "#result == null")
    public CacheDemoDtos.ProductCachePayload findByNameUnlessNull(String name) {
        System.out.printf("[CACHEABLE-UNLESS-CHECK] cache=productsByName key=%s thread=%s%n",
                name.toLowerCase(),
                Thread.currentThread().getName());

        return productRepository.findFirstByNameIgnoreCase(name)
                .map(product -> productPayload(
                        product,
                        "productsByName",
                        name.toLowerCase(),
                        true,
                        "Product was found, so the result is cached. Missing products return null and are not cached because unless = '#result == null'."))
                .orElse(null);
    }

    public CacheDemoDtos.CacheSelfInvocationPayload selfInvocationProblem(Long productId) {
        loadProductWithCacheableAnnotation(productId);

        return new CacheDemoDtos.CacheSelfInvocationPayload(
                productId,
                isCacheEntryPresent("products", productId),
                false,
                "The @Cacheable method was called from another method in the same class, so Spring's cache proxy was bypassed. The console DB log prints on every request.",
                "Move the cached method to another Spring bean or call it through a proxy-invoked service boundary.");
    }

    @Cacheable(cacheNames = "products", key = "#productId")
    public CacheDemoDtos.ProductCachePayload loadProductWithCacheableAnnotation(Long productId) {
        Product product = loadProduct(productId);
        System.out.printf("[CACHE-SELF-INVOCATION-DB-CALL] cache=products key=%s thread=%s%n",
                productId,
                Thread.currentThread().getName());

        return productPayload(
                product,
                "products",
                productId,
                true,
                "This method has @Cacheable, but self-invocation prevents Spring from applying it.");
    }

    @CacheEvict(cacheNames = "products", key = "#productId", beforeInvocation = true)
    public void evictBeforeInvocationAndFail(Long productId) {
        System.out.printf("[CACHEEVICT-BEFORE-INVOCATION] cache=products key=%s thread=%s%n",
                productId,
                Thread.currentThread().getName());
        throw new IllegalStateException("Simulated failure after cache eviction was already performed.");
    }

    public CacheDemoDtos.CacheEvictPayload evictResult(Long productId, String explanation) {
        return new CacheDemoDtos.CacheEvictPayload(
                "products",
                productId,
                isCacheEntryPresent("products", productId),
                explanation);
    }

    public boolean isCacheEntryPresent(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        return cache != null && cache.get(key) != null;
    }

    private Product loadProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product was not found"));
    }

    public CacheDemoDtos.ProductCachePayload productPayload(
            Product product,
            String cacheName,
            Object cacheKey,
            boolean methodExecuted,
            String explanation) {
        return new CacheDemoDtos.ProductCachePayload(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                cacheName,
                cacheKey,
                methodExecuted,
                explanation);
    }
}
