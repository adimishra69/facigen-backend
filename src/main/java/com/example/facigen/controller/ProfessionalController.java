package com.example.facigen.controller;

import com.example.facigen.dto.OrderAcceptedResponse;
import com.example.facigen.dto.OrderStatusResponse;
import com.example.facigen.dto.StartOrderRequest;
import com.example.facigen.Entity.Order;
import com.example.facigen.service.ProfessionalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professional")
public class ProfessionalController {

    @Autowired
    private ProfessionalService professionalService;

    // Get pending requests for a professional
    @GetMapping("/requests")
    public List<Order> getRequests(
            @RequestParam Long userId
    ) {
        return professionalService.getPendingRequests(userId);
    }

    // Accept request
    @PutMapping("/request/{orderId}/accept")
    public OrderAcceptedResponse acceptRequest(
            @PathVariable Long orderId,
            @RequestParam Long userId
    ) {
        return professionalService.acceptRequest(orderId, userId);
    }

    // Reject request
    @PutMapping("/request/{orderId}/reject")
    public OrderStatusResponse rejectRequest(
            @PathVariable Long orderId,
            @RequestParam Long userId
    ) {
        return professionalService.rejectRequest(orderId, userId);
    }

    // Start order
    @PostMapping("/request/{orderId}/start")
    public OrderStatusResponse startOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId,
            @RequestBody StartOrderRequest request
    ) {
        return professionalService.startOrder(
                orderId,
                userId,
                request.getOtp()
        );
    }

    // Request completion
    @PutMapping("/request/{orderId}/request-completion")
    public OrderStatusResponse requestCompletion(
            @PathVariable Long orderId,
            @RequestParam Long userId
    ) {
        return professionalService.requestCompletion(orderId, userId);
    }

    // Cancel request
    @PutMapping("/request/{orderId}/cancel")
    public OrderStatusResponse cancelRequest(
            @PathVariable Long orderId,
            @RequestParam Long userId
    ) {
        return professionalService.cancelRequest(orderId, userId);
    }
}