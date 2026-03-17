package com.nurix.campaign.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.nurix.campaign.entity.Campaign;

class BusinessHourValidatorTest {

    @Test
    void isWithinHours_shouldAllowValidTimeOnCorrectDay() {
        // Arrange
        Campaign.BusinessWindow window = new Campaign.BusinessWindow();
        window.setDayOfWeek(DayOfWeek.TUESDAY);
        window.setStartTime("09:00");
        window.setEndTime("17:00");

        // Today is Tuesday, March 17, 2026
        LocalDateTime validTime = LocalDateTime.of(2026, 3, 17, 10, 0);

        // Act
        boolean result = BusinessHourValidator.isWithinHours(validTime, List.of(window));

        // Assert
        assertTrue(result, "Should be within business hours on a Tuesday morning");
    }

    @Test
    void isWithinHours_shouldDenyTimeOnWrongDay() {
        // Arrange
        Campaign.BusinessWindow window = new Campaign.BusinessWindow();
        window.setDayOfWeek(DayOfWeek.SUNDAY);
        window.setStartTime("09:00");
        window.setEndTime("17:00");

        LocalDateTime tuesdayTime = LocalDateTime.of(2026, 3, 17, 10, 0);

        // Act
        boolean result = BusinessHourValidator.isWithinHours(tuesdayTime, List.of(window));

        // Assert
        assertFalse(result, "Tuesday should not be allowed if only Sunday is configured");
    }

    @Test
    void isWithinHours_shouldReturnTrueWhenNoWindowsDefined() {
        // Requirement: If windows are empty, there are no restrictions (Always Open)
        LocalDateTime anyTime = LocalDateTime.of(2026, 3, 17, 2, 0); // 2 AM

        assertTrue(BusinessHourValidator.isWithinHours(anyTime, Collections.emptyList()), 
                "Should return true for empty window list");
        assertTrue(BusinessHourValidator.isWithinHours(anyTime, null), 
                "Should return true for null window list");
    }

    @Test
    void isWithinHours_shouldAllowBoundaryTimes() {
        // Arrange: 09:00 to 17:00
        Campaign.BusinessWindow window = new Campaign.BusinessWindow();
        window.setDayOfWeek(DayOfWeek.TUESDAY);
        window.setStartTime("09:00");
        window.setEndTime("17:00");

        // Logic uses !isBefore and !isAfter to include boundary minutes
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 17, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 17, 17, 0);

        // Act & Assert
        assertTrue(BusinessHourValidator.isWithinHours(startTime, List.of(window)), 
                "Should be inclusive of start time");
        assertTrue(BusinessHourValidator.isWithinHours(endTime, List.of(window)), 
                "Should be inclusive of end time");
    }

    @Test
    void isWithinHours_shouldAllowWhenOneOfMultipleWindowsMatches() {
        // Arrange: Two windows, one for morning and one for evening
        Campaign.BusinessWindow morning = new Campaign.BusinessWindow();
        morning.setDayOfWeek(DayOfWeek.TUESDAY);
        morning.setStartTime("08:00");
        morning.setEndTime("10:00");

        Campaign.BusinessWindow evening = new Campaign.BusinessWindow();
        evening.setDayOfWeek(DayOfWeek.TUESDAY);
        evening.setStartTime("18:00");
        evening.setEndTime("20:00");

        LocalDateTime morningTime = LocalDateTime.of(2026, 3, 17, 9, 0);
        LocalDateTime afternoonTime = LocalDateTime.of(2026, 3, 17, 14, 0);

        // Act & Assert
        assertTrue(BusinessHourValidator.isWithinHours(morningTime, List.of(morning, evening)), 
                "Should allow if any window matches");
        assertFalse(BusinessHourValidator.isWithinHours(afternoonTime, List.of(morning, evening)), 
                "Should deny if no windows match");
    }
}