package com.example.facigen.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Registration OTP
    public void sendRegistrationOtpEmail(
            String email,
            String otp
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("2rathernotsay22@gmail.com");
        message.setTo(email);

        message.setSubject("Facigen - Registration OTP");

        message.setText(
                "Hello,\n\n" +

                        "Your Facigen registration OTP is:\n\n" +

                        otp + "\n\n" +

                        "This OTP is valid for 3 minutes.\n\n" +

                        "Use this OTP to verify your email address and complete your registration.\n\n" +

                        "Thank you for using Facigen.\n" +
                        "Facigen Team"
        );

        mailSender.send(message);
    }

    // Temporary email testing
    public void sendTestEmail() {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo("adimishra5679@gmail.com");
        message.setSubject("Facigen Email Test");
        message.setText("This is a test email from Facigen.");

        mailSender.send(message);
    }
}