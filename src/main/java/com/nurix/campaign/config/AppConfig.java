package com.nurix.campaign.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import java.time.Clock;

@Configuration
public class AppConfig {

    /**
     * Using a Clock bean allows us to mock time in our unit tests.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Required for Phase 5's simulated/actual telephony API calls.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Hibernate6Module hibernate6Module() {
        return new Hibernate6Module();
    }
}