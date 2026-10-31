package com.example.facigen.controller;

import com.example.facigen.dto.ProfessionalProfileRequest;
import com.example.facigen.dto.ProfessionalProfileResponse;
import com.example.facigen.service.ProfessionalProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/professional/profile")
public class ProfessionalProfileController {

    @Autowired
    private ProfessionalProfileService profileService;

    @PostMapping("/{userId}")
    public ProfessionalProfileResponse saveProfile(
            @PathVariable Long userId,
            @RequestBody ProfessionalProfileRequest request
    ) {

        return profileService.createOrUpdateProfile(
                userId,
                request
        );
    }


    @GetMapping("/{userId}")
    public ProfessionalProfileResponse getProfile(
            @PathVariable Long userId
    ) {

        return profileService.getProfile(userId);
    }
}