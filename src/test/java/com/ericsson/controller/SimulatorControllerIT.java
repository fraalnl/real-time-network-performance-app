package com.ericsson.controller;

import com.ericsson.service.SimulatorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = {"ADMIN"})
class SimulatorControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SimulatorService simulatorService;

    @Test
    void testGenerateHistoricalData_validCount_returnsOk() throws Exception {
        mockMvc.perform(post("/api/simulator/historical/100"))
                .andExpect(status().isOk())
                .andExpect(content().string("Generating 100 historical records with JavaFaker"));

        verify(simulatorService, times(1)).generateHistoricalBatch(100);
    }

    @Test
    void testGenerateHistoricalData_countTooLarge_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/simulator/historical/600"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Maximum 500 records allowed to prevent system overload"));

        Mockito.verifyNoInteractions(simulatorService);
    }

    @Test
    void testSimulateScenario_validNode_returnsOk() throws Exception {
        mockMvc.perform(post("/api/simulator/scenario/congestion/node/105"))
                .andExpect(status().isOk())
                .andExpect(content().string("Simulated congestion scenario for Node 105"));

        verify(simulatorService, times(1)).simulateNetworkScenario("congestion", 105);
    }

    @Test
    void testSimulateScenario_invalidNode_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/simulator/scenario/congestion/node/99"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Node ID must be between 100-119"));

        Mockito.verifyNoInteractions(simulatorService);
    }

    @Test
    void testGetSimulationStats_returnsOk() throws Exception {
        Mockito.when(simulatorService.getSimulationStats())
                .thenReturn(Map.of("totalSimulations", 42, "activeNodes", 5));

        mockMvc.perform(get("/api/simulator/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSimulations").value(42))
                .andExpect(jsonPath("$.activeNodes").value(5));

        verify(simulatorService, times(1)).getSimulationStats();
    }

    @Test
    void testGenerateTestScenarios_returnsOk() throws Exception {
        mockMvc.perform(post("/api/simulator/test-scenarios"))
                .andExpect(status().isOk())
                .andExpect(content().string("Generated diverse test scenarios for dashboard demonstration"));

        verify(simulatorService, times(1)).generateDiverseTestData();
    }

    @Test
    void testTestAllScenarios_returnsOk() throws Exception {
        mockMvc.perform(post("/api/simulator/scenario/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Generated test data for all scenario types"));

        verify(simulatorService, times(1)).simulateNetworkScenario("optimal", 100);
        verify(simulatorService, times(1)).simulateNetworkScenario("baseline", 101);
        verify(simulatorService, times(1)).simulateNetworkScenario("congestion", 102);
        verify(simulatorService, times(1)).simulateNetworkScenario("unstable", 103);
        verify(simulatorService, times(1)).simulateNetworkScenario("critical", 104);
    }
}
