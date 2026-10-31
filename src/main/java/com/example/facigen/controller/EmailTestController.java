package com.example.facigen.controller;

import com.example.facigen.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/email")
    public String testEmail() {

        emailService.sendTestEmail();

        return "Test email sent successfully";
    }
}