package com.velouracove.common;

import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class StayRules {
    private StayRules() {}

    public static void validate(LocalDate checkIn, LocalDate checkOut, int adults, int children, int maxStayNights) {
        if (checkIn == null || checkOut == null || !checkIn.isBefore(checkOut) || checkIn.isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STAY_DATES", "Check-in must be today or later and before check-out.");
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights > maxStayNights) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MAXIMUM_STAY_EXCEEDED", "The requested stay exceeds the maximum length.");
        }
        if (adults < 1 || children < 0 || adults > 12 || children > 8) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OCCUPANCY", "Guest counts are outside the allowed range.");
        }
    }

    public static boolean overlaps(LocalDate existingCheckIn, LocalDate existingCheckOut,
                                   LocalDate requestedCheckIn, LocalDate requestedCheckOut) {
        return existingCheckIn.isBefore(requestedCheckOut) && existingCheckOut.isAfter(requestedCheckIn);
    }
}
