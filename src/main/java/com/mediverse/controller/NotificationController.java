package com.mediverse.controller;

import com.mediverse.model.Notification;
import com.mediverse.repository.NotificationRepository;
import com.mediverse.security.MessageResponse;
import com.mediverse.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    NotificationRepository notificationRepository;

    @GetMapping
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<List<Notification>> getMyNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(notificationRepository.findByUserId(userDetails.getId()));
    }

    @PutMapping("/read/{id}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Notification not found."));

        if (!notification.getUser().getId().equals(userDetails.getId())) {
             return ResponseEntity.status(403).body(new MessageResponse("Error: Unauthorized to modify this notification."));
        }

        notification.setRead(true);
        notificationRepository.save(notification);

        return ResponseEntity.ok(new MessageResponse("Notification marked as read."));
    }
}
