package com.nurix.campaign.dto.response;

import lombok.Data;
import java.util.Map;

@Data
public class CampaignSummaryResponse {
    private Long campaignId;
    private String campaignName;
    private String status;
    private long totalCalls;
    private Map<String, Long> statsByStatus;
    private double successRate;

    // --- Explicit Getters for Jackson Serialization ---
    public Long getCampaignId() { return campaignId; }
    public String getCampaignName() { return campaignName; }
    public String getStatus() { return status; }
    public long getTotalCalls() { return totalCalls; }
    public Map<String, Long> getStatsByStatus() { return statsByStatus; }
    public double getSuccessRate() { return successRate; }

    // Manual Getters/Setters (Staff-level fail-safe)
    public void setCampaignId(Long id) { this.campaignId = id; }
    public void setCampaignName(String name) { this.campaignName = name; }
    public void setStatus(String status) { this.status = status; }
    public void setTotalCalls(long total) { this.totalCalls = total; }
    public void setStatsByStatus(Map<String, Long> stats) { this.statsByStatus = stats; }
    public void setSuccessRate(double rate) { this.successRate = rate; }
}