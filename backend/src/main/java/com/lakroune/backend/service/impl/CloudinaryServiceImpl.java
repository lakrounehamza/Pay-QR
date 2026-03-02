package com.lakroune.backend.service.impl;



import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lakroune.backend.dto.response.CloudinaryResponse;
import com.lakroune.backend.service.ICloudinaryService;
import com.lakroune.backend.util.FileUploadUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CloudinaryServiceImpl implements ICloudinaryService {

    private final Cloudinary cloudinary;

    @Transactional
    public CloudinaryResponse uploadFile(MultipartFile file) {

        FileUploadUtil.assertAllowed(file, FileUploadUtil.IMAGE_PDF_PATTERN);

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = FilenameUtils
                    .getExtension(originalFilename)
                    .toLowerCase();

            String fileName = FileUploadUtil.getFileName(originalFilename);

            boolean isPdf = "pdf".equals(extension);

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", fileName,
                            "folder", isPdf ? "documents" : "images",
                            "resource_type", isPdf ? "raw" : "image",
                            "type", "upload",          
                            "access_mode", "public"    
                    )
            );
            return  new CloudinaryResponse(uploadResult.get("public_id").toString()
                    ,uploadResult.get("secure_url").toString(), fileName,extension,isPdf ? "raw" : "image");

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }

    @Override
    public CloudinaryResponse uploadBytes(byte[] data, String fileName, String folder) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    data,
                    ObjectUtils.asMap(
                            "public_id", fileName,
                            "folder", folder,
                            "resource_type", "image",
                            "type", "upload",
                            "access_mode", "public"
                    )
            );

            String extension = FilenameUtils.getExtension(fileName);
            return new CloudinaryResponse(
                    uploadResult.get("public_id").toString(),
                    uploadResult.get("secure_url").toString(),
                    fileName,
                    extension,
                    uploadResult.get("resource_type").toString()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }
}

