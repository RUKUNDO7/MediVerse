package com.mediverse.controller;

import com.mediverse.model.HealthMetric;
import com.mediverse.model.Patient;
import com.mediverse.repository.HealthMetricRepository;
import com.mediverse.repository.PatientRepository;
import com.mediverse.security.MessageResponse;
import com.mediverse.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/health-metrics")
public class HealthMetricController {

    @Autowired
    HealthMetricRepository healthMetricRepository;

    @Autowired
    PatientRepository patientRepository;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> recordMetric(@RequestBody HealthMetric metricRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Patient patient = patientRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Patient profile not found."));

        HealthMetric metric = new HealthMetric();
        metric.setPatient(patient);
        metric.setType(metricRequest.getType());
        metric.setValue(metricRequest.getValue());
        metric.setUnit(metricRequest.getUnit());
        metric.setRecordedAt(LocalDateTime.now());
        metric.setNotes(metricRequest.getNotes());

        healthMetricRepository.save(metric);

        return ResponseEntity.ok(new MessageResponse("Health metric recorded successfully."));
    }

    @GetMapping("/my-metrics")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<HealthMetric>> getMyMetrics(@RequestParam(required = false) String type) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Patient patient = patientRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Patient profile not found."));

        if (type != null) {
            return ResponseEntity.ok(healthMetricRepository.findByPatientIdAndTypeOrderByRecordedAtDesc(patient.getId(), type));
        }
        return ResponseEntity.ok(healthMetricRepository.findByPatientIdOrderByRecordedAtDesc(patient.getId()));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<HealthMetric>> getPatientMetrics(
            @PathVariable Long patientId,
            @RequestParam(required = false) String type) {
        
        if (type != null) {
            return ResponseEntity.ok(healthMetricRepository.findByPatientIdAndTypeOrderByRecordedAtDesc(patientId, type));
        }
        return ResponseEntity.ok(healthMetricRepository.findByPatientIdOrderByRecordedAtDesc(patientId));
    }
}
