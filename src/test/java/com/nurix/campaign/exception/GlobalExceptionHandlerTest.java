package com.nurix.campaign.exception;

import com.nurix.campaign.controller.CampaignController;
import com.nurix.campaign.service.CampaignService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampaignController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CampaignService campaignService;

    @Test
    void shouldReturnStructuredErrorForFileProcessingException() throws Exception {
        when(campaignService.createCampaign(any(), any()))
                .thenThrow(new FileProcessingException("Invalid CSV structure"));

        MockMultipartFile file = new MockMultipartFile("file", "test.csv", 
                MediaType.TEXT_PLAIN_VALUE, "data".getBytes());

        mockMvc.perform(multipart("/api/campaigns")
                        .file(file)
                        .param("name", "Test Campaign"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid CSV structure"))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}