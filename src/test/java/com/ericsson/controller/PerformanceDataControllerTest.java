package com.ericsson.controller;

import com.ericsson.model.PerformanceData;
import com.ericsson.service.NetworkPerformancePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PerformanceDataControllerTest {

    @Mock
    private NetworkPerformancePublisher publisher;

    @InjectMocks
    private PerformanceDataController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testPublishPerformanceData() throws Exception {
        String json = "{ \"nodeId\": 1, \"networkId\": 2, \"latency\": 25.0, \"throughput\": 100.0, \"errorRate\": 0.5, \"timestamp\": \"2025-06-01T10:15:30\" }";

        mockMvc.perform(post("/api/performance/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Performance data published successfully!"));

        Mockito.verify(publisher).sendPerformanceData(Mockito.any(PerformanceData.class));
    }
}



