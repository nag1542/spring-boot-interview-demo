package com.interviewprep.platform.web.controller;
import com.interviewprep.platform.domain.Order;
import com.interviewprep.platform.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController @RequiredArgsConstructor
public class OrderController {
    private final OrderRepository orderRepository;
    @PostMapping("/api/orders") public Order create(@RequestBody Order order){ order.setCreatedAt(Instant.now()); return orderRepository.save(order); }
    @GetMapping("/api/orders/{id}") public Order byId(@PathVariable Long id){ return orderRepository.findById(id).orElseThrow(); }
}
