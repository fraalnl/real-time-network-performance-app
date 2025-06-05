package com.ericsson.controller;

import com.ericsson.model.PerformanceData;
import com.ericsson.service.MetricsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(MetricsController.class)
class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricsService metricsService;

    private PerformanceData createTestData() {
        PerformanceData data = new PerformanceData();
        data.setNodeId(1);
        data.setNetworkId(100);
        data.setLatency(50.0);
        data.setThroughput(100.0);
        data.setErrorRate(1.0);
        data.setTimestamp(LocalDateTime.now());
        return data;
    }

    @Test
    void testGetRealTimeMetrics() throws Exception {
        PerformanceData testData = createTestData();
        Mockito.when(metricsService.getLatestMetricsForAllNodes()).thenReturn(Collections.singletonList(testData));

        mockMvc.perform(get("/api/metrics/realtime"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nodeId").value(1));
    }

    @Test
    void testGetKpiSummary() throws Exception {
        Map<String, Object> summary = Map.of(
                "totalNodes", 2,
                "healthyNodes", 1,
                "criticalNodes", 1,
                "averageLatency", 100.0,
                "systemStatus", "CRITICAL"
        );

        Mockito.when(metricsService.getKpiSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/metrics/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalNodes").value(2))
                .andExpect(jsonPath("$.systemStatus").value("CRITICAL"));
    }

    @Test
    void testGetAnomalyAlerts() throws Exception {
        PerformanceData alertData = createTestData();
        Mockito.when(metricsService.detectAnomalies()).thenReturn(Collections.singletonList(alertData));

        mockMvc.perform(get("/api/metrics/alerts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nodeId").value(1));
    }

    @Test
    void testGetNodesByHealthStatus() throws Exception {
        PerformanceData healthy = createTestData();
        Map<String, List<PerformanceData>> healthGroups = Map.of(
                "healthy", Collections.singletonList(healthy),
                "critical", Collections.emptyList(),
                "warning", Collections.emptyList()
        );

        Mockito.when(metricsService.getNodesByHealthStatus()).thenReturn(healthGroups);

        mockMvc.perform(get("/api/metrics/health-status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.healthy[0].nodeId").value(1))
                .andExpect(jsonPath("$.critical").isEmpty())
                .andExpect(jsonPath("$.warning").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN") // or "ENGINEER"
    void testGetSummaryForRange() throws Exception {
        String range = "last_1_hour";

        Map<String, Object> mockSummary = new HashMap<>();
        mockSummary.put("range", range);
        mockSummary.put("avgLatency", 123.45);
        mockSummary.put("avgThroughput", 456.78);
        mockSummary.put("avgErrorRate", 0.01);
        mockSummary.put("sampleSize", 50);

        Mockito.when(metricsService.getKpiSummaryForRange(range)).thenReturn(mockSummary);

        mockMvc.perform(get("/api/metrics/summary/range")
                        .param("range", range)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range").value(range))
                .andExpect(jsonPath("$.avgLatency").value(123.45))
                .andExpect(jsonPath("$.avgThroughput").value(456.78))
                .andExpect(jsonPath("$.avgErrorRate").value(0.01))
                .andExpect(jsonPath("$.sampleSize").value(50));
    }


}

