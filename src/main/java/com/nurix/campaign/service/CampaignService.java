package com.nurix.campaign.service;

import com.nurix.campaign.dto.request.CampaignRequest;
import com.nurix.campaign.dto.response.CampaignSummaryResponse;
import com.nurix.campaign.entity.CallRecord;
import com.nurix.campaign.entity.Campaign;
import org.springframework.web.multipart.MultipartFile;

public interface CampaignService {
    Campaign createCampaign(CampaignRequest request, MultipartFile file);
    CampaignSummaryResponse getCampaignSummary(Long campaignId);
    CallRecord getCallRecord(Long callId);
}