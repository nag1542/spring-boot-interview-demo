package com.interviewprep.platform.service;

import com.interviewprep.platform.domain.Order;
import com.interviewprep.platform.domain.Product;
import com.interviewprep.platform.domain.User;
import com.interviewprep.platform.domain.UserAuditLog;
import com.interviewprep.platform.repository.OrderRepository;
import com.interviewprep.platform.repository.ProductRepository;
import com.interviewprep.platform.repository.UserAuditLogRepository;
import com.interviewprep.platform.repository.UserRepository;
import com.interviewprep.platform.web.dto.TransactionDemoDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TransactionalTrapDemoService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserAuditLogRepository userAuditLogRepository;

    public TransactionDemoDtos.TransactionResultPayload selfInvocationProblem(String userEmail, Long productId) {
        User user = loadUser(userEmail);
        Product product = loadProduct(productId);
        int inventoryBefore = product.getStock();

        try {
            placeOrderWithTransactionalAnnotation(user, product);
            throw new IllegalStateException("Expected audit failure did not happen");
        } catch (RuntimeException ex) {
            return payload(
                    "self-invocation-problem",
                    "PARTIAL_COMMIT",
                    user,
                    product,
                    inventoryBefore,
                    "The method has @Transactional, but it was called from another method on the same class. The proxy was bypassed, so order and inventory writes committed before audit failed.",
                    "Move the transactional method to another Spring bean or put @Transactional on the external service entry method.");
        }
    }

    @Transactional
    public void placeOrderWithTransactionalAnnotation(User user, Product product) {
        placeOrder(user, product);
        throw new IllegalStateException("Audit service failed after order and inventory update.");
    }

    public TransactionDemoDtos.TransactionResultPayload privateMethodProblem(String userEmail, Long productId) {
        User user = loadUser(userEmail);
        Product product = loadProduct(productId);
        int inventoryBefore = product.getStock();

        try {
            placeOrderInPrivateTransactionalMethod(user, product);
            throw new IllegalStateException("Expected audit failure did not happen");
        } catch (RuntimeException ex) {
            return payload(
                    "private-method-transaction-ignored",
                    "PARTIAL_COMMIT",
                    user,
                    product,
                    inventoryBefore,
                    "@Transactional on a private method is ignored by Spring proxy-based AOP. Order and inventory can commit before audit failure.",
                    "Keep private methods as helpers only. Put @Transactional on a proxy-invoked service method.");
        }
    }

    @Transactional
    private void placeOrderInPrivateTransactionalMethod(User user, Product product) {
        placeOrder(user, product);
        throw new IllegalStateException("Audit service failed inside private method.");
    }

    @Transactional
    public TransactionDemoDtos.TransactionResultPayload swallowedExceptionCreatesDuplicateOrderRisk(
            String userEmail,
            Long productId) {
        User user = loadUser(userEmail);
        Product product = loadProduct(productId);
        int inventoryBefore = product.getStock();

        placeOrder(user, product);

        try {
            throw new IllegalStateException("Audit provider failed");
        } catch (IllegalStateException ex) {
            // This is the trap: returning normally tells Spring to commit.
        }

        return payload(
                "swallowed-exception-duplicate-order-risk",
                "COMMITTED_WITH_ERROR",
                user,
                product,
                inventoryBefore,
                "The audit failure was caught and hidden, so the transaction returned normally and committed the order. If the user retries because the order looked failed, another order or charge can be created.",
                "Let rollback-worthy exceptions escape the transactional method, or mark the transaction rollback-only before returning an error result. Use idempotency keys for order/payment retries.");
    }

    @Transactional
    public void placeOrderCorrectly(String userEmail, Long productId) {
        User user = loadUser(userEmail);
        Product product = loadProduct(productId);

        placeOrder(user, product);
        createAuditLog(user, "ORDER_CREATED");
    }

    private Order placeOrder(User user, Product product) {
        reduceInventory(product);

        return orderRepository.saveAndFlush(Order.builder()
                .customerEmail(user.getEmail())
                .totalAmount(product.getPrice())
                .createdAt(Instant.now())
                .user(user)
                .build());
    }

    private void reduceInventory(Product product) {
        if (product.getStock() <= 0) {
            throw new IllegalArgumentException("Product is out of stock");
        }
        product.setStock(product.getStock() - 1);
        productRepository.saveAndFlush(product);
    }

    private void createAuditLog(User user, String eventType) {
        userAuditLogRepository.saveAndFlush(UserAuditLog.builder()
                .user(user)
                .eventType(eventType)
                .message("Order created for " + user.getEmail())
                .createdAt(Instant.now())
                .build());
    }

    private User loadUser(String userEmail) {
        return userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found"));
    }

    private Product loadProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product was not found"));
    }

    private TransactionDemoDtos.TransactionResultPayload payload(
            String scenario,
            String outcome,
            User user,
            Product product,
            int inventoryBefore,
            String explanation,
            String solution) {
        Product reloadedProduct = loadProduct(product.getId());

        return TransactionDemoDtos.TransactionResultPayload.of(
                scenario,
                outcome,
                user.getEmail(),
                product.getId(),
                null,
                orderRepository.countByCustomerEmail(user.getEmail()),
                inventoryBefore,
                reloadedProduct.getStock(),
                userAuditLogRepository.countByUserEmail(user.getEmail()),
                explanation,
                solution);
    }
}
