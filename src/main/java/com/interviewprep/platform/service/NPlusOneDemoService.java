package com.interviewprep.platform.service;

import com.interviewprep.platform.domain.Order;
import com.interviewprep.platform.domain.User;
import com.interviewprep.platform.repository.OrderRepository;
import com.interviewprep.platform.repository.UserRepository;
import com.interviewprep.platform.web.dto.UserDtos;
import lombok.RequiredArgsConstructor;
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
        // List<User> users = userRepository.findAll();
        // return users.stream()
        //         .map(user -> {
        //             List<Order> orders = orderRepository.findByUserId(user.getId());
        //             return UserDtos.UserWithOrdersResponse.from(user, orders);
        //         })
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
}
