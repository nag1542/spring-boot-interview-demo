package com.interviewprep.platform.web.dto;

import com.interviewprep.platform.domain.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ProductDtos {
    public record ProductRequest(
            @NotBlank String name,
            @NotNull @DecimalMin("0.0") BigDecimal price,
            @NotNull @Min(0) Integer stock) {}

    public record ProductResponse(Long id, String name, BigDecimal price, Integer stock) {
        public static ProductResponse from(Product product) {
            return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getStock());
        }
    }
}
