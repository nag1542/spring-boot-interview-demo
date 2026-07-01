package com.interviewprep.platform.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AsyncDemoDtos {

    public record PlaceOrderRequest(
            @NotNull Long productId) {
    }

    public record ThreadPoolRequest(
            @Min(1) @Max(50) int tasks) {
    }

    public record AsyncOrderPayload(
            Long orderId,
            String customerEmail,
            String requestThread,
            String notificationThread,
            String message) {
    }

    public record AsyncTrapPayload(
            String scenario,
            String requestThread,
            String observedThread,
            boolean asyncProxyApplied,
            String explanation,
            String solution) {
    }

    public record ThreadPoolPayload(
            String scenario,
            int tasksSubmitted,
            List<String> threadNames,
            String explanation,
            String solution) {
    }
}
