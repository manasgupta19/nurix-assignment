package com.nurix.campaign.util;

import com.nurix.campaign.entity.Campaign;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BusinessHourValidatorTest {

    @Test
    void shouldReturnTrueWhenNoBusinessHoursDefined() {
        Campaign campaign = new Campaign();
        campaign.setBusinessHours(null);
        assertTrue(BusinessHourValidator.isWithinBusinessHours(campaign, LocalDateTime.now()));
    }

    @Test
    void shouldReturnFalseWhenOutsideBusinessHours() {
        Campaign campaign = new Campaign();
        campaign.setTimeZone("UTC");
        
        Campaign.BusinessWindow window = new Campaign.BusinessWindow();
        window.setDayOfWeek("MONDAY");
        window.setStartTime("09:00");
        window.setEndTime("17:00");
        campaign.setBusinessHours(List.of(window));

        // Test a Monday at 6:00 PM (18:00) -> Should be False
        LocalDateTime evening = LocalDateTime.of(2026, 3, 16, 18, 0); 
        assertFalse(BusinessHourValidator.isWithinBusinessHours(campaign, evening));
    }

    @Test
    void shouldHandleTimezoneConversionCorrectly() {
        Campaign campaign = new Campaign();
        campaign.setTimeZone("Asia/Kolkata"); // IST is UTC+5:30
        
        Campaign.BusinessWindow window = new Campaign.BusinessWindow();
        window.setDayOfWeek("MONDAY");
        window.setStartTime("10:00");
        window.setEndTime("11:00");
        campaign.setBusinessHours(List.of(window));

        // UTC Time is 05:00 AM Monday -> In Kolkata it is 10:30 AM Monday
        LocalDateTime utcNow = LocalDateTime.of(2026, 3, 16, 5, 0);
        assertTrue(BusinessHourValidator.isWithinBusinessHours(campaign, utcNow));
    }
}