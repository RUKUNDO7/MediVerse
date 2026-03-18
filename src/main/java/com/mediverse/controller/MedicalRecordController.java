package com.mediverse.controller;

import com.mediverse.model.Doctor;
import com.mediverse.model.MedicalRecord;
import com.mediverse.model.Patient;
import com.mediverse.repository.DoctorRepository;
import com.mediverse.repository.MedicalRecordRepository;
import com.mediverse.repository.PatientRepository;
import com.mediverse.security.MessageResponse;
import com.mediverse.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/records")
public class MedicalRecordController {

    @Autowired
    MedicalRecordRepository medicalRecordRepository;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    DoctorRepository doctorRepository;

    private static final String UPLOAD_DIR = "uploads/medical_records/";

    @PostMapping("/upload/{patientId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> uploadMedicalRecord(
            @PathVariable Long patientId,
            @RequestParam("diagnosis") String diagnosis,
            @RequestParam(value = "treatmentPlan", required = false) String treatmentPlan,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Doctor profile not found."));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Error: Patient not found."));

        MedicalRecord record = new MedicalRecord();
        record.setDoctor(doctor);
        record.setPatient(patient);
        record.setDiagnosis(diagnosis);
        record.setTreatmentPlan(treatmentPlan);

        if (file != null && !file.isEmpty()) {
            try {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR + filename);
                Files.write(filePath, file.getBytes());

                record.setAttachedFilePath(filePath.toString());
                record.setFileType(file.getContentType());
            } catch (IOException e) {
                return ResponseEntity.internalServerError().body(new MessageResponse("Could not upload file: " + e.getMessage()));
            }
        }

        medicalRecordRepository.save(record);

        return ResponseEntity.ok(new MessageResponse("Medical record added successfully."));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<?> getRecordsForPatient(@PathVariable Long patientId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Security check: if PATIENT role, they can only see their own records
        boolean isAdminOrDoctor = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_DOCTOR"));

        if (!isAdminOrDoctor) {
            Patient patient = patientRepository.findByUserId(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("Error: Patient profile not found."));
            if (!patient.getId().equals(patientId)) {
                return ResponseEntity.status(403).body(new MessageResponse("Error: Unauthorized to view these records."));
            }
        }

        List<MedicalRecord> records = medicalRecordRepository.findByPatientId(patientId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Medical record not found."));

        if (record.getAttachedFilePath() == null || record.getAttachedFilePath().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = Paths.get(record.getAttachedFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
