package com.ericsson.service;

import com.ericsson.model.PerformanceData;
import com.ericsson.repository.PerformanceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);

    private final PerformanceDataRepository repository;

    // Anomaly detection thresholds
    private static final double HIGH_LATENCY_THRESHOLD = 100.0; // ms
    private static final double LOW_THROUGHPUT_THRESHOLD = 50.0; // Mbps
    private static final double HIGH_ERROR_RATE_THRESHOLD = 2.0; // %

    @Autowired
    public MetricsService(PerformanceDataRepository repository) {
        this.repository = repository;
    }

    /**
     * Get the latest metrics for all nodes (one record per node)
     * This is critical for real-time dashboard display
     */
    public List<PerformanceData> getLatestMetricsForAllNodes() {
        List<PerformanceData> allData = repository.findAll();

        if (allData.isEmpty()) {
            logger.warn("No performance data found in database");
            return new ArrayList<>();
        }

        // Group by nodeId and get the latest timestamp for each
        Map<Integer, PerformanceData> latestByNode = allData.stream()
                .collect(Collectors.toMap(
                        PerformanceData::getNodeId,
                        data -> data,
                        (existing, replacement) ->
                                existing.getTimestamp().isAfter(replacement.getTimestamp()) ? existing : replacement
                ));

        List<PerformanceData> result = new ArrayList<>(latestByNode.values());

        // Sort by node ID for consistent ordering
        result.sort(Comparator.comparing(PerformanceData::getNodeId));

        logger.debug("Retrieved latest metrics for {} nodes", result.size());
        return result;
    }


    /**
     * Get KPI summary for dashboard overview
     */
    public Map<String, Object> getKpiSummary() {
        List<PerformanceData> latestMetrics = getLatestMetricsForAllNodes();

        if (latestMetrics.isEmpty()) {
            logger.info("No metrics available for KPI summary");
            Map<String, Object> emptyResponse = new HashMap<>();
            emptyResponse.put("message", "No data available");
            emptyResponse.put("totalNodes", 0);
            emptyResponse.put("healthyNodes", 0);
            emptyResponse.put("averageLatency", 0.0);
            emptyResponse.put("averageThroughput", 0.0);
            emptyResponse.put("averageErrorRate", 0.0);
            emptyResponse.put("lastUpdated", LocalDateTime.now());
            return emptyResponse;
        }

        // Calculate averages
        double avgLatency = latestMetrics.stream()
                .mapToDouble(PerformanceData::getLatency)
                .average().orElse(0.0);

        double avgThroughput = latestMetrics.stream()
                .mapToDouble(PerformanceData::getThroughput)
                .average().orElse(0.0);

        double avgErrorRate = latestMetrics.stream()
                .mapToDouble(PerformanceData::getErrorRate)
                .average().orElse(0.0);

        // Calculate health metrics
        int totalNodes = latestMetrics.size();
        int healthyNodes = (int) latestMetrics.stream()
                .filter(this::isNodeHealthy)
                .count();

        int criticalNodes = (int) latestMetrics.stream()
                .filter(this::isNodeCritical)
                .count();

        // Calculate min/max values for context
        double maxLatency = latestMetrics.stream()
                .mapToDouble(PerformanceData::getLatency)
                .max().orElse(0.0);

        double minThroughput = latestMetrics.stream()
                .mapToDouble(PerformanceData::getThroughput)
                .min().orElse(0.0);

        double maxErrorRate = latestMetrics.stream()
                .mapToDouble(PerformanceData::getErrorRate)
                .max().orElse(0.0);

        // Build summary response
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalNodes", totalNodes);
        summary.put("healthyNodes", healthyNodes);
        summary.put("criticalNodes", criticalNodes);
        summary.put("healthPercentage", totalNodes > 0 ? Math.round((healthyNodes * 100.0) / totalNodes) : 0);

        // Average KPIs (rounded for display)
        summary.put("averageLatency", Math.round(avgLatency * 100.0) / 100.0);
        summary.put("averageThroughput", Math.round(avgThroughput * 100.0) / 100.0);
        summary.put("averageErrorRate", Math.round(avgErrorRate * 1000.0) / 1000.0);

        // Min/Max values for context
        summary.put("maxLatency", Math.round(maxLatency * 100.0) / 100.0);
        summary.put("minThroughput", Math.round(minThroughput * 100.0) / 100.0);
        summary.put("maxErrorRate", Math.round(maxErrorRate * 1000.0) / 1000.0);

        // System status
        summary.put("systemStatus", determineSystemStatus(healthyNodes, totalNodes, criticalNodes));
        summary.put("lastUpdated", LocalDateTime.now());

        // Network-level summaries
        summary.put("networkSummary", getNetworkSummary(latestMetrics));

        logger.info("Generated KPI summary: {}/{} healthy nodes, avg latency: {}ms",
                healthyNodes, totalNodes, Math.round(avgLatency * 100.0) / 100.0);
        return summary;
    }

    /**
     * Get summary by network
     */
    private Map<String, Object> getNetworkSummary(List<PerformanceData> latestMetrics) {
        Map<Integer, List<PerformanceData>> byNetwork = latestMetrics.stream()
                .collect(Collectors.groupingBy(PerformanceData::getNetworkId));

        Map<String, Object> networkSummary = new HashMap<>();

        byNetwork.forEach((networkId, nodes) -> {
            Map<String, Object> netStats = new HashMap<>();
            netStats.put("totalNodes", nodes.size());
            netStats.put("healthyNodes", nodes.stream().mapToInt(node -> isNodeHealthy(node) ? 1 : 0).sum());
            netStats.put("avgLatency", Math.round(nodes.stream().mapToDouble(PerformanceData::getLatency).average().orElse(0.0) * 100.0) / 100.0);
            netStats.put("avgThroughput", Math.round(nodes.stream().mapToDouble(PerformanceData::getThroughput).average().orElse(0.0) * 100.0) / 100.0);
            netStats.put("avgErrorRate", Math.round(nodes.stream().mapToDouble(PerformanceData::getErrorRate).average().orElse(0.0) * 1000.0) / 1000.0);

            networkSummary.put("network_" + networkId, netStats);
        });

        return networkSummary;
    }

    /**
     * Determine overall system status
     */
    private String determineSystemStatus(int healthyNodes, int totalNodes, int criticalNodes) {
        if (totalNodes == 0) return "NO_DATA";

        double healthPercentage = (healthyNodes * 100.0) / totalNodes;

        if (criticalNodes > 0) return "CRITICAL";
        if (healthPercentage >= 90) return "HEALTHY";
        if (healthPercentage >= 70) return "WARNING";
        return "DEGRADED";
    }

    /**
     * Detect anomalies for network engineer alerts
     */
    public List<PerformanceData> detectAnomalies() {
        List<PerformanceData> latestMetrics = getLatestMetricsForAllNodes();

        List<PerformanceData> anomalies = latestMetrics.stream()
                .filter(data -> !isNodeHealthy(data))
                .sorted((a, b) -> {
                    // Sort by severity: critical first, then by worst latency
                    boolean aCritical = isNodeCritical(a);
                    boolean bCritical = isNodeCritical(b);

                    if (aCritical && !bCritical) return -1;
                    if (!aCritical && bCritical) return 1;

                    // Both same severity level, sort by latency
                    return Double.compare(b.getLatency(), a.getLatency());
                })
                .toList();

        logger.info("Detected {} anomalies out of {} nodes", anomalies.size(), latestMetrics.size());
        return anomalies;
    }

    /**
     * Get nodes grouped by health status
     */
    public Map<String, List<PerformanceData>> getNodesByHealthStatus() {
        List<PerformanceData> latestMetrics = getLatestMetricsForAllNodes();

        Map<String, List<PerformanceData>> grouped = new HashMap<>();
        grouped.put("healthy", new ArrayList<>());
        grouped.put("warning", new ArrayList<>());
        grouped.put("critical", new ArrayList<>());

        for (PerformanceData data : latestMetrics) {
            if (isNodeCritical(data)) {
                grouped.get("critical").add(data);
            } else if (isNodeHealthy(data)) {
                grouped.get("healthy").add(data);
            } else {
                grouped.get("warning").add(data);
            }
        }

        logger.debug("Grouped nodes by health: {} healthy, {} warning, {} critical",
                grouped.get("healthy").size(), grouped.get("warning").size(), grouped.get("critical").size());

        return grouped;
    }


    /**
     * Check if a node is healthy based on thresholds
     */
    private boolean isNodeHealthy(PerformanceData data) {
        return data.getLatency() < HIGH_LATENCY_THRESHOLD &&
                data.getThroughput() > LOW_THROUGHPUT_THRESHOLD &&
                data.getErrorRate() < HIGH_ERROR_RATE_THRESHOLD;
    }

    /**
     * Check if a node is in critical state
     */
    private boolean isNodeCritical(PerformanceData data) {
        return data.getLatency() > (HIGH_LATENCY_THRESHOLD * 1.5) ||
                data.getThroughput() < (LOW_THROUGHPUT_THRESHOLD * 0.5) ||
                data.getErrorRate() > (HIGH_ERROR_RATE_THRESHOLD * 2.0);
    }

    // Get KPI summary for a specific time range for the historical data analysis
    public Map<String, Object> getKpiSummaryForRange(String range) {
        LocalDateTime startTime = switch (range.toLowerCase()) {
            case "last_5_minutes" -> LocalDateTime.now().minusMinutes(5);
            case "last_1_hour" -> LocalDateTime.now().minusHours(1);
            case "last_24_hours" -> LocalDateTime.now().minusHours(24);
            default -> throw new IllegalArgumentException("Invalid range");
        };

        List<PerformanceData> recent = repository.findByTimestampAfter(startTime);

        double avgLatency = recent.stream().mapToDouble(PerformanceData::getLatency).average().orElse(0);
        double avgThroughput = recent.stream().mapToDouble(PerformanceData::getThroughput).average().orElse(0);
        double avgErrorRate = recent.stream().mapToDouble(PerformanceData::getErrorRate).average().orElse(0);

        Map<String, Object> result = new HashMap<>();
        result.put("range", range);
        result.put("avgLatency", avgLatency);
        result.put("avgThroughput", avgThroughput);
        result.put("avgErrorRate", avgErrorRate);
        result.put("sampleSize", recent.size());

        return result;
    }


}