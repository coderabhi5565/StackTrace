package com.stacktrace.post_service.service.impl;

import com.stacktrace.post_service.dto.response.UploadResponse;
import com.stacktrace.post_service.exception.ImageUploadFailedException;
import com.stacktrace.post_service.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final Cloudinary cloudinary;

    @Override
    public UploadResponse uploadImage(MultipartFile file) {

        validateImage(file);

        try {

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.emptyMap()
            );

            String imageUrl = result.get("secure_url").toString();

            return UploadResponse.builder()
                    .imageUrl(imageUrl)
                    .build();

        } catch (IOException e) {
            throw new ImageUploadFailedException(
                    "Failed to upload image."
            );
        }
    }

    private void validateImage(MultipartFile file) {

        if (file.isEmpty()) {
            throw new ImageUploadFailedException(
                    "Image file is required."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ImageUploadFailedException(
                    "Image size must not exceed 5 MB."
            );
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !contentType.startsWith("image/")) {

            throw new ImageUploadFailedException(
                    "Only image files are allowed."
            );
        }
    }
}