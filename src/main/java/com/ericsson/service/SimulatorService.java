package com.ericsson.service;

import com.ericsson.model.PerformanceData;
import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@EnableScheduling
public class SimulatorService {

    private static final Logger logger = LoggerFactory.getLogger(SimulatorService.class);

    private final NetworkPerformancePublisher publisher;
    private final Faker faker = new Faker();
    private final Random random = new Random();

    // Network topology (matching your DataInitializer)
    private final int[] nodeIds = {100, 101, 102, 103, 104, 105, 106, 107, 108, 109,
            110, 111, 112, 113, 114, 115, 116, 117, 118, 119};
    private final int[] networkIds = {201, 202, 203, 204};

    // Node characteristics for realistic variation
    private final Map<Integer, NodeCharacteristics> nodeProfiles = new HashMap<>();

    // Performance baselines
    private static final double BASE_LATENCY = 25.0;
    private static final double BASE_THROUGHPUT = 100.0;
    private static final double BASE_ERROR_RATE = 0.1;

    @Autowired
    public SimulatorService(NetworkPerformancePublisher publisher) {
        this.publisher = publisher;
        initializeNodeProfiles();
    }

    /**
     * Initialize unique characteristics for each node using JavaFaker
     */
    private void initializeNodeProfiles() {
        for (int nodeId : nodeIds) {
            NodeCharacteristics profile = new NodeCharacteristics();

            // Use JavaFaker number().numberBetween() which works correctly
            profile.latencyModifier = faker.number().numberBetween(-5, 10); // -5 to +10ms
            profile.throughputModifier = faker.number().numberBetween(-15, 20); // -15 to +20 Mbps
            profile.stabilityFactor = 0.8 + (random.nextDouble() * 0.4); // 0.8 to 1.2
            profile.congestionProbability = 0.02 + (random.nextDouble() * 0.13); // 2-15% chance

            nodeProfiles.put(nodeId, profile);
        }
        logger.info("Initialized {} unique node profiles using JavaFaker", nodeProfiles.size());
    }

    /**
     * Main scheduled method - generates data every 3 seconds
     */
    @Scheduled(fixedRate = 3000)
    @Async
    public void generatePerformanceData() {
        PerformanceData data = createPerformanceData();

        // Publish to Kafka
        publisher.sendPerformanceData(data);

        logger.debug("Generated data for Node {} (Network {}): Latency={}ms, Throughput={}Mbps, ErrorRate={}%",
                data.getNodeId(), data.getNetworkId(), data.getLatency(), data.getThroughput(), data.getErrorRate());
    }

    /**
     * Generate a single historical data point
     */
    private void generateHistoricalDataPoint() {
        PerformanceData data = createPerformanceData();

        // Override timestamp for historical data
        int minutesAgo = faker.number().numberBetween(5, 1440); // Last 24 hours
        LocalDateTime historicalTime = LocalDateTime.now().minusMinutes(minutesAgo);
        data.setTimestamp(historicalTime);

        publisher.sendPerformanceData(data);
    }

    /**
     * Create performance data with current timestamp - extracted common logic
     */
    private PerformanceData createPerformanceData() {
        // Use JavaFaker to select random node
        int nodeIndex = faker.number().numberBetween(0, nodeIds.length - 1);
        int nodeId = nodeIds[nodeIndex];
        int networkId = networkIds[nodeIndex / 5]; // 5 nodes per network

        PerformanceData data = new PerformanceData();
        data.setNodeId(nodeId);
        data.setNetworkId(networkId);
        data.setLatency(generateRealisticLatency(nodeId));
        data.setThroughput(generateRealisticThroughput(nodeId));
        data.setErrorRate(generateRealisticErrorRate(nodeId));
        data.setTimestamp(LocalDateTime.now());

        return data;
    }

    /**
     * Generate realistic latency using JavaFaker number methods that work
     */
    private double generateRealisticLatency(int nodeId) {
        NodeCharacteristics profile = nodeProfiles.get(nodeId);

        // Base latency for this specific node
        double nodeBaseline = BASE_LATENCY + profile.latencyModifier;

        // Use JavaFaker number().numberBetween() for integer range, then convert
        int latencyVariation = faker.number().numberBetween(-3, 8);
        double latency = nodeBaseline + latencyVariation;

        // Add some decimal precision using Random - Fixed: Use nextDouble() instead of * 1.0
        latency += (random.nextDouble() * 2.0 - 1.0); // Add -1 to +1 ms variation

        // Network congestion scenarios
        double congestionChance = random.nextDouble();

        if (congestionChance < profile.congestionProbability) {
            // Light congestion - add 15-40ms
            latency += faker.number().numberBetween(15, 40);
        } else if (congestionChance < profile.congestionProbability * 2) {
            // Heavy congestion - add 50-120ms
            latency += faker.number().numberBetween(50, 120);
        } else if (congestionChance > 0.995) {
            // Rare severe spike - add 150-300ms
            latency += faker.number().numberBetween(150, 300);
        }

        return Math.max(5.0, Math.round(latency * 100.0) / 100.0);
    }

    /**
     * Generate realistic throughput using JavaFaker
     */
    private double generateRealisticThroughput(int nodeId) {
        NodeCharacteristics profile = nodeProfiles.get(nodeId);

        // Base throughput for this specific node
        double nodeCapacity = BASE_THROUGHPUT + profile.throughputModifier;

        // Normal operation (85-98% of capacity)
        double efficiencyPercent = faker.number().numberBetween(85, 98);
        double throughput = nodeCapacity * (efficiencyPercent / 100.0);

        // Add small random variation
        throughput += (random.nextDouble() * 4.0 - 2.0); // ±2 Mbps variation

        // Network issues
        double issueChance = random.nextDouble();

        if (issueChance < 0.05) {
            // Bandwidth limitation (40-70% capacity)
            int limitPercent = faker.number().numberBetween(40, 70);
            throughput = nodeCapacity * (limitPercent / 100.0);
        } else if (issueChance < 0.08) {
            // Severe limitation (15-40% capacity)
            int severePercent = faker.number().numberBetween(15, 40);
            throughput = nodeCapacity * (severePercent / 100.0);
        }

        return Math.max(10.0, Math.round(throughput * 100.0) / 100.0);
    }

    /**
     * Generate realistic error rates using JavaFaker
     */
    private double generateRealisticErrorRate(int nodeId) {
        NodeCharacteristics profile = nodeProfiles.get(nodeId);

        // Base error rate with node stability factor
        double baseErrorRate = BASE_ERROR_RATE * profile.stabilityFactor;

        // Normal operation - use random for small decimal values
        double errorRate = random.nextDouble() * baseErrorRate;

        // Error spike scenarios
        double errorChance = random.nextDouble();

        if (errorChance < 0.02) {
            // Minor error spike - add 0.5-1.5%
            double spikeAmount = 0.5 + random.nextDouble();
            errorRate += spikeAmount;
        }
        // Removed the "always false" condition that SonarQube was complaining about

        return Math.round(errorRate * 1000.0) / 1000.0;
    }

    /**
     * Generate batch of realistic historical data for testing
     */
    public void generateHistoricalBatch(int count) {
        logger.info("Generating {} historical data points with JavaFaker", count);

        for (int i = 0; i < count; i++) {
            generateHistoricalDataPoint();

            try {
                Thread.sleep(100); // Prevent overwhelming Kafka
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Historical data generation interrupted");
                break;
            }
        }

        logger.info("Historical data generation complete");
    }


    /**
     * Simulate specific network scenarios for testing
     * Fixed: Use enhanced switch statement
     */
    public void simulateNetworkScenario(String scenario, int nodeId) {
        PerformanceData data = new PerformanceData();
        data.setNodeId(nodeId);
        data.setNetworkId(networkIds[(nodeId - 100) / 5]);
        data.setTimestamp(LocalDateTime.now());

        switch (scenario.toLowerCase()) {
            case "optimal" -> setOptimalPerformance(data);
            case "congestion" -> setCongestionPerformance(data);
            case "critical" -> setCriticalPerformance(data);
            case "unstable" -> setUnstablePerformance(data);
            case "baseline" -> setBaselinePerformance(data);
            default -> setNormalPerformance(data, nodeId);
        }

        publisher.sendPerformanceData(data);
        logger.info("Simulated {} scenario for Node {} - Latency: {}ms, Throughput: {}Mbps, Error: {}%",
                scenario, nodeId, data.getLatency(), data.getThroughput(), data.getErrorRate());
    }

    /**
     * Set optimal performance metrics
     */
    private void setOptimalPerformance(PerformanceData data) {
        data.setLatency(faker.number().numberBetween(8, 15) + random.nextDouble());
        data.setThroughput(faker.number().numberBetween(110, 125) + random.nextDouble());
        data.setErrorRate(random.nextDouble() * 0.05); // 0-0.05%
    }

    /**
     * Set congestion performance metrics
     */
    private void setCongestionPerformance(PerformanceData data) {
        data.setLatency(faker.number().numberBetween(80, 150) + random.nextDouble());
        data.setThroughput(faker.number().numberBetween(30, 60) + random.nextDouble());
        data.setErrorRate(1.0 + (random.nextDouble() * 2.0)); // 1-3%
    }

    /**
     * Set critical performance metrics
     */
    private void setCriticalPerformance(PerformanceData data) {
        data.setLatency(faker.number().numberBetween(200, 400) + random.nextDouble());
        data.setThroughput(faker.number().numberBetween(5, 25) + random.nextDouble());
        data.setErrorRate(4.0 + (random.nextDouble() * 6.0)); // 4-10%
    }

    /**
     * Set unstable performance metrics
     */
    private void setUnstablePerformance(PerformanceData data) {
        data.setLatency(faker.number().numberBetween(20, 200) + random.nextDouble());
        data.setThroughput(faker.number().numberBetween(40, 120) + random.nextDouble());
        data.setErrorRate(0.5 + (random.nextDouble() * 3.5)); // 0.5-4%
    }

    /**
     * Set baseline performance metrics
     */
    private void setBaselinePerformance(PerformanceData data) {
        data.setLatency(faker.number().numberBetween(20, 35) + random.nextDouble());
        data.setThroughput(faker.number().numberBetween(90, 110) + random.nextDouble());
        data.setErrorRate(random.nextDouble() * 0.2); // 0-0.2%
    }

    /**
     * Set normal random performance metrics - extracted to avoid code duplication
     */
    private void setNormalPerformance(PerformanceData data, int nodeId) {
        data.setLatency(generateRealisticLatency(nodeId));
        data.setThroughput(generateRealisticThroughput(nodeId));
        data.setErrorRate(generateRealisticErrorRate(nodeId));
    }

    /**
     * Generate diverse data for dashboard testing
     */
    public void generateDiverseTestData() {
        logger.info("Generating diverse test data for dashboard");

        // Generate data for each node with different characteristics
        for (int i = 0; i < nodeIds.length; i++) {
            int nodeId = nodeIds[i];
            String scenario = getScenarioForIndex(i);

            simulateNetworkScenario(scenario, nodeId);

            try {
                Thread.sleep(200); // Small delay between generations
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Diverse test data generation interrupted");
                break;
            }
        }

        logger.info("Diverse test data generation complete");
    }

    /**
     * Get scenario type based on index
     */
    private String getScenarioForIndex(int index) {
        String[] scenarios = {"optimal", "baseline", "congestion", "unstable", "critical"};
        return scenarios[index % scenarios.length];
    }

    /**
     * Get simulation statistics
     */
    public Map<String, Object> getSimulationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNodes", nodeIds.length);
        stats.put("totalNetworks", networkIds.length);
        stats.put("averageLatencyBaseline", BASE_LATENCY);
        stats.put("averageThroughputBaseline", BASE_THROUGHPUT);
        stats.put("simulationInterval", "3000ms");
        stats.put("dataGenerator", "JavaFaker");
        stats.put("nodeProfiles", nodeProfiles.size());

        return stats;
    }

    /**
     * Inner class to store node characteristics
     */
    private static class NodeCharacteristics {
        double latencyModifier;
        double throughputModifier;
        double stabilityFactor;
        double congestionProbability;
    }
}