package com.nurix.campaign.service;

import com.nurix.campaign.dto.request.CampaignRequest;
import com.nurix.campaign.dto.response.CampaignSummaryResponse;
import com.nurix.campaign.entity.Campaign;

public interface CampaignService {
    Campaign createCampaign(CampaignRequest request);
    CampaignSummaryResponse getCampaignSummary(Long campaignId);
}