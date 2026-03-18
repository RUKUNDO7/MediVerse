package com.mediverse.controller;

import com.mediverse.model.Doctor;
import com.mediverse.model.User;
import com.mediverse.repository.DoctorRepository;
import com.mediverse.repository.UserRepository;
import com.mediverse.security.MessageResponse;
import com.mediverse.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    DoctorRepository doctorRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    com.mediverse.repository.DoctorReviewRepository reviewRepository;

    @Autowired
    com.mediverse.repository.AppointmentRepository appointmentRepository;

    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Optional<Doctor> doctor = doctorRepository.findByUserId(userDetails.getId());

        if (doctor.isPresent()) {
            return ResponseEntity.ok(doctor.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> createOrUpdateProfile(@RequestBody Doctor profileRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        Optional<Doctor> existingDoctor = doctorRepository.findByUserId(user.getId());

        Doctor doctor;
        if (existingDoctor.isPresent()) {
            doctor = existingDoctor.get();
        } else {
            doctor = new Doctor();
            doctor.setUser(user);
        }

        doctor.setFirstName(profileRequest.getFirstName());
        doctor.setLastName(profileRequest.getLastName());
        doctor.setSpecialization(profileRequest.getSpecialization());

        // Note: Department assignment is usually handled by Admin, but omitting complex department linking here for brevity

        doctorRepository.save(doctor);

        return ResponseEntity.ok(new MessageResponse("Doctor profile updated successfully!"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<?> getAllDoctors() {
        return ResponseEntity.ok(doctorRepository.findAll());
    }

    @PostMapping("/{doctorId}/reviews")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> addReview(@PathVariable Long doctorId, @RequestBody Map<String, Object> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Error: Doctor not found."));

        Long appointmentId = Long.valueOf(request.get("appointmentId").toString());
        com.mediverse.model.Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Error: Appointment not found."));

        // Verify appointment belongs to patient and doctor, and is completed
        if (!appointment.getPatient().getUser().getId().equals(userDetails.getId()) || 
            !appointment.getDoctor().getId().equals(doctorId) || 
            appointment.getStatus() != com.mediverse.model.Appointment.AppointmentStatus.COMPLETED) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid or incomplete appointment for review."));
        }

        if (reviewRepository.existsByAppointmentId(appointmentId)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Review already exists for this appointment."));
        }

        int rating = Integer.parseInt(request.get("rating").toString());
        String comment = request.getOrDefault("comment", "").toString();

        com.mediverse.model.DoctorReview review = new com.mediverse.model.DoctorReview();
        review.setDoctor(doctor);
        review.setPatient(appointment.getPatient());
        review.setAppointment(appointment);
        review.setRating(rating);
        review.setComment(comment);

        reviewRepository.save(review);

        // Update doctor stats
        doctor.addReview(rating);
        doctorRepository.save(doctor);

        return ResponseEntity.ok(new MessageResponse("Review submitted successfully!"));
    }

    @GetMapping("/{doctorId}/reviews")
    public ResponseEntity<?> getDoctorReviews(@PathVariable Long doctorId) {
        return ResponseEntity.ok(reviewRepository.findByDoctorId(doctorId));
    }
}
