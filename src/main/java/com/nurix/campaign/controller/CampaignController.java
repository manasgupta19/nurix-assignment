package com.nurix.campaign.controller;

import com.nurix.campaign.dto.request.CampaignRequest;
import com.nurix.campaign.dto.response.CampaignSummaryResponse;
import com.nurix.campaign.entity.CallRecord;
import com.nurix.campaign.entity.Campaign;
import com.nurix.campaign.service.CampaignService;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    @Autowired
    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Campaign> createCampaign(
            @RequestPart("campaign") @Valid CampaignRequest request, // Campaign metadata as JSON
            @RequestPart(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) { // Optional CSV file
        
        Campaign campaign = campaignService.createCampaign(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(campaign);
    }

    @GetMapping("/calls/{callId}")
    public ResponseEntity<CallRecord> getCallStatus(@PathVariable Long callId) {
        return ResponseEntity.ok(campaignService.getCallRecord(callId));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<CampaignSummaryResponse> getSummary(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignSummary(id));
    }
}