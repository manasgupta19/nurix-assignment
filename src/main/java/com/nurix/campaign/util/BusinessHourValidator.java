package com.nurix.campaign.util;

import com.nurix.campaign.entity.Campaign;
import java.time.*;

public class BusinessHourValidator {

    public static boolean isWithinBusinessHours(Campaign campaign, LocalDateTime now) {
        // If no restrictions, it's always allowed
        if (campaign.getBusinessHours() == null || campaign.getBusinessHours().isEmpty()) {
            return true;
        }

        // Clean Fix: Default to UTC if the campaign timezone is missing or invalid
        String campaignTz = (campaign.getTimeZone() == null) ? "UTC" : campaign.getTimeZone();
        
        try {
            ZoneId campaignZone = ZoneId.of(campaignTz);
            
            // Normalize: Convert 'now' (UTC from DB) to the Campaign's local time
            ZonedDateTime campaignTime = now.atZone(ZoneOffset.UTC)
                                            .withZoneSameInstant(campaignZone);
            
            String currentDay = campaignTime.getDayOfWeek().name();
            LocalTime currentTime = campaignTime.toLocalTime();

            return campaign.getBusinessHours().stream()
                    .filter(w -> w.getDayOfWeek().equalsIgnoreCase(currentDay))
                    .anyMatch(w -> {
                        LocalTime start = LocalTime.parse(w.getStartTime());
                        LocalTime end = LocalTime.parse(w.getEndTime());
                        return !currentTime.isBefore(start) && !currentTime.isAfter(end);
                    });
        } catch (DateTimeException e) {
            // Log error and fallback: safer to not call if timezone is corrupted
            return false;
        }
    }
}