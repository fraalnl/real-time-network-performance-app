package com.ericsson.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PerformanceData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer nodeId;
    private Integer networkId;
    private Double latency;       // in milliseconds
    private Double throughput;    // in Mbps
    private Double errorRate;     // in %

    private LocalDateTime timestamp;

    public PerformanceData() { // no-arg constructor needed
    }

    public PerformanceData(Integer nodeId, Integer networkId, Double latency, Double throughput, Double errorRate, LocalDateTime timestamp) {
        this.nodeId = nodeId;
        this.networkId = networkId;
        this.latency = latency;
        this.throughput = throughput;
        this.errorRate = errorRate;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Integer networkId) {
        this.networkId = networkId;
    }

    public Double getLatency() {
        return latency;
    }

    public void setLatency(Double latency) {
        this.latency = latency;
    }

    public Double getThroughput() {
        return throughput;
    }

    public void setThroughput(Double throughput) {
        this.throughput = throughput;
    }

    public Double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(Double errorRate) {
        this.errorRate = errorRate;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PerformanceData{" +
                "id=" + id +
                ", nodeId=" + nodeId +
                ", networkId=" + networkId +
                ", latency=" + latency +
                ", throughput=" + throughput +
                ", errorRate=" + errorRate +
                ", timestamp=" + timestamp +
                '}';
    }
}
