package com.ericsson.controller;

import com.ericsson.service.SimulatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/simulator")
@CrossOrigin(origins = "*")
public class SimulatorController {

    private final SimulatorService simulatorService;

    @Autowired
    public SimulatorController(SimulatorService simulatorService) {
        this.simulatorService = simulatorService;
    }

    /**
     * Generate historical data for testing
     */
    @PostMapping("/historical/{count}")
    public ResponseEntity<String> generateHistoricalData(@PathVariable int count) {
        if (count > 500) {
            return ResponseEntity.badRequest()
                    .body("Maximum 500 records allowed to prevent system overload");
        }

        simulatorService.generateHistoricalBatch(count);
        return ResponseEntity.ok("Generating " + count + " historical records with JavaFaker");
    }

    /**
     * Simulate specific network scenario
     */
    @PostMapping("/scenario/{scenario}/node/{nodeId}")
    public ResponseEntity<String> simulateScenario(
            @PathVariable String scenario,
            @PathVariable int nodeId) {

        // Validate nodeId
        if (nodeId < 100 || nodeId > 119) {
            return ResponseEntity.badRequest()
                    .body("Node ID must be between 100-119");
        }

        simulatorService.simulateNetworkScenario(scenario, nodeId);
        return ResponseEntity.ok("Simulated " + scenario + " scenario for Node " + nodeId);
    }

    /**
     * Get simulation statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSimulationStats() {
        Map<String, Object> stats = simulatorService.getSimulationStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Generate batch scenarios for dashboard testing
     */
    @PostMapping("/test-scenarios")
    public ResponseEntity<String> generateTestScenarios() {
        simulatorService.generateDiverseTestData();
        return ResponseEntity.ok("Generated diverse test scenarios for dashboard demonstration");
    }

    /**
     * Test specific scenario for a node
     */
    @PostMapping("/scenario/test")
    public ResponseEntity<String> testAllScenarios() {
        // Test all scenario types
        simulatorService.simulateNetworkScenario("optimal", 100);
        simulatorService.simulateNetworkScenario("baseline", 101);
        simulatorService.simulateNetworkScenario("congestion", 102);
        simulatorService.simulateNetworkScenario("unstable", 103);
        simulatorService.simulateNetworkScenario("critical", 104);

        return ResponseEntity.ok("Generated test data for all scenario types");
    }
}