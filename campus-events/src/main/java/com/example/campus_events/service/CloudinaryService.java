package com.example.campus_events.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public String uploadEventImage(MultipartFile file) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "eventara/events",
                        "transformation", "w_800,h_400,c_fill,q_auto,f_auto"
                )
        );
        return (String) result.get("secure_url");
    }

    public void deleteImage(String imageUrl) {
        try {
            // Extract public_id from URL
            String[] parts = imageUrl.split("/");
            String publicId = "eventara/events/" +
                    parts[parts.length - 1].replaceAll("\\.[^.]+$", "");
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            // log but don't throw
        }
    }
}