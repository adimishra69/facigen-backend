package com.example.facigen.Entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "professional_reviews"
//        uniqueConstraints = {
//                @UniqueConstraint(
//                        name = "uk_order_review",
//                        columnNames = {"order_id"}
//                )
//        }
)
public class ProfessionalReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The order for which this review is given
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    // Customer who gave the review
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    // Rating given by the customer
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;

    // Customer's comment
    @Column(length = 2000)
    private String comment;

    private LocalDateTime createdAt;

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