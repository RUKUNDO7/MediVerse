package com.mediverse.controller;

import com.mediverse.model.AvailabilitySlot;
import com.mediverse.model.Doctor;
import com.mediverse.repository.AvailabilitySlotRepository;
import com.mediverse.repository.DoctorRepository;
import com.mediverse.security.MessageResponse;
import com.mediverse.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/availability")
public class AvailabilitySlotController {

    @Autowired
    AvailabilitySlotRepository slotRepository;

    @Autowired
    DoctorRepository doctorRepository;

    @PostMapping("/add")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> addSlots(@RequestBody Map<String, Object> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Doctor profile not found."));

        LocalDate date = LocalDate.parse(request.get("date").toString());
        @SuppressWarnings("unchecked")
        List<String> startTimesStr = (List<String>) request.get("startTimes");
        int durationMinutes = Integer.parseInt(request.getOrDefault("durationMinutes", "30").toString());

        for (String startTimeStr : startTimesStr) {
            LocalTime startTime = LocalTime.parse(startTimeStr);
            LocalTime endTime = startTime.plusMinutes(durationMinutes);

            AvailabilitySlot slot = new AvailabilitySlot();
            slot.setDoctor(doctor);
            slot.setDate(date);
            slot.setStartTime(startTime);
            slot.setEndTime(endTime);
            slotRepository.save(slot);
        }

        return ResponseEntity.ok(new MessageResponse("Availability slots added successfully."));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AvailabilitySlot>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam("date") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return ResponseEntity.ok(slotRepository.findByDoctorIdAndDateAndIsBookedFalseOrderByStartTimeAsc(doctorId, date));
    }
}
