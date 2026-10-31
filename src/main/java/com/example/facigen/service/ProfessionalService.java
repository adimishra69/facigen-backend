package com.example.facigen.service;

import com.example.facigen.dto.OrderAcceptedResponse;
import com.example.facigen.dto.OrderStatusResponse;
import com.example.facigen.model.Availability;
import com.example.facigen.Entity.Order;
import com.example.facigen.model.OrderStatus;
import com.example.facigen.Entity.ProfessionalProfile;
import com.example.facigen.repository.OrderRepository;
import com.example.facigen.repository.ProfessionalProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProfessionalService {

    private final OrderRepository orderRepository;

    private final EmailService emailService;

    private final ProfessionalProfileRepository professionalProfileRepository;

    // Get pending requests
    public List<Order> getPendingRequests(Long userId) {

        ProfessionalProfile professional =
                professionalProfileRepository.findByUserId(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Professional profile not found"
                                )
                        );

        return orderRepository.findByProfessional_IdAndStatus(
                userId,
                OrderStatus.PENDING
        );
    }


    // Accept request
    public OrderAcceptedResponse acceptRequest(Long orderId, Long userId){

        ProfessionalProfile professional =
                professionalProfileRepository.findByUserId(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Professional profile not found"
                                )
                                {}      );

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        // Check that this order belongs to this professional
        if (!order.getProfessional().getId().equals(userId)) {
            throw new RuntimeException(
                    "This order does not belong to you"
            );
        }

        // Only PENDING requests can be accepted
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException(
                    "Only pending requests can be accepted"
            );
        }

        // Accept this request
        order.setStatus(OrderStatus.ACCEPTED);

        SecureRandom secureRandom = new SecureRandom();

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1000000)
        );

        order.setStartOtp(otp);
        order.setOtpVerified(false);

        // Find all other PENDING requests
        // made by the same customer
        List<Order> otherRequests =
                orderRepository.findOtherOrdersByCustomerAndStatus(
                        order.getCustomer().getId(),
                        OrderStatus.PENDING,
                        order.getId()
                );

        for (Order otherOrder : otherRequests) {

            otherOrder.setStatus(OrderStatus.REJECTED);
        }

        // Save rejected requests
        orderRepository.saveAll(otherRequests);

        // Make this professional BUSY
        professional.setAvailability(Availability.BUSY);
        professionalProfileRepository.save(professional);

        // Save accepted request first
        Order savedOrder = orderRepository.save(order);

        return new OrderAcceptedResponse(
                savedOrder.getId(),
                savedOrder.getStatus().name(),
                savedOrder.getService(),
                savedOrder.getAmount()
        );
    }

    // Reject request
    public  OrderStatusResponse rejectRequest(Long orderId, Long userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );


        // Check ownership
        if (!order.getProfessional().getId().equals(userId)) {

            throw new RuntimeException(
                    "This order does not belong to you"
            );
        }

        // Only PENDING requests can be rejected
        if (order.getStatus() != OrderStatus.PENDING) {

            throw new RuntimeException(
                    "Only pending requests can be rejected"
            );
        }

        order.setStatus(OrderStatus.REJECTED);

        Order savedOrder = orderRepository.save(order);

        return new OrderStatusResponse(
                savedOrder.getId(),
                savedOrder.getStatus().name()
        );
    }

    public OrderStatusResponse cancelRequest(Long orderId, Long userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        // Check that this order belongs to this professional
        if (!order.getProfessional().getId().equals(userId)) {
            throw new RuntimeException(
                    "This order does not belong to you"
            );
        }

        // Professional can cancel only after accepting
        // and before work starts
        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new RuntimeException(
                    "Only accepted orders can be cancelled"
            );
        }

        // Make professional available again
        ProfessionalProfile professional =
                professionalProfileRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Professional profile not found"
                                )
                        );

        professional.setAvailability(Availability.AVAILABLE);

        professionalProfileRepository.save(professional);

        // Cancel the order
        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);

        return new OrderStatusResponse(
                savedOrder.getId(),
                savedOrder.getStatus().name()
        );
    }

    public  OrderStatusResponse startOrder(
            Long orderId,
            Long userId,
            String otp
    ) {

        ProfessionalProfile professional =
                professionalProfileRepository.findByUserId(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Professional profile not found"
                                )
                        );

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        // Check that order belongs to this professional
        if (!order.getProfessional().getId().equals(userId)) {
            throw new RuntimeException(
                    "This order does not belong to you"
            );
        }

        // Only accepted orders can be started
        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new RuntimeException(
                    "Only accepted orders can be started"
            );
        }

        // Check OTP
        String startOtp = order.getStartOtp();

        if (!Objects.equals(otp, startOtp)) {
            throw new RuntimeException("Invalid OTP");
        }

        // OTP verified
        order.setOtpVerified(true);
        order.setStatus(OrderStatus.IN_PROGRESS);

        Order savedOrder = orderRepository.save(order);

        return new OrderStatusResponse(
                savedOrder.getId(),
                savedOrder.getStatus().name()
        );
    }

    public OrderStatusResponse requestCompletion(Long orderId, Long userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        if (!order.getProfessional().getId().equals(userId)) {
            throw new RuntimeException(
                    "This order does not belong to you"
            );
        }

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new RuntimeException(
                    "Only orders in progress can request completion"
            );
        }

        order.setStatus(OrderStatus.COMPLETION_REQUESTED);

        Order savedOrder = orderRepository.save(order);

        return new OrderStatusResponse(
                savedOrder.getId(),
                savedOrder.getStatus().name()
        );
    }
}