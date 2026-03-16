package com.nurix.campaign;

import com.nurix.campaign.controller.CampaignController;
import com.nurix.campaign.repository.CampaignRepository;
import com.nurix.campaign.scheduler.CallDispatcher;
import com.nurix.campaign.service.CampaignService;

import io.micrometer.core.instrument.MeterRegistry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CampaignServiceApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CampaignController campaignController;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CallDispatcher callDispatcher;

    @Test
    @DisplayName("Context Load Sanity Check: Verify all core beans are initialized")
    void contextLoads() {
        // Verify Application Context starts
        assertThat(applicationContext).isNotNull();

        // Verify Controllers and Services are correctly wired (Dependency Injection check)
        assertThat(campaignController).isNotNull();
        assertThat(campaignService).isNotNull();
        
        // Verify Data Layer is ready
        assertThat(campaignRepository).isNotNull();

        // Verify the background polling engine is active
        assertThat(callDispatcher).isNotNull();
    }

    @Test
	@DisplayName("Verify Actuator and Metrics beans are present")
	void metricsBeansActive() {
		// Check for the general MeterRegistry interface rather than the specific Prometheus implementation
		MeterRegistry registry = applicationContext.getBean(MeterRegistry.class);
		assertThat(registry).isNotNull();
	}
}