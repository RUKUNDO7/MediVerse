package com.mediverse.controller;

import com.mediverse.model.Appointment;
import com.mediverse.model.Appointment.AppointmentStatus;
import com.mediverse.model.Doctor;
import com.mediverse.model.Patient;
import com.mediverse.repository.AppointmentRepository;
import com.mediverse.repository.DoctorRepository;
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
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    AppointmentRepository appointmentRepository;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    DoctorRepository doctorRepository;

    @Autowired
    com.mediverse.repository.AvailabilitySlotRepository slotRepository;

    @Autowired
    com.mediverse.service.EmailService emailService;

    @PostMapping("/book")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> bookAppointment(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Patient patient = patientRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Patient profile not found. Please complete profile first."));

        Long doctorId = Long.parseLong(request.get("doctorId"));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Error: Doctor not found."));

        LocalDateTime appointmentDate;

        // If slotId is provided, mark it as booked
        if (request.containsKey("slotId")) {
            Long slotId = Long.parseLong(request.get("slotId"));
            com.mediverse.model.AvailabilitySlot slot = slotRepository.findById(slotId)
                    .orElseThrow(() -> new RuntimeException("Error: Slot is not available."));
            if (slot.isBooked()) {
                 throw new RuntimeException("Error: Slot is already booked.");
            }
            slot.setBooked(true);
            slotRepository.save(slot);
            
            // Override appointment date with slot exact time
            appointmentDate = LocalDateTime.of(slot.getDate(), slot.getStartTime());
        } else {
            appointmentDate = LocalDateTime.parse(request.get("appointmentDate"));
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setReasonForVisit(request.get("reasonForVisit"));
        appointment.setVirtual(Boolean.parseBoolean(request.getOrDefault("isVirtual", "false")));
        appointment.setStatus(AppointmentStatus.PENDING);

        appointmentRepository.save(appointment);

        emailService.sendAppointmentBookedEmail(
            patient.getUser().getEmail(), 
            patient.getFirstName() + " " + patient.getLastName(), 
            doctor.getLastName(), 
            appointmentDate.toString(), 
            appointment.isVirtual()
        );

        return ResponseEntity.ok(new MessageResponse("Appointment booked successfully. Awaiting approval."));
    }

    @GetMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getPatientAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Patient patient = patientRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Patient profile not found."));

        List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getDoctorAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Doctor profile not found."));

        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctor.getId());
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Appointment not found."));

        AppointmentStatus status = AppointmentStatus.valueOf(request.get("status").toUpperCase());
        appointment.setStatus(status);

        if (request.containsKey("meetingLink")) {
            appointment.setMeetingLink(request.get("meetingLink"));
        }

        appointmentRepository.save(appointment);

        emailService.sendAppointmentStatusEmail(
            appointment.getPatient().getUser().getEmail(), 
            appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName(), 
            status.toString(), 
            appointment.getMeetingLink()
        );

        return ResponseEntity.ok(new MessageResponse("Appointment status updated to " + status + (appointment.getMeetingLink() != null ? " with meeting link." : "")));
    }
}
