package com.velouracove.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI velouraOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Veloura Cove API")
                .version("v1")
                .description("Portfolio demonstration for a fictional resort. Reservation and payment data are simulated; no real bookings or payments are processed."));
    }
}
