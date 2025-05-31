package com.ericsson.service;

import com.ericsson.model.PerformanceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NetworkPerformancePublisher {

    private final KafkaTemplate<String, PerformanceData> kafkaTemplate;
    private static final String topicName = "network-performance";

    @Autowired
    public NetworkPerformancePublisher(KafkaTemplate<String, PerformanceData> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPerformanceData(PerformanceData message) {
        kafkaTemplate.send(topicName, message);
        System.out.println("Published performance data: " + message);
    }
}


