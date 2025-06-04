package com.ericsson.service;

import com.ericsson.model.PerformanceData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WithMockUser(roles = "ADMIN")
class MetricsServiceIT {

    @Autowired
    private MetricsService metricsService;

    @Test
    void testGetLatestMetricsForAllNodes() {
        List<PerformanceData> latestMetrics = metricsService.getLatestMetricsForAllNodes();

        assertThat(latestMetrics).isNotEmpty();
        // Further checks can include checking specific node IDs if known
        System.out.println("Latest metrics: " + latestMetrics);
    }

    @Test
    void testGetKpiSummary_withRealData() {
        Map<String, Object> summary = metricsService.getKpiSummary();

        assertThat(summary).isNotEmpty();
        assertThat(summary.get("totalNodes")).isInstanceOf(Integer.class);
        assertThat(summary.get("healthyNodes")).isInstanceOf(Integer.class);
        assertThat(summary.get("averageLatency")).isInstanceOf(Double.class);
        assertThat(summary.get("networkSummary")).isInstanceOf(Map.class);

        System.out.println("KPI Summary: " + summary);
    }

    @Test
    void testDetectAnomalies() {
        List<PerformanceData> anomalies = metricsService.detectAnomalies();

        assertThat(anomalies).isNotNull();
        // Further checks for known node IDs or thresholds if needed
        System.out.println("Anomalies detected: " + anomalies);
    }

    @Test
    void testGetNodesByHealthStatus() {
        Map<String, List<PerformanceData>> grouped = metricsService.getNodesByHealthStatus();

        assertThat(grouped).containsKeys("healthy", "warning", "critical");
        // Validate at least one group is not empty, depending on your real data
        System.out.println("Nodes by health status: " + grouped);
    }
}

