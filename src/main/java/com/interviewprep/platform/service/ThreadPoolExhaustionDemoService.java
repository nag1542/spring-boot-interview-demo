package com.interviewprep.platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Service
public class ThreadPoolExhaustionDemoService {
    private final RestTemplate restTemplate;
    private final WebClient webClient;

    @Value("${app.demo.payment-base-url:http://localhost:8080}")
    private String paymentBaseUrl;

    public ThreadPoolExhaustionDemoService(
            RestTemplate restTemplate,
            WebClient webClient) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
    }

    public ThreadPoolDemoResponse callSlowPaymentWithRestTemplate(long delayMs, String requestThread) {
        Instant startedAt = Instant.now();

        /*
         * PROBLEM: RestTemplate blocks the request thread.
         *
         * The same Tomcat request thread that entered the controller waits here until the payment call finishes.
         * With server.tomcat.threads.max=5 and 10 concurrent Postman users, the extra requests wait for a free thread.
         */
        String paymentResponse = restTemplate.getForObject(paymentUrl(delayMs), String.class);

        return buildResponse("REST_TEMPLATE_BLOCKING", delayMs, startedAt, requestThread, paymentResponse);
    }

    public CompletableFuture<ThreadPoolDemoResponse> callSlowPaymentWithCompletableFuture(
            long delayMs,
            String requestThread) {
        /*
         * SOLUTION 1: WebClient Mono converted to CompletableFuture.
         *
         * The controller returns CompletableFuture, so Spring MVC releases the incoming Tomcat request thread.
         * WebClient performs the outbound HTTP call using non-blocking IO, and toFuture() adapts the Mono
         * for APIs or teams that prefer CompletableFuture.
         */
        Instant startedAt = Instant.now();
        return webClient.get()
                .uri(paymentUrl(delayMs))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(60))
                .map(paymentResponse -> buildResponse("COMPLETABLE_FUTURE_FROM_WEB_CLIENT", delayMs, startedAt,
                        requestThread, paymentResponse))
                .toFuture();
    }

    public Mono<ThreadPoolDemoResponse> callSlowPaymentWithWebClient(long delayMs, String requestThread) {
        Instant startedAt = Instant.now();

        /*
         * SOLUTION 2: WebClient.
         *
         * WebClient uses non-blocking IO for the outbound HTTP call.
         * The controller returns Mono, so Spring MVC also uses async request processing for this response.
         */
        return webClient.get()
                .uri(paymentUrl(delayMs))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(60))
                .map(paymentResponse -> buildResponse("WEB_CLIENT_NON_BLOCKING", delayMs, startedAt, requestThread,
                        paymentResponse));
    }

    private String paymentUrl(long delayMs) {
        return paymentBaseUrl + "/api/demo/payments/slow?delayMs=" + delayMs + "&callNumber=1";
    }

    private ThreadPoolDemoResponse buildResponse(
            String strategy,
            long delayMs,
            Instant startedAt,
            String requestThread,
            String paymentResponse) {
        long durationMs = Duration.between(startedAt, Instant.now()).toMillis();
        return new ThreadPoolDemoResponse(
                strategy,
                delayMs,
                durationMs,
                requestThread,
                Thread.currentThread().getName(),
                paymentResponse);
    }

    public record ThreadPoolDemoResponse(
            String strategy,
            long paymentDelayMs,
            long totalDurationMs,
            String requestThread,
            String executionThread,
            String paymentResponse) {
    }
}
