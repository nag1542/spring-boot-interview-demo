package com.interviewprep.platform.aop;

import com.interviewprep.platform.web.dto.AopDemoDtos.LoanApplicationRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@RequiredArgsConstructor
public class AopDemoAspect {
    private final AopDemoTraceRecorder traceRecorder;
    private final Map<String, Object> priceQuoteCache = new ConcurrentHashMap<>();

    @Before("@annotation(beforeFraudScreen) && args(request)")
    public void runFraudScreen(BeforeFraudScreen beforeFraudScreen, LoanApplicationRequest request) {
        traceRecorder.record("BEFORE advice: screened loan application before the service method executed.");

        if (request.requestedAmount() > beforeFraudScreen.maxAutoApprovalAmount()) {
            traceRecorder.record("BEFORE advice: blocked request because amount exceeded auto-approval policy.");
            throw new IllegalArgumentException("Loan amount requires manual review before the application can be submitted.");
        }

        if (request.creditScore() < 650) {
            traceRecorder.record("BEFORE advice: blocked request because credit score is below policy.");
            throw new IllegalArgumentException("Credit score is below the automated approval threshold.");
        }
    }

    @After("@annotation(afterCustomerNotification)")
    public void sendCustomerNotification(AfterCustomerNotification afterCustomerNotification) {
        traceRecorder.record("AFTER advice: queued " + afterCustomerNotification.channel()
                + " notification after ticket workflow finished.");
    }

    @Around("@annotation(com.interviewprep.platform.aop.AroundCachedPriceQuote)")
    public Object cacheAndTimePriceQuote(ProceedingJoinPoint joinPoint) throws Throwable {
        String cacheKey = joinPoint.getSignature().toShortString() + Arrays.deepToString(joinPoint.getArgs());
        long startNanos = System.nanoTime();

        if (priceQuoteCache.containsKey(cacheKey)) {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            traceRecorder.record("AROUND advice: returned cached supplier quote in " + elapsedMs + " ms.");
            return priceQuoteCache.get(cacheKey);
        }

        traceRecorder.record("AROUND advice: cache miss, calling the supplier pricing workflow.");
        Object result = joinPoint.proceed();
        priceQuoteCache.put(cacheKey, result);

        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
        traceRecorder.record("AROUND advice: supplier quote completed in " + elapsedMs + " ms and was cached.");
        return result;
    }

    @Around("@annotation(com.interviewprep.platform.aop.AroundLogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startNanos = System.nanoTime();
        traceRecorder.record("AROUND advice: started timing " + joinPoint.getSignature().toShortString() + ".");

        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            traceRecorder.record("AROUND advice: " + joinPoint.getSignature().toShortString()
                    + " completed in " + elapsedMs + " ms.");
        }
    }

    @After("within(com.interviewprep.platform.web.controller.AopDemoController)")
    public void clearTraceIfControllerFailed() {
        traceRecorder.clear();
    }
}
