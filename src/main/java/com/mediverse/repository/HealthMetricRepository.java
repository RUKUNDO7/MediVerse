package com.mediverse.repository;

import com.mediverse.model.HealthMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric, Long> {
    List<HealthMetric> findByPatientIdOrderByRecordedAtDesc(Long patientId);
    List<HealthMetric> findByPatientIdAndTypeOrderByRecordedAtDesc(Long patientId, String type);
}
