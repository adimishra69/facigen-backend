package com.example.facigen.Entity;

import com.example.facigen.model.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Customer who placed the order
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;


    private String startOtp;

    @Column(nullable = false)
    private boolean otpVerified = false;

    // Professional who will provide the service
    @ManyToOne
    @JoinColumn(name = "professional_id", nullable = false)
    private User professional;

    // Service requested by the customer
    @Column(nullable = false)
    private String service;

    // Amount agreed for this service
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // When the service is scheduled
    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    // Current status of the order
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // Database creation time
    private LocalDateTime createdAt;

    // Last modification time
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}