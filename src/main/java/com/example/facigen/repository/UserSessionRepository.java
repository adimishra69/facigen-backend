package com.example.facigen.repository;

import com.example.facigen.Entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSessionRepository
        extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByUserId(Long userId);

    Optional<UserSession> findBySessionToken(String sessionToken);

    void deleteByUserId(Long userId);
}