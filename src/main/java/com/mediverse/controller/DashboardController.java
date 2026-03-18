package com.mediverse.controller;

import com.mediverse.repository.PatientRepository;
import com.mediverse.repository.DoctorRepository;
import com.mediverse.repository.AppointmentRepository;
import com.mediverse.repository.PaymentRepository;
import com.mediverse.repository.DepartmentRepository;
import com.mediverse.model.Doctor;
import com.mediverse.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    DoctorRepository doctorRepository;

    @Autowired
    AppointmentRepository appointmentRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminStats() {
        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();
        long totalAppointments = appointmentRepository.count();

        // Calculate Revenue
        java.math.BigDecimal revenue = paymentRepository.sumAmountByStatus(com.mediverse.model.Payment.PaymentStatus.PAID);
        if (revenue == null) revenue = java.math.BigDecimal.ZERO;

        // Department Stats
        Map<String, Long> doctorDistribution = new HashMap<>();
        departmentRepository.findAll().forEach(dept -> {
            doctorDistribution.put(dept.getName(), doctorRepository.countByDepartmentId(dept.getId()));
        });

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPatients", totalPatients);
        stats.put("totalDoctors", totalDoctors);
        stats.put("totalAppointments", totalAppointments);
        stats.put("revenue", revenue);
        stats.put("doctorDistribution", doctorDistribution);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/doctor/stats")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getDoctorStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Doctor profile not found."));

        long totalAppointments = appointmentRepository.findByDoctorId(doctor.getId()).size();
        long pendingAppointments = appointmentRepository.findByDoctorId(doctor.getId()).stream()
                .filter(a -> a.getStatus() == com.mediverse.model.Appointment.AppointmentStatus.PENDING)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAppointments", totalAppointments);
        stats.put("pendingAppointments", pendingAppointments);
        stats.put("specialization", doctor.getSpecialization());

        return ResponseEntity.ok(stats);
    }
}
