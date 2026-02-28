package com.lakroune.backend.service.impl;

import java.io.InputStream;
import java.net.URL;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper service for downloading images from external URLs (Cloudinary, S3, CDN, etc.)
 * before passing them to AI vision models.
 */
@Service
@Slf4j
public class ImageAnalysisService {
    public byte[] downloadImage(String imageUrl) throws Exception {
        log.info("Downloading image from URL: {}", imageUrl);
        try (InputStream in = new URL(imageUrl).openStream()) {
            byte[] bytes = in.readAllBytes();
            log.info("Image downloaded: {} bytes", bytes.length);
            return bytes;
        } catch (Exception e) {
            log.error("Failed to download image from {}: {}", imageUrl, e.getMessage());
            throw new RuntimeException("Failed to download image from URL: " + imageUrl, e);
        }
    }
}
