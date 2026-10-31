package com.example.facigen.repository;

import com.example.facigen.Entity.Order;
import com.example.facigen.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByProfessional_IdAndStatus(
            Long professionalId,
            OrderStatus status
    );

    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM orders
            WHERE customer_id = :customerId
              AND professional_id = :professionalId
              AND status IN ('PENDING', 'ACCEPTED', 'IN_PROGRESS')
        )
        """, nativeQuery = true)
    boolean existsActiveRequest(
            @Param("customerId") Long customerId,
            @Param("professionalId") Long professionalId
    );

    List<Order> findByCustomer_IdAndStatus(
            Long customerId,
            OrderStatus status
    );

    // Finds other pending orders of the same customer,
    // excluding the order that is currently being accepted.
    @Query("""
        SELECT o
        FROM Order o
        WHERE o.customer.id = :customerId
          AND o.status = :status
          AND o.id <> :orderId
    """)
    List<Order> findOtherOrdersByCustomerAndStatus(
            @Param("customerId") Long customerId,
            @Param("status") OrderStatus status,
            @Param("orderId") Long orderId
    );
}