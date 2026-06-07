package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.entity.Notification;
import com.fmcg.ecommerce.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseNotificationService {

    private final NotificationRepository notificationRepository;
    
    // Map of User ID to a list of active SseEmitters (a user can have multiple devices/tabs open)
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        // Set timeout to 30 minutes, or -1 for infinite (though proxies might drop infinite connections)
        SseEmitter emitter = new SseEmitter(1800000L);
        
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((e) -> removeEmitter(userId, emitter));

        // Send a dummy event to establish the connection immediately
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connection established"));
        } catch (IOException e) {
            removeEmitter(userId, emitter);
        }

        return emitter;
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }

    /**
     * Saves the notification to the DB and attempts to push it to the user if they are online.
     */
    public void sendNotification(Long userId, Notification notification) {
        // 1. Save to database
        Notification savedNotification = notificationRepository.save(notification);

        // Convert to a safe Map to prevent Jackson LazyInitializationExceptions on the User entity
        Map<String, Object> safePayload = Map.of(
            "id", savedNotification.getId(),
            "publicId", savedNotification.getPublicId() != null ? savedNotification.getPublicId() : "",
            "title", savedNotification.getTitle(),
            "message", savedNotification.getMessage(),
            "type", savedNotification.getType(),
            "referenceId", savedNotification.getReferenceId() != null ? savedNotification.getReferenceId() : "",
            "isRead", savedNotification.getIsRead(),
            "createdAt", savedNotification.getCreatedAt() != null ? savedNotification.getCreatedAt().toString() : ""
        );

        // 2. Push to live connections
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            userEmitters.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("NOTIFICATION")
                            .data(safePayload));
                } catch (Exception e) {
                    // Catch ALL exceptions (IOException, IllegalStateException) so a broken stream
                    // never rolls back the main transaction!
                    deadEmitters.add(emitter);
                }
            });
            // Clean up dead connections
            userEmitters.removeAll(deadEmitters);
        }
    }
}
