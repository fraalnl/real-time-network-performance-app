package com.ericsson.service;

import com.ericsson.model.PerformanceData;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;

class NetworkPerformancePublisherTest {

    @Test
    void testSendPerformanceData() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, PerformanceData> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        NetworkPerformancePublisher publisher = new NetworkPerformancePublisher(kafkaTemplate);

        PerformanceData data = new PerformanceData(1, 2, 25.0, 100.0, 0.5, LocalDateTime.now());
        publisher.sendPerformanceData(data);

        Mockito.verify(kafkaTemplate).send("network-performance", data);
    }
}

