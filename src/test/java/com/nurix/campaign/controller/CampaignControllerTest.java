package com.nurix.campaign.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nurix.campaign.dto.request.CampaignRequest;
import com.nurix.campaign.entity.Campaign;
import com.nurix.campaign.exception.FileProcessingException;
import com.nurix.campaign.exception.GlobalExceptionHandler;
import com.nurix.campaign.service.CampaignService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampaignController.class)
@Import(GlobalExceptionHandler.class)
class CampaignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CampaignService campaignService;

    @Test
    void createCampaign_shouldReturnCreated_whenRequestIsValid() throws Exception {
        // 1. Arrange JSON Metadata
        CampaignRequest request = new CampaignRequest();
        request.setName("New Year Promo");
        request.setMaxConcurrency(5);
        request.setMaxRetries(2);
        request.setRetryDelaySeconds(60);
        request.setTimeZone("UTC");
        request.setBusinessHours(Collections.emptyList());

        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile campaignPart = new MockMultipartFile(
                "campaign", "", "application/json", json.getBytes());

        // 2. Arrange CSV File
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", "numbers.csv", "text/csv", "9876543210\n1234567890".getBytes());

        // 3. Mock Service Behavior
        Campaign mockCampaign = new Campaign();
        mockCampaign.setId(1L);
        mockCampaign.setName("New Year Promo");
        when(campaignService.createCampaign(any(CampaignRequest.class), any())).thenReturn(mockCampaign);

        // 4. Act & Assert
        mockMvc.perform(multipart("/api/campaigns")
                .file(campaignPart)
                .file(csvFile))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Year Promo"));
    }

    @Test
    void createCampaign_shouldReturnBadRequest_whenValidationFails() throws Exception {
        // Arrange an invalid request (e.g., negative concurrency)
        CampaignRequest request = new CampaignRequest();
        request.setMaxConcurrency(-1); 

        MockMultipartFile campaignPart = new MockMultipartFile(
                "campaign", "", "application/json", objectMapper.writeValueAsString(request).getBytes());
        MockMultipartFile csvFile = new MockMultipartFile("file", "test.csv", "text/csv", "123".getBytes());

        mockMvc.perform(multipart("/api/campaigns")
                .file(campaignPart)
                .file(csvFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.maxConcurrency").exists());
    }

    @Test
    void getCallStatus_shouldReturnCallRecord() throws Exception {
        // This tests your new Individual Call Status API
        mockMvc.perform(get("/api/campaigns/calls/1"))
                .andExpect(status().isOk());
    }
}