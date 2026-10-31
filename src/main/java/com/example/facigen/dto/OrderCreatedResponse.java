package com.example.facigen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderCreatedResponse {

    private Long orderId;
    private String status;
    private BigDecimal amount;
}