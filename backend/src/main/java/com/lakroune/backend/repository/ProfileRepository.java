package com.lakroune.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lakroune.backend.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
	Optional<Profile> findByUser_Id(UUID userId);
}
