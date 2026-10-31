package com.example.facigen.dto;

import com.example.facigen.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String message;
    private Long userId;
    private String name;
    private String email;
    private Role role;
    private String Phone;
    private String token;
    private String sessionId;
}