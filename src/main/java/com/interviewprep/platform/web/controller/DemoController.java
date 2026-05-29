package com.interviewprep.platform.web.controller;

import com.interviewprep.platform.service.NPlusOneDemoService;
import com.interviewprep.platform.service.ThreadPoolExhaustionDemoService;
import com.interviewprep.platform.web.dto.UserDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DemoController {
    private final NPlusOneDemoService nPlusOneDemoService;
    private final ThreadPoolExhaustionDemoService threadPoolExhaustionDemoService;

    @GetMapping("/api/demo/public")
    public Map<String, String> publicAccess() {
        return Map.of(
                "message", "Public endpoint: no JWT required",
                "access", "anonymous");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/demo/admin")
    public Map<String, String> adminAccess(Authentication authentication) {
        return Map.of(
                "message", "Admin endpoint: valid JWT with ROLE_ADMIN required",
                "user", authentication.getName());
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/demo/n-plus-one/users-orders")
    public List<UserDtos.UserWithOrdersResponse> usersWithOrdersNPlusOneDemo() {
        return nPlusOneDemoService.getUsersWithOrders();
    }

    @GetMapping("/api/demo/users/{userId}/orders-page")
    public UserDtos.UserOrdersPageResponse userOrdersPaginationDemo(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return nPlusOneDemoService.getUserOrdersPage(userId, pageable);
    }

    @GetMapping("/api/demo/payments/slow")
    public Map<String, String> slowPaymentEndpoint(
            @RequestParam(defaultValue = "2000") long delayMs,
            @RequestParam(defaultValue = "1") int callNumber) throws InterruptedException {
        Thread.sleep(delayMs);
        return Map.of(
                "status", "PAYMENT_AUTHORIZED",
                "callNumber", String.valueOf(callNumber),
                "delayMs", String.valueOf(delayMs),
                "thread", Thread.currentThread().getName());
    }

    
    @GetMapping("/api/demo/thread-pool-exhaustion/payments")
    public ThreadPoolExhaustionDemoService.ThreadPoolDemoResponse threadPoolExhaustionDemo(
            @RequestParam(defaultValue = "5") int calls,
            @RequestParam(defaultValue = "2000") long delayMs) {
        return threadPoolExhaustionDemoService.callSlowPayments(calls, delayMs);
    }
}
