package com.mediverse.controller;

import com.mediverse.model.Appointment;
import com.mediverse.model.Doctor;
import com.mediverse.model.Patient;
import com.mediverse.model.Prescription;
import com.mediverse.repository.AppointmentRepository;
import com.mediverse.repository.DoctorRepository;
import com.mediverse.repository.PatientRepository;
import com.mediverse.repository.PrescriptionRepository;
import com.mediverse.security.MessageResponse;
import com.mediverse.security.UserDetailsImpl;
import com.mediverse.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired
    PrescriptionRepository prescriptionRepository;

    @Autowired
    AppointmentRepository appointmentRepository;

    @Autowired
    DoctorRepository doctorRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    PatientRepository patientRepository;

    @PostMapping("/issue/{appointmentId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> issuePrescription(@PathVariable Long appointmentId, @RequestBody Prescription request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Doctor profile not found."));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Error: Appointment not found."));

        // Validate doctor is the one who took the appointment
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
             return ResponseEntity.badRequest().body(new MessageResponse("Error: Unauthorized to issue prescription for this appointment."));
        }

        Patient patient = appointment.getPatient();

        Prescription prescription = new Prescription();
        prescription.setAppointment(appointment);
        prescription.setDoctor(doctor);
        prescription.setPatient(patient);
        prescription.setMedications(request.getMedications());
        prescription.setDosageInstructions(request.getDosageInstructions());
        prescription.setDuration(request.getDuration());

        // Placeholder for PDF generation path if implemented later
        prescription.setDigitalSignaturePath("/path/to/generated/pdf_" + appointmentId + ".pdf");

        prescriptionRepository.save(prescription);

        emailService.sendPrescriptionIssuedEmail(
            patient.getUser().getEmail(),
            patient.getFirstName() + " " + patient.getLastName(),
            doctor.getLastName(),
            request.getMedications()
        );

        return ResponseEntity.ok(new MessageResponse("Prescription issued successfully."));
    }

    @GetMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getPatientPrescriptions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Patient patient = patientRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Patient profile not found."));

        return ResponseEntity.ok(prescriptionRepository.findByPatientId(patient.getId()));
    }
}
