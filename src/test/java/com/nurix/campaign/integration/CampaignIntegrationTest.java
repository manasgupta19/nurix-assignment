package com.nurix.campaign.integration;

import com.nurix.campaign.dto.request.CampaignRequest;
import com.nurix.campaign.repository.CampaignRepository;
import com.nurix.campaign.repository.CallRecordRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class CampaignIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CallRecordRepository callRecordRepository;

    @BeforeEach
    void setUp() {
        // Explicitly clear the database before every test run
        // Delete child records first to avoid foreign key violations
        callRecordRepository.deleteAll();
        campaignRepository.deleteAll();
    }

    @Test
    void shouldCreateCampaignAndCallRecords() {
        CampaignRequest request = new CampaignRequest();
        request.setName("Integration Test Campaign");
        request.setPhoneNumbers(List.of("111", "222"));
        request.setMaxConcurrency(1);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/campaigns", request, String.class);
        
        // Assertions
        assertEquals(201, response.getStatusCode().value());
        
        // Now the count will always be exactly 1
        assertEquals(1, campaignRepository.count(), "Database should contain exactly one campaign");
        
        var savedCampaign = campaignRepository.findAll().get(0);
        assertEquals(2, savedCampaign.getCalls().size(), "Campaign should have exactly two call records");
    }
}