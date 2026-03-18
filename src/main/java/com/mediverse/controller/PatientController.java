package com.mediverse.controller;

import com.mediverse.model.Patient;
import com.mediverse.model.User;
import com.mediverse.repository.PatientRepository;
import com.mediverse.repository.UserRepository;
import com.mediverse.security.MessageResponse;
import com.mediverse.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Optional<Patient> patient = patientRepository.findByUserId(userDetails.getId());

        if (patient.isPresent()) {
            return ResponseEntity.ok(patient.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> createOrUpdateProfile(@RequestBody Patient profileRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        Optional<Patient> existingPatient = patientRepository.findByUserId(user.getId());

        Patient patient;
        if (existingPatient.isPresent()) {
            patient = existingPatient.get();
        } else {
            patient = new Patient();
            patient.setUser(user);
        }

        patient.setFirstName(profileRequest.getFirstName());
        patient.setLastName(profileRequest.getLastName());
        patient.setDateOfBirth(profileRequest.getDateOfBirth());
        patient.setGender(profileRequest.getGender());
        patient.setContactNumber(profileRequest.getContactNumber());
        patient.setAddress(profileRequest.getAddress());
        patient.setMedicalHistory(profileRequest.getMedicalHistory());
        patient.setAllergies(profileRequest.getAllergies());
        patient.setBloodGroup(profileRequest.getBloodGroup());
        patient.setEmergencyContactName(profileRequest.getEmergencyContactName());
        patient.setEmergencyContactNumber(profileRequest.getEmergencyContactNumber());
        patient.setInsuranceProvider(profileRequest.getInsuranceProvider());
        patient.setInsurancePolicyNumber(profileRequest.getInsurancePolicyNumber());

        patientRepository.save(patient);

        return ResponseEntity.ok(new MessageResponse("Patient profile updated successfully!"));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
        Optional<Patient> patient = patientRepository.findById(id);

        if (patient.isPresent()) {
            return ResponseEntity.ok(patient.get());
        }

        return ResponseEntity.notFound().build();
    }
}
