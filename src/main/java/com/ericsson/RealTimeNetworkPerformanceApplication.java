package com.ericsson;

import com.ericsson.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class RealTimeNetworkPerformanceApplication {
	public static void main(String[] args) {
		SpringApplication.run(RealTimeNetworkPerformanceApplication.class, args);
	}
}
