package com.mediverse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private boolean isRead = false;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    public enum NotificationType {
        APPOINTMENT_REMINDER,
        PRESCRIPTION_ALERT,
        SYSTEM_ALERT
    }
}
