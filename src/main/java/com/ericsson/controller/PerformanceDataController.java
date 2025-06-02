package com.ericsson.controller;

import com.ericsson.model.PerformanceData;
import com.ericsson.service.NetworkPerformancePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/performance")
public class PerformanceDataController {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceDataController.class);

    private final NetworkPerformancePublisher publisher;

    @Autowired
    public PerformanceDataController(NetworkPerformancePublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/publish")
    public String publishPerformanceData(@RequestBody PerformanceData data) {
        logger.info("Received request to publish performance data: {}", data);
        publisher.sendPerformanceData(data);
        return "Performance data published successfully!";
    }
}

