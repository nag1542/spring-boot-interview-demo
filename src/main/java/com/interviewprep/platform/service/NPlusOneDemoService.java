package com.interviewprep.platform.service;

import com.interviewprep.platform.domain.Order;
import com.interviewprep.platform.domain.User;
import com.interviewprep.platform.repository.OrderRepository;
import com.interviewprep.platform.repository.UserRepository;
import com.interviewprep.platform.web.dto.UserDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NPlusOneDemoService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<UserDtos.UserWithOrdersResponse> getUsersWithOrders() {
        /*
         * PROBLEM: N+1 query pattern.
         *
         * 1 query loads all users.
         * Then 1 extra query runs for each user to load orders.
         *
         * Example:
         * - 1 user query
         * - 10 users returned
         * - 10 order queries
         * = 11 total SQL queries for one API call
         */
        

        /*
         * PROBLEM VARIANT: N+1 caused by FetchType / association access.
         *
         * This version does not call orderRepository.findByUserId(...) manually.
         * It loads users and then accesses user.getOrders().
         *
         * Demo options:
         * 1. Set User.orders to FetchType.LAZY:
         *    - userRepository.findAll() loads users.
         *    - user.getOrders() triggers one SQL query per user.
         *
         * 2. Set User.orders to FetchType.EAGER:
         *    - Hibernate tries to load orders automatically when users are loaded.
         *    - This often creates unexpected additional SQL and slower endpoints.
         *
         * Uncomment this block and comment the manual OrderRepository problem block above.
         */

        // List<User> users = userRepository.findAll();
        // return users.stream()
        //         .map(user -> {
        //             List<Order> orders = orderRepository.findByUserId(user.getId());
        //             return UserDtos.UserWithOrdersResponse.from(user, orders);
        //         })
        //         .toList();

        // List<User> users = userRepository.findAll();
        // return users.stream()
        //         .map(user -> UserDtos.UserWithOrdersResponse.from(user, List.copyOf(user.getOrders())))
        //         .toList();

        /*
         * FIX A: JOIN FETCH using JPQL.
         *
         * Uncomment this block and comment the PROBLEM block above.
         * Hibernate loads users and orders in one SQL query using a join.
         */
        // List<User> users = userRepository.findAllWithOrdersUsingJoinFetch();
        // return users.stream()
        //         .map(user -> UserDtos.UserWithOrdersResponse.from(user, List.copyOf(user.getOrders())))
        //         .toList();

        /*
         * FIX B: @EntityGraph.
         *
         * Uncomment this block and comment the PROBLEM block above.
         * Hibernate keeps the entity relationship LAZY by default, but fetches orders eagerly for this query.
         */
        List<User> users = userRepository.findAllWithOrdersUsingEntityGraph();
        return users.stream()
                .map(user -> UserDtos.UserWithOrdersResponse.from(user, List.copyOf(user.getOrders())))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDtos.UserOrdersPageResponse getUserOrdersPage(Long userId, Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findPageByUserId(userId, pageable);
        return new UserDtos.UserOrdersPageResponse(
                userId,
                ordersPage.getNumber(),
                ordersPage.getSize(),
                ordersPage.getTotalElements(),
                ordersPage.getTotalPages(),
                ordersPage.isFirst(),
                ordersPage.isLast(),
                ordersPage.getContent().stream().map(UserDtos.OrderSummaryResponse::from).toList());
    }
}
