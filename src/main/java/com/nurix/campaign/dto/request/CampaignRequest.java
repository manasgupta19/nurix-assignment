package com.nurix.campaign.dto.request;

import java.time.DayOfWeek;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CampaignRequest {

    @NotBlank(message = "Campaign name is required")
    @Size(max = 100, message = "Campaign name cannot exceed 100 characters")
    private String name;

    @Min(value = 1, message = "Max concurrency must be at least 1")
    @Max(value = 50, message = "Max concurrency cannot exceed 50 for safety")
    private int maxConcurrency;

    @Min(value = 0, message = "Max retries cannot be negative")
    @Max(value = 5, message = "Max retries cannot exceed 5")
    private int maxRetries;

    @Min(value = 10, message = "Retry delay must be at least 10 seconds")
    private long retryDelaySeconds;

    @NotBlank(message = "TimeZone is required")
    private String timeZone;

    private List<BusinessHourDTO> businessHours;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    public void setMaxConcurrency(int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getRetryDelaySeconds() {
        return retryDelaySeconds;
    }

    public void setRetryDelaySeconds(long retryDelaySeconds) {
        this.retryDelaySeconds = retryDelaySeconds;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public List<BusinessHourDTO> getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(List<BusinessHourDTO> businessHours) {
        this.businessHours = businessHours;
    }

    /**
     * Note: Phone numbers list removed from JSON as they are 
     * now exclusively loaded via CSV Multipart file.
     */

    @Data
    public static class BusinessHourDTO {
        @NotNull(message = "Day of week is required")
        private java.time.DayOfWeek dayOfWeek;

        @NotBlank(message = "Start time is required")
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Start time must be in HH:mm format")
        private String startTime;

        @NotBlank(message = "End time is required")
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "End time must be in HH:mm format")
        private String endTime;

        public DayOfWeek getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDayOfWeek(DayOfWeek dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }
}