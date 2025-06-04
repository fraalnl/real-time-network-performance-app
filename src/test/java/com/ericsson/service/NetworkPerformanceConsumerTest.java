package com.ericsson.service;

import com.ericsson.model.PerformanceData;
import com.ericsson.repository.PerformanceDataRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

class NetworkPerformanceConsumerTest {

    @Test
    void testConsumePerformanceData() {
        PerformanceDataRepository repository = Mockito.mock(PerformanceDataRepository.class);
        NetworkPerformanceConsumer consumer = new NetworkPerformanceConsumer(repository);

        PerformanceData data = new PerformanceData(1, 2, 20.0, 100.0, 0.5, LocalDateTime.now());
        // performanceRecord: Rename this variable to not match a restricted identifier.
        ConsumerRecord<String, PerformanceData> performanceRecord = new ConsumerRecord<>("network-performance", 0, 0, null, data);

        PerformanceData expectedEntity = new PerformanceData();
        expectedEntity.setNodeId(data.getNodeId());
        expectedEntity.setNetworkId(data.getNetworkId());
        expectedEntity.setLatency(data.getLatency());
        expectedEntity.setThroughput(data.getThroughput());
        expectedEntity.setErrorRate(data.getErrorRate());
        expectedEntity.setTimestamp(data.getTimestamp());

        // Fix: Stub repository to return the saved entity
        Mockito.when(repository.save(Mockito.any())).thenReturn(expectedEntity);

        consumer.consumePerformanceData(performanceRecord);

        // Validate interaction
        Mockito.verify(repository).save(Mockito.argThat(entity ->
                entity.getNodeId() == 1 &&
                        entity.getNetworkId() == 2 &&
                        entity.getLatency() == 20.0 &&
                        entity.getThroughput() == 100.0 &&
                        entity.getErrorRate() == 0.5
        ));
    }
}


