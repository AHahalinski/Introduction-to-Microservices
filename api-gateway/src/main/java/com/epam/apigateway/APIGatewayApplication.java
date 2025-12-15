package com.epam.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Application.
 * Provides a single entry point for all microservices and handles routing,
 * load balancing, and cross-cutting concerns like CORS.
 * 
 * Routes:
 * - /api/resources/** -> resource-service
 * - /api/songs/**     -> song-service
 * - /resources/**     -> resource-service (backward compatibility)
 * - /songs/**         -> song-service (backward compatibility)
 */
@SpringBootApplication
@EnableDiscoveryClient
public class APIGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(APIGatewayApplication.class, args);
    }
}

