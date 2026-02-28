package com.lakroune.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ImageAnalysisRequest (
    @NotBlank(message = "imageUrl is required")
     String imageUrl,
     String customPrompt
){}
