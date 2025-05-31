package com.ericsson.repository;

import com.ericsson.model.PerformanceData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceDataRepository extends JpaRepository<PerformanceData, Long> {
}
