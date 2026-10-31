package com.example.facigen.service;

import com.example.facigen.dto.ProfessionalProfileRequest;
import com.example.facigen.dto.ProfessionalProfileResponse;
import com.example.facigen.Entity.ProfessionalProfile;
import com.example.facigen.Entity.User;
import com.example.facigen.repository.ProfessionalProfileRepository;
import com.example.facigen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfessionalProfileService {


    private final ProfessionalProfileRepository profileRepository;


    private final UserRepository userRepository;

    public ProfessionalProfileResponse createOrUpdateProfile(
            Long userId,
            ProfessionalProfileRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        ProfessionalProfile profile =
                profileRepository.findByUserId(userId)
                        .orElse(new ProfessionalProfile());

        profile.setUser(user);

        profile.setProfession(request.getProfession());
        profile.setExperience(request.getExperience());
        profile.setLocation(request.getLocation());
        profile.setServiceArea(request.getServiceArea());
        profile.setDescription(request.getDescription());
        profile.setProfilePhoto(request.getProfilePhoto());
        profile.setWorkingHours(request.getWorkingHours());
        profile.setAvailability(request.getAvailability());
        profile.setPricing(request.getPricing());
        profile.setSkills(request.getSkills());
        profile.setServices(request.getServices());

        ProfessionalProfile savedProfile =
                profileRepository.save(profile);

        return toResponse(savedProfile);
    }


    public ProfessionalProfileResponse getProfile(Long userId) {

        ProfessionalProfile profile =
                profileRepository.findByUserId(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Professional profile not found"
                                )
                        );

        return toResponse(profile);
    }


    private ProfessionalProfileResponse toResponse(
            ProfessionalProfile profile
    ) {

        return new ProfessionalProfileResponse(

                // ProfessionalProfile information
                profile.getId(),
                profile.getUser().getId(),

                // Professional profile information
                profile.getProfession(),
                profile.getExperience(),
                profile.getLocation(),
                profile.getServiceArea(),
                profile.getDescription(),
                profile.getProfilePhoto(),
                profile.getWorkingHours(),
                profile.getAvailability(),
                profile.getPricing(),
                profile.getSkills(),
                profile.getServices()
        );
    }
}
