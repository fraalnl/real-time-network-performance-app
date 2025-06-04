package com.ericsson.controller;

import com.ericsson.model.PerformanceData;
import com.ericsson.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "*") // Enable CORS for your HTML frontend
public class MetricsController {

    private final MetricsService metricsService;

    @Autowired
    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Get real-time metrics for all nodes (latest data)
     * This is what your HTML page calls repeatedly for the dashboard
     */
    @GetMapping("/realtime")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ENGINEER')")
    public ResponseEntity<List<PerformanceData>> getRealTimeMetrics() {
        List<PerformanceData> latestMetrics = metricsService.getLatestMetricsForAllNodes();
        return ResponseEntity.ok(latestMetrics);
    }

    /**
     * Get aggregated KPI summary for dashboard
     * This gives the summary to our main dashboard cards and summary stats
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ENGINEER')")
    public ResponseEntity<Map<String, Object>> getKpiSummary() {
        Map<String, Object> summary = metricsService.getKpiSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Get anomaly alerts (high latency, low throughput, high error rates)
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ENGINEER')")
    public ResponseEntity<List<PerformanceData>> getAnomalyAlerts() {
        List<PerformanceData> alerts = metricsService.detectAnomalies();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get nodes grouped by health status
     */
    @GetMapping("/health-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ENGINEER')")
    public ResponseEntity<Map<String, List<PerformanceData>>> getNodesByHealthStatus() {
        Map<String, List<PerformanceData>> healthGroups = metricsService.getNodesByHealthStatus();
        return ResponseEntity.ok(healthGroups);
    }
}