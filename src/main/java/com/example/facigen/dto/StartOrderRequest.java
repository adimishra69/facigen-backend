package com.example.facigen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartOrderRequest {

    @NotBlank(message = "OTP is required")
    private String otp;
}