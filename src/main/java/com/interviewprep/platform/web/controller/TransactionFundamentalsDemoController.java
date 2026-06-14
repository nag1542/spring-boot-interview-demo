package com.interviewprep.platform.web.controller;

import com.interviewprep.platform.service.TransactionFundamentalsDemoService;
import com.interviewprep.platform.service.TransactionalTrapDemoService;
import com.interviewprep.platform.web.dto.ApiResponse;
import com.interviewprep.platform.web.dto.TransactionDemoDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionFundamentalsDemoController {
    private final TransactionFundamentalsDemoService transactionFundamentalsDemoService;
    private final TransactionalTrapDemoService transactionalTrapDemoService;

    // Module 1: normal happy path. Both user and audit rows should exist after the request.
    @PostMapping("/api/demo/transactions/module-1/success")
    public ApiResponse<TransactionFundamentalsDemoService.TransactionDemoResult> success() {
        return ApiResponse.success(transactionFundamentalsDemoService.createUserAndAuditSuccessfully());
    }

    // Module 1: runtime failure path. The response verifies that neither write was committed.
    @PostMapping("/api/demo/transactions/module-1/audit-failure")
    public ApiResponse<TransactionFundamentalsDemoService.TransactionDemoResult> auditFailureRollback() {
        String email = transactionFundamentalsDemoService.newRollbackDemoEmail();
        try {
            transactionFundamentalsDemoService.createUserAndFailAudit(email);
            throw new IllegalStateException("Expected audit failure did not happen");
        } catch (RuntimeException ex) {
            return ApiResponse.success(
                    transactionFundamentalsDemoService.rollbackResult(
                            email,
                            "User insert happened first, then audit insert failed. Because both were inside one @Transactional method, Spring rolled the whole transaction back."));
        }
    }

    // Module 2: checked exceptions do not roll back by default, so committed rows are expected.
    @PostMapping("/api/demo/transactions/module-2/checked-exception")
    public ApiResponse<TransactionFundamentalsDemoService.TransactionDemoResult> checkedExceptionDoesNotRollback() {
        String email = transactionFundamentalsDemoService.newCheckedExceptionDemoEmail();
        try {
            transactionFundamentalsDemoService.createUserAndThrowCheckedException(email);
            throw new IllegalStateException("Expected checked exception did not happen");
        } catch (TransactionFundamentalsDemoService.AuditNotificationException ex) {
            return ApiResponse.success(
                    transactionFundamentalsDemoService.committedAfterExceptionResult(
                            "checked-exception-default-rule",
                            email,
                            "Spring rolls back by default for RuntimeException and Error. This checked exception was thrown after user and audit writes, but the transaction still committed."));
        }
    }

    // Module 2: noRollbackFor overrides the default runtime-exception rollback behavior.
    @PostMapping("/api/demo/transactions/module-2/no-rollback-for")
    public ApiResponse<TransactionFundamentalsDemoService.TransactionDemoResult> noRollbackForRuntimeException() {
        String email = transactionFundamentalsDemoService.newNoRollbackDemoEmail();
        try {
            transactionFundamentalsDemoService.createUserAndDoNotRollbackForRuntimeException(email);
            throw new IllegalStateException("Expected runtime exception did not happen");
        } catch (IllegalStateException ex) {
            return ApiResponse.success(
                    transactionFundamentalsDemoService.committedAfterExceptionResult(
                            "no-rollback-for-runtime-exception",
                            email,
                            "Runtime exceptions normally roll back. Here @Transactional(noRollbackFor = IllegalStateException.class) overrides that rule, so user and audit records committed."));
        }
    }

    // Module 3: proper use case. readOnly = true is best for query-only service methods.
    @PostMapping("/api/demo/transactions/module-3/read-only-query")
    public ApiResponse<TransactionFundamentalsDemoService.ReadOnlyTransactionResult> readOnlyQuery() {
        return ApiResponse.success(transactionFundamentalsDemoService.readOnlySummary());
    }

    // Module 3: pitfall. readOnly = true is not a write-protection mechanism.
    @PostMapping("/api/demo/transactions/module-3/read-only-write-pitfall")
    public ApiResponse<TransactionFundamentalsDemoService.ReadOnlyTransactionResult> readOnlyWritePitfall() {
        TransactionFundamentalsDemoService.ReadOnlyTransactionResult baseline =
                transactionFundamentalsDemoService.createUserForReadOnlyPitfall();

        TransactionFundamentalsDemoService.ReadOnlyTransactionResult update =
                transactionFundamentalsDemoService.updateUserInsideReadOnlyTransaction(baseline.userEmail());

        return ApiResponse.success(
                transactionFundamentalsDemoService.readOnlyPitfallVerification(
                        baseline.userEmail(),
                        update.fullNameAfter()));
    }

    // Transaction trap: @Transactional is bypassed when a method calls another method on the same object.
    @PostMapping("/api/demo/transactions/traps/self-invocation")
    public ApiResponse<TransactionDemoDtos.TransactionResultPayload> selfInvocationProblem(
            Authentication authentication,
            @Valid @RequestBody TransactionDemoDtos.ProductOrderRequest request) {
        return ApiResponse.success(
                transactionalTrapDemoService.selfInvocationProblem(authentication.getName(), request.productId()));
    }

    // Transaction trap: @Transactional on private methods is ignored by Spring proxy-based AOP.
    @PostMapping("/api/demo/transactions/traps/private-method")
    public ApiResponse<TransactionDemoDtos.TransactionResultPayload> privateMethodProblem(
            Authentication authentication,
            @Valid @RequestBody TransactionDemoDtos.ProductOrderRequest request) {
        return ApiResponse.success(
                transactionalTrapDemoService.privateMethodProblem(authentication.getName(), request.productId()));
    }

    // Transaction trap: catching a rollback-worthy exception can commit partial work and cause duplicate orders on retry.
    @PostMapping("/api/demo/transactions/traps/swallowed-exception")
    public ApiResponse<TransactionDemoDtos.TransactionResultPayload> swallowedExceptionProblem(
            Authentication authentication,
            @Valid @RequestBody TransactionDemoDtos.ProductOrderRequest request) {
        return ApiResponse.success(
                transactionalTrapDemoService.swallowedExceptionCreatesDuplicateOrderRisk(
                        authentication.getName(),
                        request.productId()));
    }
}
