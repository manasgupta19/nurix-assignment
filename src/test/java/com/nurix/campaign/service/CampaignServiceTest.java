package com.nurix.campaign.service;

import com.nurix.campaign.dto.request.CampaignRequest;
import com.nurix.campaign.entity.Campaign;
import com.nurix.campaign.repository.CampaignRepository;
import com.nurix.campaign.service.impl.CampaignServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @Test
    void shouldCreateCampaignWithChildRecords() {
        CampaignRequest request = new CampaignRequest();
        request.setName("Test Campaign");
        request.setPhoneNumbers(List.of("123", "456"));

        when(campaignRepository.save(any(Campaign.class))).thenAnswer(i -> i.getArguments()[0]);

        Campaign saved = campaignService.createCampaign(request);

        assertNotNull(saved);
        assertEquals(2, saved.getCalls().size());
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }
}