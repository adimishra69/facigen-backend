package com.example.facigen.repository;

import com.example.facigen.Entity.PendingEmailChange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingEmailChangeRepository
        extends JpaRepository<PendingEmailChange, Long> {

    Optional<PendingEmailChange> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}