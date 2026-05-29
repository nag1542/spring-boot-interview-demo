package com.interviewprep.platform.web.dto;

import com.interviewprep.platform.domain.Role;
import com.interviewprep.platform.domain.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public class UserDtos {
    public record UserResponse(Long id, String email, String fullName, Set<Role> roles) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRoles());
        }
    }

    public record UserWithOrdersResponse(
            Long id,
            String email,
            String fullName,
            List<OrderSummaryResponse> orders) {
        public static UserWithOrdersResponse from(User user, List<com.interviewprep.platform.domain.Order> orders) {
            return new UserWithOrdersResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    orders.stream().map(OrderSummaryResponse::from).toList());
        }
    }

    public record OrderSummaryResponse(Long id, BigDecimal totalAmount, Instant createdAt) {
        public static OrderSummaryResponse from(com.interviewprep.platform.domain.Order order) {
            return new OrderSummaryResponse(order.getId(), order.getTotalAmount(), order.getCreatedAt());
        }
    }
}
