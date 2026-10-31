package com.example.facigen.service;

import com.example.facigen.Entity.Order;
import com.example.facigen.Entity.ProfessionalProfile;
import com.example.facigen.Entity.User;
import com.example.facigen.dto.CreateOrderRequest;
import com.example.facigen.dto.CustomerGetResponse;
import com.example.facigen.dto.OrderCreatedResponse;
import com.example.facigen.dto.OrderStatusResponse;
import com.example.facigen.model.*;
import com.example.facigen.repository.OrderRepository;
import com.example.facigen.repository.ProfessionalProfileRepository;
import com.example.facigen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private  final ProfessionalProfileRepository professionalProfileRepository;


    private final UserRepository userRepository;


    private final OrderRepository orderRepository;

    public List<CustomerGetResponse> getAllProfessionals() {

        List<ProfessionalProfile> profiles =
                professionalProfileRepository.findAll();

        return profiles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public CustomerGetResponse getProfessional(Long userId) {

        ProfessionalProfile profile =
                professionalProfileRepository.findByUserId(userId)
                        .orElseThrow(() ->
                                new RuntimeException("Professional profile not found")
                        );

        return convertToResponse(profile);
    }

    public  OrderCreatedResponse sendRequest(CreateOrderRequest request) {

        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (customer.getRole() != Role.CUSTOMER) {
            throw new RuntimeException("User is not a customer");
        }

        User professionalUser = userRepository.findById(request.getProfessionalUserId())
                .orElseThrow(() -> new RuntimeException("Professional not found"));

        if (professionalUser.getRole() != Role.PROFESSIONAL) {
            throw new RuntimeException("User is not a professional");
        }

        ProfessionalProfile professional =
                professionalProfileRepository.findByUserId(request.getProfessionalUserId())
                        .orElseThrow(() -> new RuntimeException("Professional profile not found"));

        if (professional.getAvailability() != Availability.AVAILABLE) {
            throw new RuntimeException("Professional is currently not available");
        }

        boolean alreadyRequested = orderRepository.existsActiveRequest(
                request.getCustomerId(),
                request.getProfessionalUserId()
        );

        if (alreadyRequested) {
            throw new RuntimeException("You already have an active request with this professional");
        }

        Order order = new Order();

        order.setCustomer(customer);
        order.setProfessional(professionalUser);
        order.setService(request.getService());
        order.setAmount(request.getAmount());
        order.setScheduledAt(request.getScheduledAt());
        order.setStatus(OrderStatus.PENDING);
        order.setOtpVerified(false);

        Order savedOrder = orderRepository.save(order);

        return new OrderCreatedResponse(
                savedOrder.getId(),
                savedOrder.getStatus().name(),
                savedOrder.getAmount()
        );
    }

    private CustomerGetResponse convertToResponse(
            ProfessionalProfile profile
    ) {

        CustomerGetResponse response = new CustomerGetResponse();

        response.setUserId(profile.getUser().getId());
        response.setName(profile.getUser().getName());
        response.setPhone(profile.getUser().getPhone());

        response.setProfession(profile.getProfession());
        response.setExperience(profile.getExperience());
        response.setLocation(profile.getLocation());
        response.setServiceArea(profile.getServiceArea());
        response.setDescription(profile.getDescription());
        response.setProfilePhoto(profile.getProfilePhoto());
        response.setWorkingHours(profile.getWorkingHours());
        response.setAvailability(profile.getAvailability());
        response.setPricing(profile.getPricing());
        response.setSkills(profile.getSkills());
        response.setServices(profile.getServices());

        return response;
    }

    public OrderStatusResponse cancelRequest(Long orderId, Long userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        // Check that this order belongs to the customer
        if (!order.getCustomer().getId().equals(userId)) {
            throw new RuntimeException(
                    "This order does not belong to you"
            );
        }

        // Customer can cancel only before work starts
        if (order.getStatus() != OrderStatus.PENDING &&
                order.getStatus() != OrderStatus.ACCEPTED) {

            throw new RuntimeException(
                    "Order cannot be cancelled after work has started"
            );
        }

        // If professional had already accepted,
        // make professional available again
        if (order.getStatus() == OrderStatus.ACCEPTED) {

            ProfessionalProfile professional =
                    professionalProfileRepository
                            .findByUserId(order.getProfessional().getId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Professional profile not found"
                                    )
                            );

            professional.setAvailability(Availability.AVAILABLE);

            professionalProfileRepository.save(professional);
        }

        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);

        return new OrderStatusResponse(
                savedOrder.getId(),
                savedOrder.getStatus().name()
        );
    }

    public OrderStatusResponse confirmCompletion(Long orderId, Long userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        if (!order.getCustomer().getId().equals(userId)) {
            throw new RuntimeException(
                    "This order does not belong to you"
            );
        }

        if (order.getStatus() != OrderStatus.COMPLETION_REQUESTED) {
            throw new RuntimeException(
                    "Completion has not been requested"
            );
        }

        ProfessionalProfile professional =
                professionalProfileRepository
                        .findByUserId(order.getProfessional().getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Professional profile not found"
                                )
                        );

        order.setStatus(OrderStatus.COMPLETED);

        professional.setAvailability(Availability.AVAILABLE);
        professionalProfileRepository.save(professional);

        Order savedOrder = orderRepository.save(order);

        return new OrderStatusResponse(
                savedOrder.getId(),
                savedOrder.getStatus().name()
        );
    }
}