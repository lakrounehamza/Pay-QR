package com.lakroune.backend.service;

import java.util.UUID;

import com.lakroune.backend.dto.request.RegisterRequest;
import com.lakroune.backend.entity.Profile;

public interface IProfileService {
    /**
     * Save profile information for a newly registered user
     */
    Profile saveProfileForNewUser(UUID userId, RegisterRequest request);

    /**
     * Update existing user profile
     */
    Profile updateProfile(UUID userId, Profile profile);

    /**
     * Get user profile by user ID
     */
    Profile getProfileByUserId(UUID userId);
}
