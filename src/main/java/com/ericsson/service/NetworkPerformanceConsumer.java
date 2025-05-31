package com.ericsson.service;

import com.ericsson.model.PerformanceData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NetworkPerformanceConsumer {

    @KafkaListener(topics = "network-performance", groupId = "network-performance-group")
    public void consumePerformanceData(ConsumerRecord<String, PerformanceData> record) {
        PerformanceData message = record.value();
        System.out.println("Consumed performance data: " + message);

        // TODO: Insert logic to store to database
        // Example:
        // databaseService.save(message);
    }
}

