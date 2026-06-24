package com.interviewprep.platform.web.controller;

import com.interviewprep.platform.aop.AopDemoTraceRecorder;
import com.interviewprep.platform.service.AopDemoService;
import com.interviewprep.platform.web.dto.ApiResponse;
import com.interviewprep.platform.web.dto.AopDemoDtos;
import com.interviewprep.platform.web.dto.AopDemoDtos.AopDemoResponse;
import com.interviewprep.platform.web.dto.AopDemoDtos.ExecutionReportResult;
import com.interviewprep.platform.web.dto.AopDemoDtos.LoanApplicationRequest;
import com.interviewprep.platform.web.dto.AopDemoDtos.LoanApplicationResult;
import com.interviewprep.platform.web.dto.AopDemoDtos.PriceQuoteResult;
import com.interviewprep.platform.web.dto.AopDemoDtos.SupportTicketResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class AopDemoController {
    private final AopDemoService aopDemoService;
    private final AopDemoTraceRecorder traceRecorder;

    @PostMapping("/api/demo/aop/before/loan-application")
    public ApiResponse<AopDemoResponse<LoanApplicationResult>> beforeAdviceLoanApplication(
            @Valid @RequestBody LoanApplicationRequest request) {
        LoanApplicationResult result = aopDemoService.submitLoanApplication(request);
        return ApiResponse.success(new AopDemoResponse<>(
                "BEFORE",
                "Banking risk policy: validate credit score and loan amount before creating an application.",
                result,
                traceRecorder.drain(),
                "Use @Before for pre-checks, authorization, risk screening, and audit context that must run before business logic."));
    }

    @PostMapping("/api/demo/aop/after/support-ticket-close")
    public ApiResponse<AopDemoResponse<SupportTicketResult>> afterAdviceSupportTicket(
            @RequestParam(defaultValue = "TICKET-1001") String ticketId) {
        SupportTicketResult result = aopDemoService.closeSupportTicket(ticketId);
        return ApiResponse.success(new AopDemoResponse<>(
                "AFTER",
                "Customer support: always queue a customer notification after the ticket workflow completes.",
                result,
                traceRecorder.drain(),
                "Use @After for guaranteed follow-up work such as cleanup, audit markers, or notifications that should run after method completion."));
    }

    @GetMapping("/api/demo/aop/around/supplier-price-quote")
    public ApiResponse<AopDemoResponse<PriceQuoteResult>> aroundAdvicePriceQuote(
            @RequestParam(defaultValue = "LAPTOP-PRO") String sku,
            @RequestParam(defaultValue = "2") @Min(1) int quantity) {
        PriceQuoteResult result = aopDemoService.getSupplierPriceQuote(sku, quantity);
        return ApiResponse.success(new AopDemoResponse<>(
                "AROUND",
                "E-commerce procurement: time an external supplier quote and cache repeated quote requests.",
                result,
                traceRecorder.drain(),
                "Use @Around when advice must control method execution, measure duration, short-circuit with cache, retry, or decorate the result."));
    }

    @GetMapping("/api/demo/aop/around/log-execution-time")
    public ApiResponse<AopDemoResponse<ExecutionReportResult>> aroundAdviceLogExecutionTime(
            @RequestParam(defaultValue = "south") String region,
            @RequestParam(defaultValue = "30") @Min(1) int dayCount) {
        ExecutionReportResult result = aopDemoService.generateMonthlySalesReport(region, dayCount);
        return ApiResponse.success(new AopDemoResponse<>(
                "AROUND",
                "Analytics/reporting: measure how long a monthly sales report takes to build.",
                result,
                traceRecorder.drain(),
                "Use @Around for cross-cutting concerns like execution timing, where the advice wraps the business method before and after it runs."));
    }
}
