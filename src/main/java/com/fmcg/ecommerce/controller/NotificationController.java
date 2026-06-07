package com.fmcg.ecommerce.controller;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.entity.Notification;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.NotificationRepository;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.impl.SseNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Real-time SSE Notifications API")
public class NotificationController {

    private final SseNotificationService sseService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private Long getUserId(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", auth.getName()))
                .getId();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to real-time notification stream (Server-Sent Events)")
    public SseEmitter subscribe(Authentication auth) {
        Long userId = getUserId(auth);
        return sseService.subscribe(userId);
    }

    @GetMapping
    @Operation(summary = "Get all historical unread notifications")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnreadNotifications(Authentication auth) {
        Long userId = getUserId(auth);
        // Note: For production we would add findByUserIdAndIsReadFalse to NotificationRepository.
        // Doing inline filtering for this demo implementation:
        List<Notification> unread = notificationRepository.findAll().stream()
                .filter(n -> n.getUser() != null && n.getUser().getId().equals(userId) && !n.getIsRead())
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Notifications fetched successfully", unread));
    }

    @PostMapping("/{publicId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Object>> markAsRead(@PathVariable String publicId, Authentication auth) {
        Long userId = getUserId(auth);
        
        Notification notification = notificationRepository.findAll().stream()
                .filter(n -> publicId.equals(n.getPublicId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "publicId", publicId));

        if (notification.getUser() == null || !notification.getUser().getId().equals(userId)) {
            throw new com.fmcg.ecommerce.exception.UnauthorizedException("Cannot modify this notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);

        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read"));
    }
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication auth) {
        Long userId = getUserId(auth);
        long count = notificationRepository.findAll().stream()
                .filter(n -> n.getUser() != null && n.getUser().getId().equals(userId) && !n.getIsRead())
                .count();
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    @PatchMapping("/mark-all-read")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<String>> markAllRead(Authentication auth) {
        Long userId = getUserId(auth);
        List<Notification> unread = notificationRepository.findAll().stream()
                .filter(n -> n.getUser() != null && n.getUser().getId().equals(userId) && !n.getIsRead())
                .toList();
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read"));
    }
}