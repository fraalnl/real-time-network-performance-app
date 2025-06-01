package com.ericsson.service;

import com.ericsson.model.PerformanceData;
import com.ericsson.repository.PerformanceDataRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NetworkPerformanceConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NetworkPerformanceConsumer.class);

    private final PerformanceDataRepository repository;

    @Autowired
    public NetworkPerformanceConsumer(PerformanceDataRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "network-performance", groupId = "network-performance-group")
    public void consumePerformanceData(ConsumerRecord<String, PerformanceData> record) {
        PerformanceData message = record.value();
        logger.info("Consumed performance data: {}", message);

        // Map to entity
        PerformanceData entity = new PerformanceData();
        entity.setNodeId(message.getNodeId());
        entity.setNetworkId(message.getNetworkId());
        entity.setLatency(message.getLatency());
        entity.setThroughput(message.getThroughput());
        entity.setErrorRate(message.getErrorRate());
        entity.setTimestamp(message.getTimestamp());

        // Save to DB
        repository.save(entity);
        logger.info("Saved to DB: {}", entity);
    }
}


