package com.nurix.campaign.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nurix Campaign Management API")
                        .version("1.0.0")
                        .description("Distributed Scheduler for Automated Call Campaigns with Timezone-Awareness and Resilience.")
                        .contact(new Contact()
                                .name("Engineering Team")
                                .email("dev-support@nurix.com")));
    }
}