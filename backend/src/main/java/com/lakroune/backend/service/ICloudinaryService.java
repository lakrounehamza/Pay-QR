package com.lakroune.backend.service;

import org.springframework.web.multipart.MultipartFile;

import com.lakroune.backend.dto.response.CloudinaryResponse;

public interface ICloudinaryService {
    CloudinaryResponse uploadFile(MultipartFile file);
    CloudinaryResponse uploadBytes(byte[] data, String fileName, String folder);
}
