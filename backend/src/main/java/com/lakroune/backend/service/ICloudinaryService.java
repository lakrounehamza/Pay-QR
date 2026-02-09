package com.lakroune.backend.service;

import com.lakroune.backend.dto.response.CloudinaryResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ICloudinaryService {
    public CloudinaryResponse uploadFile(MultipartFile file);
}
