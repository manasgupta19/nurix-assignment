package com.nurix.campaign.service.impl;

import com.nurix.campaign.dto.request.CampaignRequest;
import com.nurix.campaign.dto.response.CampaignSummaryResponse;
import com.nurix.campaign.entity.CallRecord;
import com.nurix.campaign.entity.Campaign;
import com.nurix.campaign.entity.enums.CallStatus;
import com.nurix.campaign.exception.FileProcessingException;
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
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CampaignServiceImpl implements CampaignService {
    private static final Logger log = LoggerFactory.getLogger(CampaignServiceImpl.class);
    
    private final CampaignRepository campaignRepository;

    private final CallRecordRepository callRecordRepository;

    // Optimized: Pre-compiled regex pattern to avoid recompilation per line/request
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\d{10}$");

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
        Set<String> uniqueNumbers = parseCsvNumbers(file);

        if (uniqueNumbers.isEmpty()) {
            throw new FileProcessingException("The uploaded CSV contains no valid 10-digit phone numbers.");
        }

        // Convert numbers to CallRecord entities
        for (String number : uniqueNumbers) {
            CallRecord record = new CallRecord();
            record.setPhoneNumber(number);
            record.setStatus(CallStatus.PENDING);
            record.setCampaign(campaign);
            campaign.getCalls().add(record);
        }

        log.info("Creating campaign '{}' with {} unique numbers.", campaign.getName(), uniqueNumbers.size());
        return campaignRepository.save(campaign);
    }

    // Helper method for CSV parsing
    private Set<String> parseCsvNumbers(MultipartFile file) {
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> PHONE_NUMBER_PATTERN.matcher(line).matches())
                .collect(Collectors.toSet());
        } catch (IOException e) {
            log.error("Failed to read CSV file: {}", file.getOriginalFilename(), e);
            throw new FileProcessingException("Could not process the uploaded CSV file. Please ensure it is a valid text/csv format.", e);
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