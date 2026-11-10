package com.velouracove.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velouracove.accommodation.domain.AccommodationType;
import com.velouracove.accommodation.domain.AccommodationUnit;
import com.velouracove.accommodation.persistence.AccommodationTypeRepository;
import com.velouracove.accommodation.persistence.AccommodationUnitRepository;
import com.velouracove.api.ApiDtos.PaymentResult;
import com.velouracove.api.ApiDtos.PaymentSimulationRequest;
import com.velouracove.api.ApiDtos.ReservationRequest;
import com.velouracove.config.persistence.ResortSettingsRepository;
import com.velouracove.guest.domain.Guest;
import com.velouracove.guest.persistence.GuestRepository;
import com.velouracove.pricing.PricingService;
import com.velouracove.reservation.domain.Reservation;
import com.velouracove.reservation.domain.Reservation.ReservationStatus;
import com.velouracove.reservation.persistence.ReservationRepository;
import com.velouracove.accommodation.AccommodationMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {
    @Test
    void reservationCreationRetryDoesNotCreateAnotherReservation() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        Reservation existing = heldReservation();
        existing.setReservationCreationKey("retry-key-123");
        existing.setPriceBreakdown("{\"nightlyCharges\":[],\"roomSubtotal\":0,\"resortFee\":0,\"taxes\":0,\"total\":0,\"currency\":\"USD\"}");
        when(reservations.findByReservationCreationKey("retry-key-123")).thenReturn(Optional.of(existing));
        ReservationService service = service(reservations);

        var result = service.createReservation(existing.getId(), "retry-key-123", new ReservationRequest(existing.getId(),
                "Ari", "Guest", "ari@example.test", "555-0100", "US", null, null, null, null));

        assertEquals(existing.getId(), result.reservationId());
        assertNull(result.managementToken());
        verify(reservations, never()).save(any());
        verify(reservations, never()).saveAndFlush(any());
    }

    @Test
    void successfulPaymentRetryReturnsExistingConfirmationWithoutAnotherSave() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        Reservation reservation = heldReservation();
        reservation.setGuest(new Guest());
        reservation.setExpiresAt(OffsetDateTime.now().plusMinutes(10));
        when(reservations.findByIdForUpdate(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservations.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ReservationService service = service(reservations);
        PaymentSimulationRequest payment = new PaymentSimulationRequest(PaymentResult.SUCCESS);

        var first = service.simulatePayment(reservation.getId(), "payment-key-123", payment);
        var retry = service.simulatePayment(reservation.getId(), "payment-key-123", payment);

        assertEquals(ReservationStatus.CONFIRMED.name(), first.status());
        assertTrue(first.confirmationNumber().matches("VC-[A-HJ-NP-Z2-9]{6}"));
        assertEquals(first.confirmationNumber(), retry.confirmationNumber());
        assertEquals(first.paymentReference(), retry.paymentReference());
        verify(reservations, times(1)).saveAndFlush(reservation);
    }

    private ReservationService service(ReservationRepository reservations) {
        return new ReservationService(mock(AccommodationTypeRepository.class), mock(AccommodationUnitRepository.class),
                reservations, mock(GuestRepository.class), mock(ResortSettingsRepository.class),
                mock(PricingService.class), new AccommodationMapper(), new ObjectMapper());
    }

    private Reservation heldReservation() {
        AccommodationType type = new AccommodationType();
        type.setName("Horizon Suite");
        AccommodationUnit unit = new AccommodationUnit();
        unit.setAccommodationType(type);
        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setAccommodationUnit(unit);
        reservation.setStatus(ReservationStatus.HELD);
        reservation.setCheckIn(LocalDate.of(2027, 5, 10));
        reservation.setCheckOut(LocalDate.of(2027, 5, 12));
        reservation.setAdults(2);
        reservation.setChildren(0);
        return reservation;
    }
}
