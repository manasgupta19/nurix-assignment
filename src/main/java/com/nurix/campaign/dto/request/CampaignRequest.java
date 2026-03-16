package com.nurix.campaign.dto.request;

import java.util.List;

public class CampaignRequest {
    private String name;
    private int maxConcurrency;
    private int maxRetries;
    private long retryDelaySeconds;
    private String timeZone;
    private List<BusinessWindowDTO> businessHours;
    private List<String> phoneNumbers;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getMaxConcurrency() { return maxConcurrency; }
    public void setMaxConcurrency(int maxConcurrency) { this.maxConcurrency = maxConcurrency; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public long getRetryDelaySeconds() { return retryDelaySeconds; }
    public void setRetryDelaySeconds(long retryDelaySeconds) { this.retryDelaySeconds = retryDelaySeconds; }
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
    public List<BusinessWindowDTO> getBusinessHours() { return businessHours; }
    public void setBusinessHours(List<BusinessWindowDTO> businessHours) { this.businessHours = businessHours; }
    public List<String> getPhoneNumbers() { return phoneNumbers; }
    public void setPhoneNumbers(List<String> phoneNumbers) { this.phoneNumbers = phoneNumbers; }

    public static class BusinessWindowDTO {
        private String dayOfWeek;
        private String startTime;
        private String endTime;

        public String getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
    }
}