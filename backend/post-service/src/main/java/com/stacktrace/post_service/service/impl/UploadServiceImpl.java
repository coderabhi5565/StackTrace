package com.stacktrace.post_service.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.stacktrace.post_service.dto.response.UploadResponse;
import com.stacktrace.post_service.exception.ImageUploadFailedException;
import com.stacktrace.post_service.exception.UploadsNotConfiguredException;
import com.stacktrace.post_service.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg", "image/webp", "image/gif");

    private final Cloudinary cloudinary;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Override
    public UploadResponse uploadImage(MultipartFile file) {
        if (apiSecret == null || apiSecret.isBlank()) {
            throw new UploadsNotConfiguredException("Image uploads are not configured on this server.");
        }
        validateImage(file);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "stacktrace", "resource_type", "image"));
            return UploadResponse.builder()
                    .imageUrl(result.get("secure_url").toString())
                    .build();
        } catch (IOException | RuntimeException e) {
            throw new ImageUploadFailedException("Failed to upload image.");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ImageUploadFailedException("Image file is required.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ImageUploadFailedException("Image size must not exceed 5 MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ImageUploadFailedException("Only PNG, JPEG, WebP or GIF images are allowed.");
        }
    }
}
