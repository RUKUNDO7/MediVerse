package com.mediverse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "health_metrics")
public class HealthMetric extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private String type; // e.g., "BLOOD_PRESSURE", "GLUCOSE", "WEIGHT", "HEART_RATE"

    @Column(nullable = false)
    private String value; // String to handle ranges like "120/80" or simple numbers

    private String unit; // e.g., "mmHg", "mg/dL", "kg", "bpm"

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    private String notes;
}
