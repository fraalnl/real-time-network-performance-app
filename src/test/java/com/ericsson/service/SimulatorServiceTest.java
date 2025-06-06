package com.ericsson.service;

import com.ericsson.model.PerformanceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulatorServiceTest {

    @Mock
    private NetworkPerformancePublisher publisher;

    @InjectMocks
    private SimulatorService simulatorService;

    @Captor
    private ArgumentCaptor<PerformanceData> performanceDataCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGeneratePerformanceData_runsAndPublishes() {
        assertDoesNotThrow(() -> simulatorService.generatePerformanceData());

        verify(publisher, atLeastOnce()).sendPerformanceData(performanceDataCaptor.capture());
        PerformanceData data = performanceDataCaptor.getValue();

        assertNotNull(data);
        assertNotNull(data.getNodeId());
        assertNotNull(data.getNetworkId());
        assertTrue(data.getLatency() > 0);
        assertTrue(data.getThroughput() > 0);
        assertTrue(data.getErrorRate() >= 0);
        assertNotNull(data.getTimestamp());
    }

    @Test
    void testGenerateHistoricalBatch_runsAndPublishes() {
        assertDoesNotThrow(() -> simulatorService.generateHistoricalBatch(5));

        verify(publisher, atLeastOnce()).sendPerformanceData(performanceDataCaptor.capture());
        for (PerformanceData data : performanceDataCaptor.getAllValues()) {
            assertNotNull(data.getNodeId());
            assertNotNull(data.getNetworkId());
            assertTrue(data.getLatency() > 0);
            assertTrue(data.getThroughput() > 0);
            assertTrue(data.getErrorRate() >= 0);
            assertNotNull(data.getTimestamp());
        }
    }

    @Test
    void testSimulateNetworkScenario_runsAndPublishes() {
        assertDoesNotThrow(() -> simulatorService.simulateNetworkScenario("stress", 101));

        verify(publisher, atLeastOnce()).sendPerformanceData(performanceDataCaptor.capture());
        for (PerformanceData data : performanceDataCaptor.getAllValues()) {
            assertNotNull(data.getNodeId());
            assertNotNull(data.getNetworkId());
            assertTrue(data.getLatency() > 0);
            assertTrue(data.getThroughput() > 0);
            assertTrue(data.getErrorRate() >= 0);
            assertNotNull(data.getTimestamp());
        }
    }

    @Test
    void testGenerateDiverseTestData_runsAndPublishes() {
        assertDoesNotThrow(() -> simulatorService.generateDiverseTestData());

        verify(publisher, atLeastOnce()).sendPerformanceData(performanceDataCaptor.capture());
        for (PerformanceData data : performanceDataCaptor.getAllValues()) {
            assertNotNull(data.getNodeId());
            assertNotNull(data.getNetworkId());
            assertTrue(data.getLatency() > 0);
            assertTrue(data.getThroughput() > 0);
            assertTrue(data.getErrorRate() >= 0);
            assertNotNull(data.getTimestamp());
        }
    }
}
