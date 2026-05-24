package com.example.campus_events.controller;

import com.example.campus_events.model.Event;
import com.example.campus_events.repository.EventRepository;
import com.example.campus_events.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class ImageController {

    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private EventRepository eventRepository;

    @PostMapping("/{id}/image")
    public ResponseEntity<?> uploadImage(@PathVariable Integer id,
                                         @RequestParam("file") MultipartFile file) {
        try {
            Event event = eventRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Event not found"));

            // Delete old image if exists
            if (event.getImageUrl() != null) {
                cloudinaryService.deleteImage(event.getImageUrl());
            }

            String imageUrl = cloudinaryService.uploadEventImage(file);
            event.setImageUrl(imageUrl);
            eventRepository.save(event);

            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Image upload failed: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}