package com.directharvest.backend.shared.cloudinary.service;

import com.directharvest.backend.shared.cloudinary.response.CloudinaryUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    CloudinaryUploadResponse uploadFile(MultipartFile file);

    void delete(String publicId);
}

