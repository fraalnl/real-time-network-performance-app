package com.ericsson.controller;

import com.ericsson.service.SimulatorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SimulatorController.class)
public class SimulatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SimulatorService simulatorService;

    @Test
    void testGenerateHistoricalData_validCount() throws Exception {
        mockMvc.perform(post("/api/simulator/historical/100"))
                .andExpect(status().isOk())
                .andExpect(content().string("Generating 100 historical records with JavaFaker"));
        Mockito.verify(simulatorService).generateHistoricalBatch(100);
    }

    @Test
    void testGenerateHistoricalData_exceedsLimit() throws Exception {
        mockMvc.perform(post("/api/simulator/historical/600"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Maximum 500 records allowed to prevent system overload"));
    }

    @Test
    void testSimulateScenario_valid() throws Exception {
        mockMvc.perform(post("/api/simulator/scenario/optimal/node/100"))
                .andExpect(status().isOk())
                .andExpect(content().string("Simulated optimal scenario for Node 100"));
        Mockito.verify(simulatorService).simulateNetworkScenario("optimal", 100);
    }

    @Test
    void testSimulateScenario_invalidNodeId() throws Exception {
        mockMvc.perform(post("/api/simulator/scenario/optimal/node/50"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Node ID must be between 100-119"));
    }

    @Test
    void testGetSimulationStats() throws Exception {
        Map<String, Object> mockStats = Map.of("totalNodes", 20, "totalNetworks", 4);
        Mockito.when(simulatorService.getSimulationStats()).thenReturn(mockStats);

        mockMvc.perform(get("/api/simulator/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalNodes").value(20))
                .andExpect(jsonPath("$.totalNetworks").value(4));
    }

    @Test
    void testGenerateTestScenarios() throws Exception {
        mockMvc.perform(post("/api/simulator/test-scenarios"))
                .andExpect(status().isOk())
                .andExpect(content().string("Generated diverse test scenarios for dashboard demonstration"));
        Mockito.verify(simulatorService).generateDiverseTestData();
    }

    @Test
    void testTestAllScenarios() throws Exception {
        mockMvc.perform(post("/api/simulator/scenario/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Generated test data for all scenario types"));
        // We verify that simulateNetworkScenario was called 5 times with expected data
        Mockito.verify(simulatorService).simulateNetworkScenario("optimal", 100);
        Mockito.verify(simulatorService).simulateNetworkScenario("baseline", 101);
        Mockito.verify(simulatorService).simulateNetworkScenario("congestion", 102);
        Mockito.verify(simulatorService).simulateNetworkScenario("unstable", 103);
        Mockito.verify(simulatorService).simulateNetworkScenario("critical", 104);
    }
}

