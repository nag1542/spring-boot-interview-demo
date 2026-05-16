package com.interviewprep.platform.web.controller;
import com.interviewprep.platform.domain.Product;
import com.interviewprep.platform.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
public class ProductController {
    private final ProductRepository productRepository;
    @GetMapping("/api/products") public Object list(){ return productRepository.findAll(); }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/api/admin/products") public Product create(@RequestBody Product product){ return productRepository.save(product); }
}
