package com.interviewprep.platform.web.controller;

import com.interviewprep.platform.domain.Order;
import com.interviewprep.platform.repository.OrderRepository;
import com.interviewprep.platform.web.dto.OrderDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderRepository orderRepository;

    @PostMapping("/api/orders")
    public OrderDtos.OrderResponse create(@Valid @RequestBody OrderDtos.OrderRequest request) {
        Order order = Order.builder()
                .customerEmail(request.customerEmail())
                .totalAmount(request.totalAmount())
                .createdAt(Instant.now())
                .build();
        return OrderDtos.OrderResponse.from(orderRepository.save(order));
    }

    @GetMapping("/api/orders/{id}")
    public OrderDtos.OrderResponse byId(@PathVariable Long id) {
        return orderRepository.findById(id).map(OrderDtos.OrderResponse::from).orElseThrow();
    }
}
