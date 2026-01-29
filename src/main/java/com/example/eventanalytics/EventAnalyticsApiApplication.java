package com.example.eventanalytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.eventanalytics.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class EventAnalyticsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventAnalyticsApiApplication.class, args);
	}

}
