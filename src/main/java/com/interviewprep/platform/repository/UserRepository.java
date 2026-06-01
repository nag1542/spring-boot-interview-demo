package com.interviewprep.platform.repository;
import com.interviewprep.platform.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Roles are LAZY by default to avoid unrelated user_roles queries in demos and list queries.
    // Fetch them explicitly for security and profile use cases that need authorities.
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "roles")
    @Query("select u from User u")
    List<User> findAllWithRoles();

    // FIX A: JOIN FETCH loads users and orders in one SQL query.
    // Use DISTINCT because one user can have many orders, which would otherwise duplicate users in the result.
    @Query("select distinct u from User u left join fetch u.orders")
    List<User> findAllWithOrdersUsingJoinFetch();

    // FIX B: @EntityGraph asks Hibernate to fetch the orders association eagerly for this query only.
    // This keeps the default entity mapping LAZY while avoiding N+1 for this read use case.
    @EntityGraph(attributePaths = "orders")
    @Query("select u from User u")
    List<User> findAllWithOrdersUsingEntityGraph();
}
