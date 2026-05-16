package com.interviewprep.platform.repository;
import com.interviewprep.platform.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductRepository extends JpaRepository<Product,Long> {}
