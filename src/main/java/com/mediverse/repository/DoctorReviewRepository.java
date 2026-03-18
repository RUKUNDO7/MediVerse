package com.mediverse.repository;

import com.mediverse.model.DoctorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorReviewRepository extends JpaRepository<DoctorReview, Long> {
    List<DoctorReview> findByDoctorId(Long doctorId);
    boolean existsByAppointmentId(Long appointmentId);
}
