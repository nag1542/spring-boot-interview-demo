package com.interviewprep.platform.repository;
import com.interviewprep.platform.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    // Problem query used by the N+1 demo:
    // If this runs once for every user returned by userRepository.findAll(), the API does 1 + N queries.
    List<Order> findByUserId(Long userId);
}
