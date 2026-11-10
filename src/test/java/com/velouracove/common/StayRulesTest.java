package com.velouracove.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class StayRulesTest {
    private final LocalDate checkIn = LocalDate.of(2027, 6, 10);
    private final LocalDate checkOut = LocalDate.of(2027, 6, 13);

    @Test
    void overlapUsesExclusiveCheckoutBoundary() {
        assertTrue(StayRules.overlaps(checkIn, checkOut, LocalDate.of(2027, 6, 12), LocalDate.of(2027, 6, 15)));
        assertFalse(StayRules.overlaps(checkIn, checkOut, checkOut, LocalDate.of(2027, 6, 16)));
        assertFalse(StayRules.overlaps(checkIn, checkOut, LocalDate.of(2027, 6, 7), checkIn));
        assertTrue(StayRules.overlaps(checkIn, checkOut, LocalDate.of(2027, 6, 11), LocalDate.of(2027, 6, 12)));
        assertTrue(StayRules.overlaps(checkIn, checkOut, LocalDate.of(2027, 6, 8), LocalDate.of(2027, 6, 15)));
        assertTrue(StayRules.overlaps(checkIn, checkOut, checkIn, checkOut));
    }

    @Test
    void stayRulesRejectInvalidDatesOccupancyAndLongStays() {
        ApiException invalidDates = assertThrows(ApiException.class,
                () -> StayRules.validate(checkOut, checkIn, 2, 0, 30));
        assertEquals("INVALID_STAY_DATES", invalidDates.getCode());

        ApiException invalidOccupancy = assertThrows(ApiException.class,
                () -> StayRules.validate(checkIn, checkOut, 0, 0, 30));
        assertEquals("INVALID_OCCUPANCY", invalidOccupancy.getCode());

        ApiException tooLong = assertThrows(ApiException.class,
                () -> StayRules.validate(checkIn, checkIn.plusDays(31), 2, 0, 30));
        assertEquals("MAXIMUM_STAY_EXCEEDED", tooLong.getCode());
    }
}
