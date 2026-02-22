package com.lakroune.backend.util;


import com.lakroune.backend.exception.FuncErrorException;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Pattern;

@UtilityClass
public class FileUploadUtil {

    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public static final String IMAGE_PATTERN =
            "([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)";

    public static final String PDF_PATTERN =
            "([^\\s]+(\\.(?i)(pdf))$)";

    public static final String IMAGE_PDF_PATTERN =
            "([^\\s]+(\\.(?i)(jpg|png|gif|bmp|pdf))$)";

    public static final String DATE_FORMAT = "yyyyMMddHHmmss";
    public static final String FILE_NAME_FORMAT = "%s_%s_%s";

    public static void assertAllowed(MultipartFile file, String pattern) {

        if (file == null || file.isEmpty()) {
            throw new FuncErrorException("File is empty or null");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FuncErrorException("Max file size is 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new FuncErrorException("Invalid file name");
        }

        if (!isAllowedExtension(originalFilename, pattern)) {
            throw new FuncErrorException(
                    "Only image (jpg, png, gif, bmp) or PDF files are allowed"
            );
        }
    }

    private static boolean isAllowedExtension(String fileName, String pattern) {
        return Pattern
                .compile(pattern, Pattern.CASE_INSENSITIVE)
                .matcher(fileName)
                .matches();
    }

    public static String getFileName(String originalFilename) {

        String extension = FilenameUtils.getExtension(originalFilename);
        String baseName = FilenameUtils
                .getBaseName(originalFilename)
                .replaceAll("\\s+", "_");

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(DATE_FORMAT));

        return String.format(
                FILE_NAME_FORMAT,
                baseName,
                timestamp,
                UUID.randomUUID() + "." + extension
        );
    }
}

