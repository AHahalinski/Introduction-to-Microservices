package com.epam.microservices.songservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for Song Service.
 * Registers with Eureka Server for service discovery.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SongServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SongServiceApplication.class, args);
    }
}


