package com.nurix.campaign.integration;

import com.nurix.campaign.dto.request.CampaignRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CampaignIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCampaign_withValidMultipartData_shouldReturnCreated() throws Exception {
        // 1. Prepare Campaign Metadata
        CampaignRequest request = new CampaignRequest();
        request.setName("Integration Test Campaign");
        request.setMaxConcurrency(5);
        request.setMaxRetries(2);
        request.setRetryDelaySeconds(30);
        request.setTimeZone("UTC");
        request.setBusinessHours(java.util.Collections.emptyList());

        String jsonMetadata = objectMapper.writeValueAsString(request);
        MockMultipartFile campaignPart = new MockMultipartFile(
                "campaign", "", "application/json", jsonMetadata.getBytes());

        // 2. Prepare CSV File
        String csvContent = "9876543210\n8888877777\n1234567890";
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", "numbers.csv", "text/csv", csvContent.getBytes());

        // 3. Execute Multipart Request
        mockMvc.perform(multipart("/api/campaigns")
                .file(campaignPart)
                .file(csvFile))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Test Campaign"))
                .andExpect(jsonPath("$.calls.length()").value(3));
    }

    @Test
    void createCampaign_withInvalidMetadata_shouldReturnBadRequest() throws Exception {
        CampaignRequest invalidRequest = new CampaignRequest();
        invalidRequest.setMaxRetries(10); // Violates @Max(5)

        MockMultipartFile campaignPart = new MockMultipartFile(
                "campaign", "", "application/json", objectMapper.writeValueAsString(invalidRequest).getBytes());
        
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "test.csv", "text/csv", "".getBytes());

        mockMvc.perform(multipart("/api/campaigns")
                .file(campaignPart)
                .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.maxRetries").exists());
    }
}