package com.nurix.campaign.service.impl;

import com.nurix.campaign.dto.request.CampaignRequest;
import com.nurix.campaign.dto.response.CampaignSummaryResponse;
import com.nurix.campaign.entity.CallRecord;
import com.nurix.campaign.entity.Campaign;
import com.nurix.campaign.entity.enums.CallStatus;
import com.nurix.campaign.exception.ResourceNotFoundException;
import com.nurix.campaign.repository.CampaignRepository;
import com.nurix.campaign.repository.CallRecordRepository;
import com.nurix.campaign.service.CampaignService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CampaignServiceImpl implements CampaignService {
    
    private static final Logger log = LoggerFactory.getLogger(CampaignServiceImpl.class);

    @Autowired
    private final CampaignRepository campaignRepository;

    @Autowired
    private final CallRecordRepository callRecordRepository;

    @Autowired
    public CampaignServiceImpl(CampaignRepository campaignRepository, CallRecordRepository callRecordRepository) {
        this.campaignRepository = campaignRepository;
        this.callRecordRepository = callRecordRepository;
    }

    @Override
    public CallRecord getCallRecord(Long callId) {
        return callRecordRepository.findById(callId)
                .orElseThrow(() -> new ResourceNotFoundException("Call record not found"));
    }

    @Override
    @Transactional
    public Campaign createCampaign(CampaignRequest request, MultipartFile file) {
        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setMaxConcurrency(request.getMaxConcurrency());
        campaign.setMaxRetries(request.getMaxRetries());
        campaign.setRetryDelaySeconds(request.getRetryDelaySeconds());
        campaign.setTimeZone(request.getTimeZone());

        // 1. Map Business Windows
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

        // 2. Collect Phone Numbers (Set ensures uniqueness)
        Set<String> allPhoneNumbers = new HashSet<>();

        // Add numbers from CSV file if provided
        if (file != null && !file.isEmpty()) {
            allPhoneNumbers.addAll(parseCsvNumbers(file));
        }

        // 3. Map unique numbers to CallRecords
        if (!allPhoneNumbers.isEmpty()) {
            campaign.setCalls(allPhoneNumbers.stream()
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

    // Helper method for CSV parsing
    private List<String> parseCsvNumbers(MultipartFile file) {
        // Regex for exactly 10 digits
        String phoneRegex = "^[0-9]{10}$";
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            return reader.lines()
                    .filter(line -> line != null && !line.trim().isEmpty())
                    .map(String::trim)
                    .peek(phone -> {
                        if (!phone.matches(phoneRegex)) {
                            throw new IllegalArgumentException("Invalid phone number format: " + phone + ". Must be 10 digits.");
                        }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file", e);
        }
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