package com.example.facigen.dto;
import com.example.facigen.model.Availability;
import com.example.facigen.model.Profession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfessionalProfileResponse {

    private Long id;
    private Long userId;

    // Professional profile information
    private Profession profession;
    private String experience;
    private String location;
    private String serviceArea;
    private String description;
    private String profilePhoto;
    private String workingHours;
    private Availability availability;
    private String pricing;
    private String skills;
    private String services;
}

