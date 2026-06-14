package com.interviewprep.platform.web.dto;

import jakarta.validation.constraints.NotNull;

public class TransactionDemoDtos {

    public record ProductOrderRequest(
            @NotNull Long productId) {
    }

    public record TransactionResultPayload(
            String scenario,
            String outcome,
            String customerEmail,
            Long productId,
            Long orderId,
            long ordersAfterRequest,
            Integer inventoryBefore,
            Integer inventoryAfter,
            long auditLogsAfterRequest,
            String explanation,
            String productionSolution) {

        public static TransactionResultPayload of(
                String scenario,
                String outcome,
                String customerEmail,
                Long productId,
                Long orderId,
                long ordersAfterRequest,
                Integer inventoryBefore,
                Integer inventoryAfter,
                long auditLogsAfterRequest,
                String explanation,
                String productionSolution) {
            return new TransactionResultPayload(
                    scenario,
                    outcome,
                    customerEmail,
                    productId,
                    orderId,
                    ordersAfterRequest,
                    inventoryBefore,
                    inventoryAfter,
                    auditLogsAfterRequest,
                    explanation,
                    productionSolution);
        }
    }
}
