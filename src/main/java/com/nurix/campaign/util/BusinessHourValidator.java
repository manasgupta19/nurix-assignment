package com.nurix.campaign.util;

import com.nurix.campaign.entity.Campaign;
import com.nurix.campaign.scheduler.CallDispatcher;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class BusinessHourValidator {

    private static final Logger log = LoggerFactory.getLogger(BusinessHourValidator.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Checks if a given time is within any of the provided business windows.
     * If no windows are defined, the campaign is considered "Always Open".
     */
    public static boolean isWithinHours(LocalDateTime dateTime, List<Campaign.BusinessWindow> windows) {
        // Requirement: If windows are empty, there are no restrictions.
        if (windows == null || windows.isEmpty()) {
            return true;
        }

        java.time.DayOfWeek currentDay = dateTime.getDayOfWeek();
        LocalTime currentTime = dateTime.toLocalTime();

        for (Campaign.BusinessWindow window : windows) {
            // 1. Check if the day matches
            if (window.getDayOfWeek() == currentDay) {
                LocalTime start = LocalTime.parse(window.getStartTime(), TIME_FORMATTER);
                LocalTime end = LocalTime.parse(window.getEndTime(), TIME_FORMATTER);

                // 2. Check if the current time is within [start, end]
                // We use !isBefore and !isAfter to include the boundary minutes
                if (!currentTime.isBefore(start) && !currentTime.isAfter(end)) {
                    return true;
                }
            }
        }

        log.debug("Time {} is outside configured business hours.", dateTime);
        return false;
    }
}