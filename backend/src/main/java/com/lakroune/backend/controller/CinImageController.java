package com.lakroune.backend.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lakroune.backend.dto.request.ImageAnalysisRequest;
import com.lakroune.backend.dto.response.CloudinaryResponse;
import com.lakroune.backend.entity.Profile;
import com.lakroune.backend.entity.User;
import com.lakroune.backend.enums.VerificationStatus;
import com.lakroune.backend.exception.FuncErrorException;
import com.lakroune.backend.repository.ProfileRepository;
import com.lakroune.backend.repository.UserRepository;
import com.lakroune.backend.service.ICloudinaryService;
import com.lakroune.backend.service.OllamaAiService;
import com.lakroune.backend.service.impl.ImageAnalysisService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/cin")
@RequiredArgsConstructor
@Slf4j
public class CinImageController {

    private static final Pattern CIN_PATTERN = Pattern.compile("^[A-Za-z]{1,2}\\d{5,8}$");
    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    private final OllamaAiService ollamaAiService;
    private final ImageAnalysisService imageAnalysisService;
    private final ICloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

      @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeCin(
            @Valid @RequestBody ImageAnalysisRequest request) {
        log.info("CIN analysis request for URL: {}", request.imageUrl());
        try {
            byte[] imageBytes = imageAnalysisService.downloadImage(request.imageUrl());
            Map<String, String> result = ollamaAiService.analyzeCinWithOllama(imageBytes);

            boolean profileSaved = persistProfileIfCinIsValid(result, request.imageUrl());

            Map<String, Object> response = new HashMap<>(result);
            response.put("imageUrl", request.imageUrl());
            response.put("profileSaved", profileSaved);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error(" CIN analysis failed: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("status",  "error");
            error.put("message", "Failed to analyze CIN image");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private boolean persistProfileIfCinIsValid(Map<String, String> result, String imageUrl) {
        String cin = result.get("numeroCIN");
        if (cin == null || !CIN_PATTERN.matcher(cin.trim()).matches()) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return false;
        }

        Optional<User> userOpt = userRepository.findByEmail(authentication.getName());
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        Profile profile = profileRepository.findByUser_Id(user.getId())
                .orElseGet(Profile::new);

        profile.setUser(user);
        profile.setCin(cin.trim().toUpperCase());
        profile.setDocumentType("CIN");
        profile.setDocumentImageUrl(imageUrl);
        profile.setDateNaissance(parseBirthDate(result.get("dateNaissance")));
        profile.setVerificationStatus(VerificationStatus.VERIFIED);

        profileRepository.save(profile);
        return true;
    }

    private Date parseBirthDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return null;
        }

        String normalized = rawDate.trim();
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDate localDate = LocalDate.parse(normalized, formatter);
                return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkOllamaHealth() {
        boolean available = ollamaAiService.isOllamaAvailable();
        Map<String, Object> response = new HashMap<>();
        response.put("ollamaAvailable", available);
        response.put("model",           ollamaAiService.getCurrentModel());
        response.put("status",          available ? "online" : "offline");
        return available
            ? ResponseEntity.ok(response)
            : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam(name = "file") MultipartFile file) {
        try {
            CloudinaryResponse response = cloudinaryService.uploadFile(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (FuncErrorException e) {
            log.warn("CIN upload validation error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("CIN upload failed: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to upload CIN image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
