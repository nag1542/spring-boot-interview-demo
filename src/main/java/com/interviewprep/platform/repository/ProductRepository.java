package com.interviewprep.platform.repository;
import com.interviewprep.platform.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {
    Optional<Product> findFirstByNameIgnoreCase(String name);
}
