package com.velouracove.availability;

import com.velouracove.accommodation.AccommodationMapper;
import com.velouracove.accommodation.domain.AccommodationType;
import com.velouracove.accommodation.domain.AccommodationUnit;
import com.velouracove.accommodation.persistence.AccommodationTypeRepository;
import com.velouracove.accommodation.persistence.AccommodationUnitRepository;
import com.velouracove.api.ApiDtos.PriceLine;
import com.velouracove.api.ApiDtos.PriceQuote;
import com.velouracove.common.ApiException;
import com.velouracove.config.persistence.ResortSettingsRepository;
import com.velouracove.reservation.persistence.ReservationRepository;
import com.velouracove.pricing.PricingService;
import com.velouracove.config.domain.ResortSettings;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvailabilityServiceTest {
    @Test
    void remainingInventoryExcludesUnitsWithBlockingOverlaps() {
        AccommodationTypeRepository typeRepository = mock(AccommodationTypeRepository.class);
        AccommodationUnitRepository unitRepository = mock(AccommodationUnitRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        PricingService pricingService = mock(PricingService.class);
        AvailabilityService service = new AvailabilityService(typeRepository, unitRepository, reservationRepository,
                pricingService, new AccommodationMapper(), mock(ResortSettingsRepository.class));

        AccommodationUnit occupied = new AccommodationUnit();
        occupied.setId(UUID.randomUUID());
        AccommodationUnit available = new AccommodationUnit();
        available.setId(UUID.randomUUID());
        when(reservationRepository.findBlockingOverlaps(anySet(), any(), any(), anyCollection(), any(), any()))
                .thenReturn(List.of(reservation(occupied)));

        int remaining = service.remaining(List.of(occupied, available), LocalDate.of(2027, 5, 10),
                LocalDate.of(2027, 5, 12), OffsetDateTime.now());

        assertEquals(1, remaining);
        verify(reservationRepository).findBlockingOverlaps(anySet(), any(), any(), anyCollection(), any(), any());
    }

    @Test
    void emptyEligibleInventoryHasZeroAvailabilityWithoutReservationQuery() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        AvailabilityService service = new AvailabilityService(mock(AccommodationTypeRepository.class),
                mock(AccommodationUnitRepository.class), reservations, mock(PricingService.class), new AccommodationMapper(),
                mock(ResortSettingsRepository.class));

        assertEquals(0, service.remaining(List.of(), LocalDate.of(2027, 5, 10),
                LocalDate.of(2027, 5, 12), OffsetDateTime.now()));
        verifyNoInteractions(reservations);
    }

    @Test
    void searchClassifiesFullPartialAndSoldOutPhysicalInventory() {
        assertEquals("AVAILABLE", searchStatus(2, 0));
        assertEquals("LIMITED", searchStatus(3, 2));
        assertEquals("SOLD_OUT", searchStatus(2, 2));
    }

    @Test
    void searchRejectsOccupancyBeyondAccommodationCapacity() {
        var fixture = fixture(2, 0);
        assertThrows(ApiException.class, () -> fixture.service.search(LocalDate.of(2027, 5, 10),
                LocalDate.of(2027, 5, 12), 3, 2, fixture.type.getId()));
        verifyNoInteractions(fixture.unitRepository);
    }

    private String searchStatus(int unitCount, int blockedCount) {
        var fixture = fixture(unitCount, blockedCount);
        return fixture.service.search(LocalDate.of(2027, 5, 10), LocalDate.of(2027, 5, 12),
                2, 0, fixture.type.getId()).getFirst().status();
    }

    private Fixture fixture(int unitCount, int blockedCount) {
        AccommodationType type = new AccommodationType();
        type.setId(UUID.randomUUID());
        type.setName("Horizon Suite");
        type.setSlug("horizon-suite");
        type.setMaxGuests(4);
        type.setMaxAdults(3);
        type.setMaxChildren(2);
        type.setBaseNightlyRate(new BigDecimal("890.00"));
        type.setCurrency("USD");
        type.setCheckInTime(java.time.LocalTime.of(15, 0));
        type.setCheckOutTime(java.time.LocalTime.of(11, 0));
        AccommodationTypeRepository types = mock(AccommodationTypeRepository.class);
        AccommodationUnitRepository units = mock(AccommodationUnitRepository.class);
        ReservationRepository reservations = mock(ReservationRepository.class);
        PricingService pricing = mock(PricingService.class);
        ResortSettingsRepository settingsRepository = mock(ResortSettingsRepository.class);
        when(types.findByIdInAndActiveTrue(List.of(type.getId()))).thenReturn(List.of(type));
        List<AccommodationUnit> inventory = java.util.stream.IntStream.range(0, unitCount).mapToObj(index -> {
            AccommodationUnit unit = new AccommodationUnit();
            unit.setId(UUID.randomUUID());
            return unit;
        }).toList();
        when(units.findEligible(type.getId(), AccommodationUnit.MaintenanceStatus.READY)).thenReturn(inventory);
        when(reservations.findBlockingOverlaps(anyCollection(), any(), any(), anyCollection(), any(), any()))
                .thenReturn(inventory.stream().limit(blockedCount).map(this::reservation).toList());
        when(pricing.quote(eq(type), any(), any())).thenReturn(new PriceQuote(
                List.of(new PriceLine(LocalDate.of(2027, 5, 10), new BigDecimal("890.00"))),
                new BigDecimal("1780.00"), new BigDecimal("70.00"), new BigDecimal("231.25"),
                new BigDecimal("2081.25"), "USD"));
        ResortSettings settings = new ResortSettings();
        settings.setMaxStayNights(30);
        when(settingsRepository.findById((short) 1)).thenReturn(Optional.of(settings));
        return new Fixture(new AvailabilityService(types, units, reservations, pricing, new AccommodationMapper(), settingsRepository),
                type, units);
    }

    private record Fixture(AvailabilityService service, AccommodationType type,
                           AccommodationUnitRepository unitRepository) {}

    private com.velouracove.reservation.domain.Reservation reservation(AccommodationUnit unit) {
        var reservation = new com.velouracove.reservation.domain.Reservation();
        reservation.setAccommodationUnit(unit);
        return reservation;
    }
}
