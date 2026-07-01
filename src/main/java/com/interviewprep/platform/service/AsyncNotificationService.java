package com.interviewprep.platform.service;

import com.interviewprep.platform.domain.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncNotificationService {

    @Async("notificationExecutor")
    public CompletableFuture<String> sendOrderNotification(Order order) {
        String threadName = Thread.currentThread().getName();
        System.out.printf(
                "[ASYNC-NOTIFICATION] orderId=%s customer=%s thread=%s%n",
                order.getId(),
                order.getCustomerEmail(),
                threadName);

        return CompletableFuture.completedFuture(threadName);
    }

    @Async("notificationExecutor")
    public void sendNotificationAndFail(Long orderId) {
        System.out.printf(
                "[ASYNC-VOID-FAILURE] orderId=%s thread=%s%n",
                orderId,
                Thread.currentThread().getName());

        throw new IllegalStateException("Notification provider failed for order " + orderId);
    }

    @Async("notificationExecutor")
    public CompletableFuture<String> sendNotificationAndFailWithFuture(Long orderId) {
        String threadName = Thread.currentThread().getName();
        System.out.printf("[ASYNC-FUTURE-FAILURE] orderId=%s thread=%s%n", orderId, threadName);

        throw new IllegalStateException("Notification provider failed for order " + orderId);
    }

    @Async("unboundedAsyncDemoExecutor")
    public CompletableFuture<String> runWithSimpleExecutor(int taskNumber) {
        String threadName = Thread.currentThread().getName();
        System.out.printf("[ASYNC-SIMPLE-EXECUTOR] task=%d thread=%s%n", taskNumber, threadName);
        return CompletableFuture.completedFuture(threadName);
    }

    @Async("notificationExecutor")
    public CompletableFuture<String> runWithNotificationExecutor(int taskNumber) {
        String threadName = Thread.currentThread().getName();
        System.out.printf("[ASYNC-NOTIFICATION-EXECUTOR] task=%d thread=%s%n", taskNumber, threadName);
        return CompletableFuture.completedFuture(threadName);
    }
}
