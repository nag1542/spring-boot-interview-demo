package com.interviewprep.platform.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncSecurityDemoService {

    @Async
    public CompletableFuture<Map<String, String>> authenticatedUserFromAsyncThread() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication == null ? "anonymous" : authentication.getName();
        String threadName = Thread.currentThread().getName();

        return CompletableFuture.completedFuture(Map.of(
                "username", username,
                "thread", threadName
        ));
    }
}
