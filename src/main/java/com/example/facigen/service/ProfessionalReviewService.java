package com.example.facigen.service;

import com.example.facigen.Entity.Order;
import com.example.facigen.Entity.User;
import com.example.facigen.dto.CreateReviewRequest;
import com.example.facigen.Entity.ProfessionalReview;
import com.example.facigen.dto.UpdateReviewRequest;
import com.example.facigen.model.OrderStatus;
import com.example.facigen.repository.OrderRepository;
import com.example.facigen.repository.ProfessionalReviewRepository;
import com.example.facigen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfessionalReviewService {


    private  final ProfessionalReviewRepository professionalReviewRepository;


    private final OrderRepository orderRepository;


    private final UserRepository userRepository;

    public ProfessionalReview createReview(
            CreateReviewRequest request,
            Long customerId
    ) {

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() ->

                        new RuntimeException("Order not found")
                );

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException(
                    "This order does not belong to you"
            );
        }

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException(
                    "Only completed orders can be reviewed"
            );
        }

        if (professionalReviewRepository.existsByOrderId(order.getId())) {
            throw new RuntimeException(
                    "This order has already been reviewed"
            );
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found")
                );

        ProfessionalReview review = new ProfessionalReview();

        review.setOrder(order);
        review.setCustomer(customer);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return professionalReviewRepository.save(review);
    }

    public ProfessionalReview updateReview(
            Long reviewId,
            Long customerId,
            UpdateReviewRequest request
    ) {

        ProfessionalReview review = professionalReviewRepository
                .findById(reviewId)
                .orElseThrow(() ->
                        new RuntimeException("Review not found")
                );

        // Make sure this customer owns the review
        if (!review.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException(
                    "You can only update your own review"
            );
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return professionalReviewRepository.save(review);
    }
}