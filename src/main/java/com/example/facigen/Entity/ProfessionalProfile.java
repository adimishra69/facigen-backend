package com.example.facigen.Entity;

import com.example.facigen.model.Availability;
import com.example.facigen.model.Profession;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "professional_profiles")
public class ProfessionalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private Profession profession;
    private String experience;
    private String location;
    private String serviceArea;

    @Column(length = 2000)
    private String description;

    private String profilePhoto;
    private String workingHours;

    @Enumerated(EnumType.STRING)
    private Availability availability;

    private String pricing;
    private String skills;
    private String services;
}