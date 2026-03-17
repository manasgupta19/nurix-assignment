package com.nurix.campaign.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
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

        // Today is Tuesday, March 17, 2026 (per system context)
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
}