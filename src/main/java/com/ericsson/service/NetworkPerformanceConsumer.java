package com.ericsson.service;

import com.ericsson.model.PerformanceData;
import com.ericsson.repository.PerformanceDataRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NetworkPerformanceConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NetworkPerformanceConsumer.class);

    private final PerformanceDataRepository repository;

    @Autowired
    public NetworkPerformanceConsumer(PerformanceDataRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "network-performance", groupId = "network-performance-group")
    @Transactional
    public void consumePerformanceData(ConsumerRecord<String, PerformanceData> record) {
        PerformanceData message = record.value();

        try {
            PerformanceData savedData = repository.save(message);
            logger.info("✅ Saved performance data to DB: Node {}, Network {}, Latency: {}ms",
                    savedData.getNodeId(), savedData.getNetworkId(), savedData.getLatency());
        } catch (Exception e) {
            logger.error("❌ Error saving performance data: {}", e.getMessage());
        }
    }
}