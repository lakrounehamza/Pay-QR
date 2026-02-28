package com.lakroune.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lakroune.backend.dto.request.ImageAnalysisRequest;
import com.lakroune.backend.service.OllamaAiService;
import com.lakroune.backend.service.impl.ImageAnalysisService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@RestController
@RequestMapping("/api/cin")
@RequiredArgsConstructor
@Slf4j
public class CinAnalysisController {

    private final OllamaAiService ollamaAiService;
    private final ImageAnalysisService imageAnalysisService;

      @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeCin(
            @Valid @RequestBody ImageAnalysisRequest request) {
        log.info("CIN analysis request for URL: {}", request.getImageUrl());
        try {
            byte[] imageBytes = imageAnalysisService.downloadImage(request.getImageUrl());
            Map<String, String> result = ollamaAiService.analyzeCinWithOllama(imageBytes);

            Map<String, Object> response = new HashMap<>(result);
            response.put("imageUrl", request.getImageUrl());
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
}
