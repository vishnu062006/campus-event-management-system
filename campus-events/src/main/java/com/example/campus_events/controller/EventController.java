package com.example.campus_events.controller;

import com.example.campus_events.model.Event;
import com.example.campus_events.model.EventStatus;
import com.example.campus_events.service.EventService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Event endpoints
 */
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventService eventService;

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    /**
     * POST /api/events
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Map<String, String> body) {
        try {
            String title = body.get("title");
            String description = body.get("description");
            LocalDateTime eventDate = LocalDateTime.parse(body.get("eventDate"));

            LocalDateTime startTime = body.get("startTime") != null
                    ? LocalDateTime.parse(body.get("startTime"))
                    : null;

            LocalDateTime endTime = body.get("endTime") != null
                    ? LocalDateTime.parse(body.get("endTime"))
                    : null;

            String location = body.get("location");
            Integer maxParticipants = Integer.parseInt(body.get("maxParticipants"));

            Double entryFee = body.get("entryFee") != null
                    ? Double.parseDouble(body.get("entryFee"))
                    : 0.0;

            Integer organizerId = Integer.parseInt(body.get("organizerId"));

            String imageUrl = body.getOrDefault("imageUrl", "");

            Event event = eventService.createEvent(
                    title,
                    description,
                    eventDate,
                    startTime,
                    endTime,
                    location,
                    maxParticipants,
                    entryFee,
                    organizerId,
                    imageUrl
            );

            return ResponseEntity.status(201).body(event);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Failed to create event: " + e.getMessage())
            );
        }
    }

    /**
     * POST /api/events/upload-image
     */
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret
            ));

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "eventara/events")
            );

            String imageUrl = (String) uploadResult.get("secure_url");

            return ResponseEntity.ok(Map.of(
                    "imageUrl", imageUrl
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Upload failed: " + e.getMessage())
            );
        }
    }



    /**
     * GET /api/events
     */
    @GetMapping
    public List<Event> getAllEvents(@RequestParam(required = false) String status) {
        if (status != null) {
            return eventService.getEventsByStatus(
                    EventStatus.valueOf(status.toUpperCase())
            );
        }
        return eventService.getAllEvents();
    }

    /**
     * GET /api/events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable int id) {
        return eventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/events/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable int id,
            @RequestBody Map<String, String> body
    ) {
        try {
            String title = body.get("title");
            String description = body.get("description");
            LocalDateTime eventDate = LocalDateTime.parse(body.get("eventDate"));

            LocalDateTime startTime = body.get("startTime") != null
                    ? LocalDateTime.parse(body.get("startTime"))
                    : null;

            LocalDateTime endTime = body.get("endTime") != null
                    ? LocalDateTime.parse(body.get("endTime"))
                    : null;

            String location = body.get("location");
            Integer maxParticipants = Integer.parseInt(body.get("maxParticipants"));

            Double entryFee = body.get("entryFee") != null
                    ? Double.parseDouble(body.get("entryFee"))
                    : 0.0;

            String imageUrl = body.getOrDefault("imageUrl", "");

            return eventService.updateEvent(
                            id,
                            title,
                            description,
                            eventDate,
                            startTime,
                            endTime,
                            location,
                            maxParticipants,
                            entryFee,
                            imageUrl
                    )
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Failed to update event: " + e.getMessage())
            );
        }
    }

    /**
     * DELETE /api/events/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable int id) {
        boolean deleted = eventService.deleteEvent(id);

        if (deleted) {
            return ResponseEntity.ok(Map.of(
                    "message", "Event deleted"
            ));
        }

        return ResponseEntity.notFound().build();
    }
}