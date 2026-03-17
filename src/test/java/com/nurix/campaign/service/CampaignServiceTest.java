package com.nurix.campaign.service;

import com.nurix.campaign.dto.request.CampaignRequest;
import com.nurix.campaign.entity.Campaign;
import com.nurix.campaign.exception.FileProcessingException;
import com.nurix.campaign.repository.CampaignRepository;
import com.nurix.campaign.repository.CallRecordRepository;
import com.nurix.campaign.service.impl.CampaignServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CampaignServiceTest {

    private CampaignService campaignService;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private CallRecordRepository callRecordRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        campaignService = new CampaignServiceImpl(campaignRepository, callRecordRepository);
    }

    @Test
    void createCampaign_shouldProperlyMapRequestAndFile() {
        // Arrange
        CampaignRequest request = new CampaignRequest();
        request.setName("Service Test");
        request.setMaxConcurrency(10);
        request.setTimeZone("IST");
        request.setBusinessHours(Collections.emptyList());

        MockMultipartFile csvFile = new MockMultipartFile(
                "file", "test.csv", "text/csv", "9999988888".getBytes());

        when(campaignRepository.save(any(Campaign.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Campaign result = campaignService.createCampaign(request, csvFile);

        // Assert
        assertNotNull(result);
        assertEquals("Service Test", result.getName());
        assertEquals(1, result.getCalls().size());
        assertEquals("9999988888", result.getCalls().get(0).getPhoneNumber());
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    void shouldDeduplicateNumbersAndIgnoreInvalidData() {
        // Arrange
        String csvContent = "9999999999\n9999999999\n12345\nabcdefghij\n8888888888";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());
        
        CampaignRequest request = new CampaignRequest();
        request.setName("Dedupe Test");

        // ADD THIS STUB:
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(i -> i.getArgument(0));
        
        // Act
        Campaign result = campaignService.createCampaign(request, file);

        // Assert
        assertNotNull(result); // Guard against null
        assertEquals(2, result.getCalls().size());
    }

    @Test
    void shouldThrowExceptionWhenNoValidNumbersFound() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.csv", 
                "text/csv", "invalid-data".getBytes());
        
        assertThrows(FileProcessingException.class, () -> 
                campaignService.createCampaign(new CampaignRequest(), file));
    }
}