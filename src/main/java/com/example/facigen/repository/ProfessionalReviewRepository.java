package com.example.facigen.repository;

import com.example.facigen.Entity.ProfessionalReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessionalReviewRepository
        extends JpaRepository<ProfessionalReview, Long> {

    boolean existsByOrderId(Long orderId);
}