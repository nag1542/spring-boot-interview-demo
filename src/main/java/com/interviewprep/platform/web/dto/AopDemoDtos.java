package com.interviewprep.platform.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public class AopDemoDtos {
    public record LoanApplicationRequest(
            @NotBlank String customerId,
            @Positive long requestedAmount,
            @Min(300) @Max(850) int creditScore) {
    }

    public record AopDemoResponse<T>(
            String adviceType,
            String realWorldUseCase,
            T businessResult,
            List<String> adviceTrace,
            String interviewTakeaway) {
    }

    public record LoanApplicationResult(
            String applicationId,
            String customerId,
            long requestedAmount,
            String decision,
            String nextStep) {
    }

    public record SupportTicketResult(
            String ticketId,
            String status,
            String assignedTeam,
            String customerMessage) {
    }

    public record PriceQuoteResult(
            String sku,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice,
            String supplier,
            String currency) {
    }

    public record ExecutionReportResult(
            String reportId,
            String region,
            int dayCount,
            String summary) {
    }
}
