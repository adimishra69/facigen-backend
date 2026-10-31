package com.example.facigen.controller;

import com.example.facigen.Entity.ProfessionalReview;
import com.example.facigen.dto.*;
import com.example.facigen.Entity.Order;
import com.example.facigen.service.CustomerService;
import com.example.facigen.service.ProfessionalReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProfessionalReviewService professionalReviewService;

    // Get all professionals
    @GetMapping("/professionals")
    public List<CustomerGetResponse> getAllProfessionals() {
        return customerService.getAllProfessionals();
    }

    // Get one professional
    @GetMapping("/professional/{userId}")
    public CustomerGetResponse getProfessional(
            @PathVariable Long userId
    ) {
        return customerService.getProfessional(userId);
    }

    // Send service request to a professional
    @PostMapping("/request")
    public OrderCreatedResponse sendRequest(
            @RequestBody CreateOrderRequest request
    ) {
        return customerService.sendRequest(request);
    }

    // Cancel request
    @PutMapping("/request/{orderId}/cancel")
    public OrderStatusResponse cancelRequest(
            @PathVariable Long orderId,
            @RequestParam Long userId
    ) {
        return customerService.cancelRequest(orderId, userId);
    }

    // Confirm completion
    @PutMapping("/request/{orderId}/confirm-completion")
    public OrderStatusResponse confirmCompletion(
            @PathVariable Long orderId,
            @RequestParam Long userId
    ) {
        return customerService.confirmCompletion(orderId, userId);
    }

    @PostMapping("/review")
    public ProfessionalReview createReview(
            @RequestParam Long customerId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return professionalReviewService.createReview(
                request,
                customerId
        );
    }

    @PutMapping("/review/{reviewId}")
    public ProfessionalReview updateReview(
            @PathVariable Long reviewId,
            @RequestParam Long customerId,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        return professionalReviewService.updateReview(
                reviewId,
                customerId,
                request
        );
    }
}