package com.interviewprep.platform.service;

import com.interviewprep.platform.domain.Role;
import com.interviewprep.platform.domain.User;
import com.interviewprep.platform.domain.UserAuditLog;
import com.interviewprep.platform.repository.UserAuditLogRepository;
import com.interviewprep.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionFundamentalsDemoService {
    private final UserRepository userRepository;
    private final UserAuditLogRepository userAuditLogRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Module 1 - success path.
     *
     * Both writes run inside one transaction boundary. If the method returns normally,
     * Spring commits the user row and the audit row together.
     */
    @Transactional
    public TransactionDemoResult createUserAndAuditSuccessfully() {
        String email = demoEmail("success");
        User user = createDemoUser(email);
        UserAuditLog auditLog = createAuditLog(user, "USER_CREATED");

        return TransactionDemoResult.committed(
                email,
                user.getId(),
                auditLog.getId(),
                "User and audit log were saved in the same transaction. Both records committed.");
    }

    /**
     * Module 1 - failure path.
     *
     * The user is saved first, then the audit insert fails. Because the exception
     * is a runtime persistence exception inside the same transaction, Spring marks
     * the transaction rollback-only and neither row remains committed.
     */
    @Transactional
    public void createUserAndFailAudit(String email) {
        User user = createDemoUser(email);

        UserAuditLog invalidAuditLog = UserAuditLog.builder()
                .user(user)
                .eventType(null)
                .message("This audit insert intentionally fails because eventType is required.")
                .createdAt(Instant.now())
                .build();

        userAuditLogRepository.saveAndFlush(invalidAuditLog);
    }

    /**
     * Module 2 - checked exception default rule.
     *
     * Spring's default rollback policy rolls back on RuntimeException and Error,
     * but not on checked exceptions. This method commits unless rollbackFor is
     * configured explicitly for AuditNotificationException.
     */
    @Transactional
    public void createUserAndThrowCheckedException(String email) throws AuditNotificationException {
        User user = createDemoUser(email);
        createAuditLog(user, "USER_CREATED_CHECKED_EXCEPTION_DEMO");

        throw new AuditNotificationException(
                "Checked exception thrown after database writes. Spring commits by default for checked exceptions.");
    }

    /**
     * Module 2 - explicit no-rollback rule.
     *
     * Runtime exceptions normally roll back. noRollbackFor is useful only when the
     * exception represents a non-critical outcome and committing the data is still
     * the correct business decision.
     */
    @Transactional(noRollbackFor = IllegalStateException.class)
    public void createUserAndDoNotRollbackForRuntimeException(String email) {
        User user = createDemoUser(email);
        createAuditLog(user, "USER_CREATED_NO_ROLLBACK_FOR_DEMO");

        throw new IllegalStateException(
                "Runtime exception thrown, but noRollbackFor tells Spring to commit this transaction.");
    }

    /**
     * Reads rollback evidence outside the failed transaction so the API response
     * can prove that both user and audit records were removed.
     */
    public TransactionDemoResult rollbackResult(String email, String reason) {
        return TransactionDemoResult.rolledBack(
                email,
                userRepository.existsByEmail(email),
                userAuditLogRepository.countByUserEmail(email),
                reason);
    }

    /**
     * Reads commit evidence after an exception path where Spring intentionally
     * committed the transaction according to rollback rules.
     */
    public TransactionDemoResult committedAfterExceptionResult(String scenario, String email, String reason) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        return TransactionDemoResult.committedAfterException(
                scenario,
                email,
                user.getId(),
                userAuditLogRepository.countByUserEmail(email),
                reason);
    }

    /**
     * Module 3 - correct read-only use case.
     *
     * Use readOnly = true for query-only service methods. It communicates intent
     * and lets the persistence provider/database apply read-oriented optimizations.
     */
    @Transactional(readOnly = true)
    public ReadOnlyTransactionResult readOnlySummary() {
        return ReadOnlyTransactionResult.queryOnly(
                userRepository.count(),
                userAuditLogRepository.count(),
                "This service method only reads data, so @Transactional(readOnly = true) is appropriate.");
    }

    /**
     * Module 3 - setup data for the pitfall case.
     *
     * The setup is intentionally a normal write transaction. The following
     * read-only method then attempts to modify this same user.
     */
    @Transactional
    public ReadOnlyTransactionResult createUserForReadOnlyPitfall() {
        String email = demoEmail("readonly-pitfall");
        User user = createDemoUser(email);
        createAuditLog(user, "USER_CREATED_READ_ONLY_PITFALL_DEMO");

        return ReadOnlyTransactionResult.baseline(
                email,
                user.getFullName(),
                "Created baseline data in a normal transaction before entering the read-only pitfall case.");
    }

    /**
     * Module 3 - read-only pitfall.
     *
     * readOnly = true is not an authorization rule and should not be used as a
     * guarantee that writes are impossible. Depending on provider and database
     * behavior, explicit writes such as saveAndFlush can still be persisted.
     */
    @Transactional(readOnly = true)
    public ReadOnlyTransactionResult updateUserInsideReadOnlyTransaction(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        String before = user.getFullName();

        user.setFullName("Updated inside read-only transaction");
        userRepository.saveAndFlush(user);

        return ReadOnlyTransactionResult.pitfall(
                email,
                before,
                user.getFullName(),
                "The code performed an explicit saveAndFlush inside a read-only transaction. Do not rely on readOnly = true to protect data from writes.");
    }

    @Transactional(readOnly = true)
    public ReadOnlyTransactionResult readOnlyPitfallVerification(String email, String expectedName) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        return ReadOnlyTransactionResult.verification(
                email,
                user.getFullName(),
                expectedName.equals(user.getFullName()),
                "This verification query runs after the read-only method returns, proving whether the update was committed.");
    }

    public String newRollbackDemoEmail() {
        return demoEmail("rollback");
    }

    public String newCheckedExceptionDemoEmail() {
        return demoEmail("checked-exception");
    }

    public String newNoRollbackDemoEmail() {
        return demoEmail("no-rollback");
    }

    private User createDemoUser(String email) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("demo-password"))
                .fullName("Transaction Demo User")
                .roles(Set.of(Role.ROLE_USER))
                .build();
        return userRepository.saveAndFlush(user);
    }

    private UserAuditLog createAuditLog(User user, String eventType) {
        UserAuditLog auditLog = UserAuditLog.builder()
                .user(user)
                .eventType(eventType)
                .message("Created user " + user.getEmail())
                .createdAt(Instant.now())
                .build();
        return userAuditLogRepository.saveAndFlush(auditLog);
    }

    private String demoEmail(String scenario) {
        return "tx-" + scenario + "-" + UUID.randomUUID() + "@demo.local";
    }

    public record TransactionDemoResult(
            String scenario,
            String transactionOutcome,
            String userEmail,
            Long userId,
            Long auditLogId,
            boolean userExistsAfterRequest,
            long auditLogsAfterRequest,
            String explanation) {

        private static TransactionDemoResult committed(
                String userEmail,
                Long userId,
                Long auditLogId,
                String explanation) {
            return new TransactionDemoResult(
                    "success",
                    "COMMITTED",
                    userEmail,
                    userId,
                    auditLogId,
                    true,
                    1,
                    explanation);
        }

        private static TransactionDemoResult rolledBack(
                String userEmail,
                boolean userExistsAfterRequest,
                long auditLogsAfterRequest,
                String explanation) {
            return new TransactionDemoResult(
                    "audit-failure",
                    "ROLLED_BACK",
                    userEmail,
                    null,
                    null,
                    userExistsAfterRequest,
                    auditLogsAfterRequest,
                    explanation);
        }

        private static TransactionDemoResult committedAfterException(
                String scenario,
                String userEmail,
                Long userId,
                long auditLogsAfterRequest,
                String explanation) {
            return new TransactionDemoResult(
                    scenario,
                    "COMMITTED_AFTER_EXCEPTION",
                    userEmail,
                    userId,
                    null,
                    true,
                    auditLogsAfterRequest,
                    explanation);
        }
    }

    public static class AuditNotificationException extends Exception {
        public AuditNotificationException(String message) {
            super(message);
        }
    }

    public record ReadOnlyTransactionResult(
            String scenario,
            String transactionSetting,
            String userEmail,
            Long totalUsers,
            Long totalAuditLogs,
            String fullNameBefore,
            String fullNameAfter,
            boolean dataChanged,
            String explanation) {

        private static ReadOnlyTransactionResult queryOnly(
                long totalUsers,
                long totalAuditLogs,
                String explanation) {
            return new ReadOnlyTransactionResult(
                    "read-only-query",
                    "@Transactional(readOnly = true)",
                    null,
                    totalUsers,
                    totalAuditLogs,
                    null,
                    null,
                    false,
                    explanation);
        }

        private static ReadOnlyTransactionResult baseline(
                String userEmail,
                String fullName,
                String explanation) {
            return new ReadOnlyTransactionResult(
                    "read-only-pitfall-baseline",
                    "@Transactional",
                    userEmail,
                    null,
                    null,
                    null,
                    fullName,
                    false,
                    explanation);
        }

        private static ReadOnlyTransactionResult pitfall(
                String userEmail,
                String fullNameBefore,
                String fullNameAfter,
                String explanation) {
            return new ReadOnlyTransactionResult(
                    "read-only-explicit-write",
                    "@Transactional(readOnly = true)",
                    userEmail,
                    null,
                    null,
                    fullNameBefore,
                    fullNameAfter,
                    !fullNameBefore.equals(fullNameAfter),
                    explanation);
        }

        private static ReadOnlyTransactionResult verification(
                String userEmail,
                String fullNameAfter,
                boolean dataChanged,
                String explanation) {
            return new ReadOnlyTransactionResult(
                    "read-only-pitfall-verification",
                    "@Transactional(readOnly = true)",
                    userEmail,
                    null,
                    null,
                    null,
                    fullNameAfter,
                    dataChanged,
                    explanation);
        }
    }
}
