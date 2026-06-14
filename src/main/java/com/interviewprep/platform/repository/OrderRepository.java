package com.interviewprep.platform.repository;
import com.interviewprep.platform.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    long countByCustomerEmail(String customerEmail);

    // Problem query used by the N+1 demo:
    // If this runs once for every user returned by userRepository.findAll(), the API does 1 + N queries.
    List<Order> findByUserId(Long userId);

    /*
     * Pagination fix for users with many orders.
     *
     * Do not fetch hundreds or thousands of child rows just because one user was loaded.
     * Pageable adds LIMIT/OFFSET, and countQuery gives Spring Data an efficient total-count query.
     */
    @Query(
            value = """
                    select o
                    from Order o
                    where o.user.id = :userId
                    order by o.createdAt desc
                    """,
            countQuery = """
                    select count(o)
                    from Order o
                    where o.user.id = :userId
                    """
    )
    Page<Order> findPageByUserId(Long userId, Pageable pageable);
}
