package com.ericsson.repository;

import com.ericsson.model.PerformanceData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PerformanceDataRepository extends JpaRepository<PerformanceData, Long> {
    List<PerformanceData> findByTimestampAfter(LocalDateTime startTime); //automatically generates an SQL query
}

