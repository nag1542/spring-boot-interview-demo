package com.interviewprep.platform.web.controller;

import com.interviewprep.platform.domain.Product;
import com.interviewprep.platform.repository.ProductRepository;
import com.interviewprep.platform.web.dto.ProductDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductRepository productRepository;

    @GetMapping("/api/products")
    public List<ProductDtos.ProductResponse> list() {
        return productRepository.findAll().stream().map(ProductDtos.ProductResponse::from).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/api/admin/products")
    public ProductDtos.ProductResponse create(@Valid @RequestBody ProductDtos.ProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .stock(request.stock())
                .build();
        return ProductDtos.ProductResponse.from(productRepository.save(product));
    }
}
