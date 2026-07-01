package com.interviewprep.platform.service;

import com.interviewprep.platform.domain.Order;
import com.interviewprep.platform.domain.Product;
import com.interviewprep.platform.domain.User;
import com.interviewprep.platform.repository.OrderRepository;
import com.interviewprep.platform.repository.ProductRepository;
import com.interviewprep.platform.repository.UserRepository;
import com.interviewprep.platform.web.dto.AsyncDemoDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AsyncOrderDemoService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AsyncNotificationService asyncNotificationService;

    public AsyncDemoDtos.AsyncOrderPayload placeOrderAndNotify(String userEmail, Long productId) {
        String requestThread = Thread.currentThread().getName();
        Order order = placeOrder(userEmail, productId);

        CompletableFuture<String> notificationThread = asyncNotificationService.sendOrderNotification(order);

        return new AsyncDemoDtos.AsyncOrderPayload(
                order.getId(),
                order.getCustomerEmail(),
                requestThread,
                notificationThread.join(),
                "Order was saved on the request thread, and notification ran on the custom notification executor.");
    }

    public AsyncDemoDtos.AsyncTrapPayload selfInvocationProblem(String userEmail, Long productId) {
        String requestThread = Thread.currentThread().getName();
        Order order = placeOrder(userEmail, productId);
        String observedThread = sendNotificationInsideSameClass(order).join();

        return new AsyncDemoDtos.AsyncTrapPayload(
                "async-self-invocation",
                requestThread,
                observedThread,
                !requestThread.equals(observedThread),
                "The @Async method was called through this object, so Spring's async proxy was bypassed.",
                "Move the async method to another Spring bean and call that bean, as shown by AsyncNotificationService.");
    }

    @Async("notificationExecutor")
    public CompletableFuture<String> sendNotificationInsideSameClass(Order order) {
        String threadName = Thread.currentThread().getName();
        System.out.printf(
                "[ASYNC-SELF-INVOCATION] orderId=%s customer=%s thread=%s%n",
                order.getId(),
                order.getCustomerEmail(),
                threadName);

        return CompletableFuture.completedFuture(threadName);
    }

    public AsyncDemoDtos.AsyncOrderPayload exceptionHandlerDemo(String userEmail, Long productId) {
        String requestThread = Thread.currentThread().getName();
        Order order = placeOrder(userEmail, productId);

        asyncNotificationService.sendNotificationAndFail(order.getId());

        return new AsyncDemoDtos.AsyncOrderPayload(
                order.getId(),
                order.getCustomerEmail(),
                requestThread,
                "See console: AsyncUncaughtExceptionHandler prints the async failure",
                "Void @Async exceptions do not return to the caller. AsyncUncaughtExceptionHandler handles them centrally.");
    }

    public AsyncDemoDtos.AsyncOrderPayload completableFutureExceptionDemo(String userEmail, Long productId) {
        String requestThread = Thread.currentThread().getName();
        Order order = placeOrder(userEmail, productId);

        try {
            asyncNotificationService.sendNotificationAndFailWithFuture(order.getId()).join();
            throw new IllegalStateException("Expected notification failure did not happen");
        } catch (CompletionException ex) {
            return new AsyncDemoDtos.AsyncOrderPayload(
                    order.getId(),
                    order.getCustomerEmail(),
                    requestThread,
                    "Observed through CompletableFuture: " + ex.getCause().getMessage(),
                    "CompletableFuture keeps the async exception in the future, so callers can handle it with join/get/exceptionally/handle.");
        }
    }

    public AsyncDemoDtos.ThreadPoolPayload simpleExecutorThreadPoolIssue(int tasks) {
        List<CompletableFuture<String>> futures = IntStream.rangeClosed(1, tasks)
                .mapToObj(asyncNotificationService::runWithSimpleExecutor)
                .toList();

        List<String> threads = futures.stream()
                .map(CompletableFuture::join)
                .distinct()
                .toList();

        return new AsyncDemoDtos.ThreadPoolPayload(
                "simple-async-executor-thread-growth",
                tasks,
                threads,
                "SimpleAsyncTaskExecutor creates new threads instead of reusing a bounded pool, which can exhaust resources under load.",
                "Use a bounded ThreadPoolTaskExecutor with core size, max size, queue capacity, and clear thread names.");
    }

    public AsyncDemoDtos.ThreadPoolPayload customThreadPool(int tasks) {
        List<CompletableFuture<String>> futures = IntStream.rangeClosed(1, tasks)
                .mapToObj(asyncNotificationService::runWithNotificationExecutor)
                .toList();

        List<String> threads = futures.stream()
                .map(CompletableFuture::join)
                .distinct()
                .toList();

        return new AsyncDemoDtos.ThreadPoolPayload(
                "bounded-notification-executor",
                tasks,
                threads,
                "The custom notification executor reuses a bounded pool with names like notification-async-1.",
                "Define dedicated executors for important async workloads instead of relying on defaults.");
    }

    private Order placeOrder(String userEmail, Long productId) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product was not found"));

        if (product.getStock() <= 0) {
            throw new IllegalArgumentException("Product is out of stock");
        }

        product.setStock(product.getStock() - 1);
        productRepository.saveAndFlush(product);

        return orderRepository.saveAndFlush(Order.builder()
                .customerEmail(user.getEmail())
                .totalAmount(product.getPrice())
                .createdAt(Instant.now())
                .user(user)
                .build());
    }
}
