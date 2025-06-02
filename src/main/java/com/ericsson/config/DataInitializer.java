package com.ericsson.config;

import com.ericsson.model.PerformanceData;
import com.ericsson.repository.PerformanceDataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PerformanceDataRepository repository;

    public DataInitializer(PerformanceDataRepository repository) {
        this.repository = repository;
    }


    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            System.out.println("⚠️ Seed data already present. Skipping.");
            return;
        }

        // Map each nodeId to a networkId (ensuring many-to-one relationship)
        Map<Integer, Integer> nodeToNetwork = new HashMap<>();

        // Example: 20 nodes, 4 networks
        int[] networkIds = {201, 202, 203, 204};

        for (int i = 0; i < 20; i++) {
            int nodeId = 100 + i;
            int networkId = networkIds[i / 5]; // 5 nodes per network
            nodeToNetwork.put(nodeId, networkId);
        }

        // Seed 20 metrics with node/network relationship preserved
        for (int i = 0; i < 20; i++) {
            int nodeIndex = i % 20;
            int nodeId = 100 + nodeIndex;
            int networkId = nodeToNetwork.get(nodeId);

            PerformanceData data = new PerformanceData();
            data.setNodeId(nodeId);
            data.setNetworkId(networkId);
            data.setLatency(15.0 + (i % 10));
            data.setThroughput(95.0 + (i % 15));
            BigDecimal errorRate = BigDecimal.valueOf(0.2 * (i % 5))
                    .setScale(3, RoundingMode.HALF_UP);
            data.setErrorRate(errorRate.doubleValue());
            data.setTimestamp(LocalDateTime.now().minusMinutes(i * 3));

            repository.save(data);
        }

        System.out.println("✅ Seeded 20 records with 20 nodes mapped to 4 networks");
    }
}
