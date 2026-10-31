package com.example.facigen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderAcceptedResponse {

    private Long orderId;
    private String status;
    private String service;
    private BigDecimal amount;
}