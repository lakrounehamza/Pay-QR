package com.lakroune.backend.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lakroune.backend.dto.request.RegisterRequest;
import com.lakroune.backend.entity.Profile;
import com.lakroune.backend.entity.User;
import com.lakroune.backend.enums.VerificationStatus;
import com.lakroune.backend.exception.NotFoundException;
import com.lakroune.backend.repository.ProfileRepository;
import com.lakroune.backend.repository.UserRepository;
import com.lakroune.backend.service.IProfileService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProfileServiceImpl implements IProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Profile saveProfileForNewUser(UUID userId, RegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé."));

        Profile profile = Profile.builder()
                .cin(request.cin())
                .dateNaissance(request.dateNaissance())
                .documentType(request.documentType())
                .documentImageUrl(request.documentImageUrl())
                .verificationStatus(VerificationStatus.PENDING)
                .user(user)
                .build();

        return profileRepository.save(profile);
    }

    @Override
    @Transactional
    public Profile updateProfile(UUID userId, Profile profile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé."));

        profile.setUser(user);
        return profileRepository.save(profile);
    }

    @Override
    public Profile getProfileByUserId(UUID userId) {
        return profileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("Profil utilisateur non trouvé."));
    }
}
