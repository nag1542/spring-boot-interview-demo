package com.interviewprep.platform.web.controller;

import com.interviewprep.platform.service.AsyncOrderDemoService;
import com.interviewprep.platform.web.dto.ApiResponse;
import com.interviewprep.platform.web.dto.AsyncDemoDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo/async-internals")
@RequiredArgsConstructor
public class AsyncInternalsDemoController {
    private final AsyncOrderDemoService asyncOrderDemoService;

    @PostMapping("/place-order-notify")
    public ApiResponse<AsyncDemoDtos.AsyncOrderPayload> placeOrderAndNotify(
            Authentication authentication,
            @Valid @RequestBody AsyncDemoDtos.PlaceOrderRequest request) {
        return ApiResponse.success(
                asyncOrderDemoService.placeOrderAndNotify(authentication.getName(), request.productId()));
    }

    @PostMapping("/self-invocation")
    public ApiResponse<AsyncDemoDtos.AsyncTrapPayload> selfInvocation(
            Authentication authentication,
            @Valid @RequestBody AsyncDemoDtos.PlaceOrderRequest request) {
        return ApiResponse.success(
                asyncOrderDemoService.selfInvocationProblem(authentication.getName(), request.productId()));
    }

    @PostMapping("/exception-handler")
    public ApiResponse<AsyncDemoDtos.AsyncOrderPayload> exceptionHandler(
            Authentication authentication,
            @Valid @RequestBody AsyncDemoDtos.PlaceOrderRequest request) {
        return ApiResponse.success(
                asyncOrderDemoService.exceptionHandlerDemo(authentication.getName(), request.productId()));
    }

    @PostMapping("/exception-completable-future")
    public ApiResponse<AsyncDemoDtos.AsyncOrderPayload> exceptionWithCompletableFuture(
            Authentication authentication,
            @Valid @RequestBody AsyncDemoDtos.PlaceOrderRequest request) {
        return ApiResponse.success(
                asyncOrderDemoService.completableFutureExceptionDemo(authentication.getName(), request.productId()));
    }

    @PostMapping("/threadpool/default-issue")
    public ApiResponse<AsyncDemoDtos.ThreadPoolPayload> defaultThreadPoolIssue(
            @Valid @RequestBody AsyncDemoDtos.ThreadPoolRequest request) {
        return ApiResponse.success(asyncOrderDemoService.simpleExecutorThreadPoolIssue(request.tasks()));
    }

    @PostMapping("/threadpool/custom")
    public ApiResponse<AsyncDemoDtos.ThreadPoolPayload> customThreadPool(
            @Valid @RequestBody AsyncDemoDtos.ThreadPoolRequest request) {
        return ApiResponse.success(asyncOrderDemoService.customThreadPool(request.tasks()));
    }
}
