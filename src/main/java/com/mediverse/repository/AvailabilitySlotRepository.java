package com.mediverse.repository;

import com.mediverse.model.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    List<AvailabilitySlot> findByDoctorIdAndDateAndIsBookedFalseOrderByStartTimeAsc(Long doctorId, LocalDate date);
    Optional<AvailabilitySlot> findByIdAndIsBookedFalse(Long id);
}
