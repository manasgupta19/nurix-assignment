package com.nurix.campaign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Enables the background polling engine we will build in Phase 3
public class CampaignApplication {
	public static void main(String[] args) {
		SpringApplication.run(CampaignApplication.class, args);
	}
}