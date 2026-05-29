package com.interviewprep.platform.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

@Service
public class ThreadPoolExhaustionDemoService {
    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final Executor paymentDemoExecutor;

    @Value("${app.demo.payment-base-url:http://localhost:8080}")
    private String paymentBaseUrl;

    public ThreadPoolExhaustionDemoService(
            RestTemplate restTemplate,
            WebClient webClient,
            @Qualifier("paymentDemoExecutor") Executor paymentDemoExecutor) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
        this.paymentDemoExecutor = paymentDemoExecutor;
    }

    public ThreadPoolDemoResponse callSlowPayments(int calls, long delayMs) {
        String url = paymentBaseUrl + "/api/demo/payments/slow?delayMs=" + delayMs;
        Instant startedAt = Instant.now();

        /*
         * PROBLEM: RestTemplate blocks the request thread.
         *
         * Every outbound payment call holds a servlet thread until the remote service responds.
         * Under load, enough blocked servlet threads can exhaust the embedded Tomcat request pool.
         */
        List<String> responses = IntStream.rangeClosed(1, calls)
                .mapToObj(callNumber -> restTemplate.getForObject(url + "&callNumber=" + callNumber, String.class))
                .toList();

        /*
         * SOLUTION 1: CompletableFuture with a bounded executor.
         *
         * Uncomment this block and comment the PROBLEM block above.
         * This keeps slow payment IO off the servlet request thread and limits concurrency with a dedicated pool.
         */
        // List<CompletableFuture<String>> futures = IntStream.rangeClosed(1, calls)
        //         .mapToObj(callNumber -> CompletableFuture.supplyAsync(
        //                 () -> restTemplate.getForObject(url + "&callNumber=" + callNumber, String.class),
        //                 paymentDemoExecutor))
        //         .toList();
        // List<String> responses = futures.stream()
        //         .map(CompletableFuture::join)
        //         .toList();

        /*
         * SOLUTION 2: WebClient.
         *
         * Uncomment this block and comment the PROBLEM block above.
         * WebClient uses non-blocking IO for the outbound calls. The final block() is kept here only because
         * this MVC endpoint returns a normal response object. In a reactive controller, return Mono/Flux directly.
         */
        // List<CompletableFuture<String>> futures = IntStream.rangeClosed(1, calls)
        //         .mapToObj(callNumber -> webClient.get()
        //                 .uri(url + "&callNumber=" + callNumber)
        //                 .retrieve()
        //                 .bodyToMono(String.class)
        //                 .timeout(Duration.ofSeconds(10))
        //                 .toFuture())
        //         .toList();
        // List<String> responses = futures.stream()
        //         .map(CompletableFuture::join)
        //         .toList();

        long durationMs = Duration.between(startedAt, Instant.now()).toMillis();
        return new ThreadPoolDemoResponse(calls, delayMs, durationMs, Thread.currentThread().getName(), responses);
    }

    public record ThreadPoolDemoResponse(
            int calls,
            long paymentDelayMs,
            long totalDurationMs,
            String handledByThread,
            List<String> paymentResponses) {
    }
}
