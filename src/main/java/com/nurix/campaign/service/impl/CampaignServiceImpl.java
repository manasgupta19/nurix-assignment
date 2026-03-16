package com.nurix.campaign.service.impl;

import com.nurix.campaign.dto.request.CampaignRequest;
import com.nurix.campaign.dto.response.CampaignSummaryResponse;
import com.nurix.campaign.entity.CallRecord;
import com.nurix.campaign.entity.Campaign;
import com.nurix.campaign.entity.enums.CallStatus;
import com.nurix.campaign.repository.CampaignRepository;
import com.nurix.campaign.service.CampaignService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CampaignServiceImpl implements CampaignService {

    @Autowired
    private final CampaignRepository campaignRepository;

    @Autowired
    public CampaignServiceImpl(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @Override
    @Transactional
    public Campaign createCampaign(CampaignRequest request) {
        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setMaxConcurrency(request.getMaxConcurrency());
        campaign.setMaxRetries(request.getMaxRetries());
        campaign.setRetryDelaySeconds(request.getRetryDelaySeconds());
        campaign.setTimeZone(request.getTimeZone());

        // Map Business Windows
        if (request.getBusinessHours() != null) {
            campaign.setBusinessHours(request.getBusinessHours().stream()
                .map(dto -> {
                    Campaign.BusinessWindow window = new Campaign.BusinessWindow();
                    window.setDayOfWeek(dto.getDayOfWeek());
                    window.setStartTime(dto.getStartTime());
                    window.setEndTime(dto.getEndTime());
                    return window;
                }).collect(Collectors.toList()));
        }

        // Map Phone Numbers to CallRecords
        if (request.getPhoneNumbers() != null) {
            campaign.setCalls(request.getPhoneNumbers().stream()
                .map(phone -> {
                    CallRecord record = new CallRecord();
                    record.setPhoneNumber(phone);
                    record.setCampaign(campaign);
                    record.setStatus(CallStatus.PENDING);
                    return record;
                }).collect(Collectors.toList()));
        }

        return campaignRepository.save(campaign);
    }

    @Override
    public CampaignSummaryResponse getCampaignSummary(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        List<CallRecord> calls = campaign.getCalls();
        
        Map<String, Long> stats = calls.stream()
                .collect(Collectors.groupingBy(c -> c.getStatus().name(), Collectors.counting()));

        long completed = stats.getOrDefault("COMPLETED", 0L);
        long total = calls.size();

        CampaignSummaryResponse summary = new CampaignSummaryResponse();
        summary.setCampaignId(campaign.getId());
        summary.setCampaignName(campaign.getName());
        summary.setStatus(campaign.getStatus().name());
        summary.setTotalCalls(total);
        summary.setStatsByStatus(stats);
        summary.setSuccessRate(total == 0 ? 0 : (double) completed / total * 100);

        return summary;
    }
}