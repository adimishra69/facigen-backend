package com.example.facigen.dto;

import com.example.facigen.model.Availability;
import com.example.facigen.model.Profession;
import lombok.Data;
@Data
public class CustomerGetResponse {

        private Long userId;

        private String name;
        private String phone;

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