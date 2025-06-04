
package com.ericsson.controller;

import com.ericsson.service.SimulatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimulatorController.class)
//@WithMockUser(authorities = "ROLE_ADMIN")
public class SimulatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SimulatorService simulatorService;

    @Test
    void testGenerateHistoricalData_validCount() throws Exception {
        int validCount = 100;
        mockMvc.perform(post("/api/simulator/historical/" + validCount)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Historical data generation started for 100 records"));
    }

    @Test
    void testGenerateHistoricalData_exceedsLimit() throws Exception {
        int invalidCount = 600;
        mockMvc.perform(post("/api/simulator/historical/" + invalidCount)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Maximum 500 records allowed to prevent system overload"));
    }

    @Test
    void testSimulateScenario() throws Exception {
        String scenario = "latency-spike";
        int nodeId = 101;
        mockMvc.perform(post("/api/simulator/scenario/" + scenario + "/node/" + nodeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Simulated scenario: latency-spike on node 101"));
    }

    @Test
    void testGenerateTestScenarios() throws Exception {
        mockMvc.perform(post("/api/simulator/test-scenarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Test scenarios generated successfully"));
    }

    @Test
    void testTestAllScenarios() throws Exception {
        mockMvc.perform(post("/api/simulator/scenario/test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("All scenarios tested successfully"));
    }
}
