package com.interviewprep.platform.repository;
import com.interviewprep.platform.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderRepository extends JpaRepository<Order,Long> {}
