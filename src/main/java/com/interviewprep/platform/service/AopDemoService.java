package com.interviewprep.platform.service;

import com.interviewprep.platform.aop.AfterCustomerNotification;
import com.interviewprep.platform.aop.AroundCachedPriceQuote;
import com.interviewprep.platform.aop.AroundLogExecutionTime;
import com.interviewprep.platform.aop.BeforeFraudScreen;
import com.interviewprep.platform.web.dto.AopDemoDtos.LoanApplicationRequest;
import com.interviewprep.platform.web.dto.AopDemoDtos.LoanApplicationResult;
import com.interviewprep.platform.web.dto.AopDemoDtos.PriceQuoteResult;
import com.interviewprep.platform.web.dto.AopDemoDtos.SupportTicketResult;
import com.interviewprep.platform.web.dto.AopDemoDtos.ExecutionReportResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class AopDemoService {

    @BeforeFraudScreen(maxAutoApprovalAmount = 500_000)
    public LoanApplicationResult submitLoanApplication(LoanApplicationRequest request) {
        return new LoanApplicationResult(
                "LOAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                request.customerId(),
                request.requestedAmount(),
                "AUTO_APPROVED_FOR_PROCESSING",
                "Application moved to document verification");
    }

    @AfterCustomerNotification(channel = "email")
    public SupportTicketResult closeSupportTicket(String ticketId) {
        return new SupportTicketResult(
                ticketId,
                "CLOSED",
                "Customer Success",
                "Issue resolved and closure summary saved");
    }

    @AroundCachedPriceQuote
    public PriceQuoteResult getSupplierPriceQuote(String sku, int quantity) {
        simulateSupplierLatency();

        BigDecimal unitPrice = switch (sku.toUpperCase()) {
            case "LAPTOP-PRO" -> new BigDecimal("1249.99");
            case "MONITOR-4K" -> new BigDecimal("399.50");
            case "KEYBOARD-MECH" -> new BigDecimal("129.00");
            default -> new BigDecimal("75.00");
        };

        return new PriceQuoteResult(
                sku.toUpperCase(),
                quantity,
                unitPrice,
                unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP),
                "Acme Wholesale",
                "USD");
    }

    @AroundLogExecutionTime
    public ExecutionReportResult generateMonthlySalesReport(String region, int dayCount) {
        simulateReportWork(dayCount);
        return new ExecutionReportResult(
                "RPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                region.toUpperCase(),
                dayCount,
                "Monthly sales report assembled for " + region.toUpperCase());
    }

    private void simulateSupplierLatency() {
        try {
            Thread.sleep(600);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Supplier quote lookup was interrupted", ex);
        }
    }

    private void simulateReportWork(int dayCount) {
        try {
            Thread.sleep(Math.min(250L + (dayCount * 15L), 900L));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Report generation was interrupted", ex);
        }
    }
}
