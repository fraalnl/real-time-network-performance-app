package com.ericsson.service;

import com.ericsson.model.PerformanceData;
import com.ericsson.repository.PerformanceDataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@WithMockUser(roles = "ADMIN")
class MetricsServiceIT {

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private PerformanceDataRepository repository;

    @Test
    void testGetLatestMetricsForAllNodes() {
        List<PerformanceData> latestMetrics = metricsService.getLatestMetricsForAllNodes();

        assertThat(latestMetrics).isNotEmpty();
    }

    @Test
    void testGetKpiSummary_withRealData() {
        Map<String, Object> summary = metricsService.getKpiSummary();

        assertThat(summary).isNotEmpty();
        assertThat(summary.get("totalNodes")).isInstanceOf(Integer.class);
        assertThat(summary.get("healthyNodes")).isInstanceOf(Integer.class);
        assertThat(summary.get("averageLatency")).isInstanceOf(Double.class);
        assertThat(summary.get("networkSummary")).isInstanceOf(Map.class);
    }

    @Test
    void testDetectAnomalies() {
        List<PerformanceData> anomalies = metricsService.detectAnomalies();

        assertThat(anomalies).isNotNull();
    }

    @Test
    void testGetNodesByHealthStatus() {
        Map<String, List<PerformanceData>> grouped = metricsService.getNodesByHealthStatus();

        assertThat(grouped).containsKeys("healthy", "warning", "critical");
        // Validate at least one group is not empty, depending on your real data
    }

    @Test
    void testGetKpiSummaryForRangeForLast1Hour() {
        int testNodeId = 9999;

        PerformanceData data1 = new PerformanceData();
        data1.setTimestamp(LocalDateTime.now().minusMinutes(30));
        data1.setLatency(100.0);
        data1.setThroughput(200.0);
        data1.setErrorRate(0.01);
        data1.setNodeId(testNodeId);
        data1.setNetworkId(1);

        PerformanceData data2 = new PerformanceData();
        data2.setTimestamp(LocalDateTime.now().minusMinutes(45));
        data2.setLatency(150.0);
        data2.setThroughput(250.0);
        data2.setErrorRate(0.02);
        data2.setNodeId(testNodeId);
        data2.setNetworkId(1);

        repository.saveAll(List.of(data1, data2));

        Map<String, Object> result = metricsService.getKpiSummaryForRange("last_1_hour");

        assertNotNull(result);
        assertTrue(result.containsKey("avgLatency"));
        assertTrue(result.containsKey("avgThroughput"));
        assertTrue(result.containsKey("avgErrorRate"));

        assertTrue(result.get("avgLatency") instanceof Double);
        assertTrue(result.get("avgThroughput") instanceof Double);
        assertTrue(result.get("avgErrorRate") instanceof Double);
    }
}

