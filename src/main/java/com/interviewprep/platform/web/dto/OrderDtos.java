package com.interviewprep.platform.web.dto;

import com.interviewprep.platform.domain.Order;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderDtos {
    public record OrderRequest(
            @Email String customerEmail,
            @NotNull @DecimalMin("0.0") BigDecimal totalAmount) {}

    public record OrderResponse(Long id, String customerEmail, BigDecimal totalAmount, Instant createdAt) {
        public static OrderResponse from(Order order) {
            return new OrderResponse(order.getId(), order.getCustomerEmail(), order.getTotalAmount(), order.getCreatedAt());
        }
    }
}
