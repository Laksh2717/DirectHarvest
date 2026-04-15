package com.directharvest.backend.shared.cloudinary.controller;

import com.directharvest.backend.shared.cloudinary.response.CloudinaryUploadResponse;
import com.directharvest.backend.shared.cloudinary.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/cloudinary")
@Tag(name = "Cloudinary", description = "Cloudinary media operations. Requires authenticated cookie (access_token).")
@SecurityRequirement(name = "cookieAuth")
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    public CloudinaryController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload image", description = "Uploads multipart image to Cloudinary in backend-managed folder.")
    public ResponseEntity<CloudinaryUploadResponse> upload(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(cloudinaryService.uploadFile(file));
    }

    @DeleteMapping("/assets/{publicId}")
    @Operation(summary = "Delete Cloudinary asset", description = "Deletes Cloudinary asset by publicId.")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String publicId) {
        cloudinaryService.delete(publicId);
        return ResponseEntity.ok(Map.of("message", "Cloudinary asset deleted"));
    }
}
