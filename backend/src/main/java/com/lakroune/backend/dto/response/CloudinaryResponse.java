package com.lakroune.backend.dto.response;

public record CloudinaryResponse(
        String publicId,
        String url,
        String filename,
        String extension,
        String fileType
) {
}

