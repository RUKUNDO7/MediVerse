package com.mediverse.controller;

import com.mediverse.model.Appointment;
import com.mediverse.model.Patient;
import com.mediverse.model.Payment;
import com.mediverse.model.Payment.PaymentStatus;
import com.mediverse.repository.AppointmentRepository;
import com.mediverse.repository.PatientRepository;
import com.mediverse.repository.PaymentRepository;
import com.mediverse.security.MessageResponse;
import com.mediverse.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    AppointmentRepository appointmentRepository;

    @PostMapping("/generate/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<?> generateInvoice(
            @PathVariable Long appointmentId,
            @RequestParam("amount") BigDecimal amount) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Error: Appointment not found."));

        Patient patient = appointment.getPatient();

        Payment payment = new Payment();
        payment.setPatient(patient);
        payment.setAppointment(appointment);
        payment.setAmount(amount);
        payment.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setStatus(PaymentStatus.UNPAID);
        
        // Setup insurance context if available
        if (patient.getInsuranceProvider() != null && !patient.getInsuranceProvider().isEmpty()) {
             payment.setPaymentMethod("Insurance");
             payment.setInsuranceClaimId("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
             payment.setStatus(PaymentStatus.PENDING); // Pending insurance approval
        }

        paymentRepository.save(payment);

        return ResponseEntity.ok(new MessageResponse("Invoice generated successfully. Invoice Number: " + payment.getInvoiceNumber()));
    }

    @PutMapping("/pay/{paymentId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<?> markAsPaid(
            @PathVariable Long paymentId,
            @RequestParam("paymentMethod") String paymentMethod) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Error: Payment record not found."));

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(paymentMethod);

        paymentRepository.save(payment);

        return ResponseEntity.ok(new MessageResponse("Payment marked as PAID."));
    }

    @GetMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<Payment>> getPatientInvoices() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Patient patient = patientRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Patient profile not found."));

        return ResponseEntity.ok(paymentRepository.findByPatientId(patient.getId()));
    }
}
