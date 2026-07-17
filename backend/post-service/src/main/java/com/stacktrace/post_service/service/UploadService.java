package com.stacktrace.post_service.service;

import com.stacktrace.post_service.dto.response.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    UploadResponse uploadImage(MultipartFile file);
}