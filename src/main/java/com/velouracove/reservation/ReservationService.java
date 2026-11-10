package com.velouracove.reservation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velouracove.accommodation.domain.AccommodationType;
import com.velouracove.accommodation.domain.AccommodationUnit;
import com.velouracove.accommodation.domain.AccommodationUnit.MaintenanceStatus;
import com.velouracove.accommodation.persistence.AccommodationTypeRepository;
import com.velouracove.accommodation.persistence.AccommodationUnitRepository;
import com.velouracove.accommodation.AccommodationMapper;
import com.velouracove.api.ApiDtos.*;
import com.velouracove.common.ApiException;
import com.velouracove.common.StayRules;
import com.velouracove.config.domain.ResortSettings;
import com.velouracove.config.persistence.ResortSettingsRepository;
import com.velouracove.guest.domain.Guest;
import com.velouracove.guest.persistence.GuestRepository;
import com.velouracove.pricing.PricingService;
import com.velouracove.reservation.domain.Reservation;
import com.velouracove.reservation.domain.Reservation.ReservationStatus;
import com.velouracove.reservation.persistence.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class ReservationService {
    private static final List<ReservationStatus> BLOCKING_STATUSES = List.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN);
    private static final char[] CONFIRMATION_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private final AccommodationTypeRepository typeRepository;
    private final AccommodationUnitRepository unitRepository;
    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final ResortSettingsRepository settingsRepository;
    private final PricingService pricingService;
    private final AccommodationMapper accommodationMapper;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public ReservationService(AccommodationTypeRepository typeRepository, AccommodationUnitRepository unitRepository,
                              ReservationRepository reservationRepository, GuestRepository guestRepository,
                              ResortSettingsRepository settingsRepository, PricingService pricingService,
                              AccommodationMapper accommodationMapper, ObjectMapper objectMapper) {
        this.typeRepository = typeRepository;
        this.unitRepository = unitRepository;
        this.reservationRepository = reservationRepository;
        this.guestRepository = guestRepository;
        this.settingsRepository = settingsRepository;
        this.pricingService = pricingService;
        this.accommodationMapper = accommodationMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public HoldDto createHold(HoldRequest request) {
        StayRules.validate(request.checkIn(), request.checkOut(), request.adults(), request.children(), settings().getMaxStayNights());
        AccommodationType type = typeRepository.findByIdInAndActiveTrue(List.of(request.accommodationTypeId())).stream().findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "The requested accommodation was not found."));
        if (request.adults() > type.getMaxAdults() || request.children() > type.getMaxChildren()
                || request.adults() + request.children() > type.getMaxGuests()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OCCUPANCY", "Guest count exceeds the selected accommodation's capacity.");
        }

        List<AccommodationUnit> eligibleUnits = unitRepository.lockEligible(type.getId(), MaintenanceStatus.READY);
        if (eligibleUnits.isEmpty()) {
            throw unavailable();
        }
        OffsetDateTime now = OffsetDateTime.now();
        Set<UUID> unitIds = eligibleUnits.stream().map(AccommodationUnit::getId).collect(java.util.stream.Collectors.toSet());
        List<Reservation> blocking = reservationRepository.findBlockingOverlaps(unitIds, request.checkIn(), request.checkOut(),
                BLOCKING_STATUSES, ReservationStatus.HELD, now);
        Set<UUID> occupied = blocking.stream().map(r -> r.getAccommodationUnit().getId()).collect(java.util.stream.Collectors.toSet());
        AccommodationUnit selected = eligibleUnits.stream().filter(unit -> !occupied.contains(unit.getId())).findFirst()
                .orElseThrow(this::unavailable);

        ResortSettings settings = settings();
        PriceQuote price = pricingService.quote(type, request.checkIn(), request.checkOut());
        Reservation reservation = new Reservation();
        reservation.setAccommodationUnit(selected);
        reservation.setCheckIn(request.checkIn());
        reservation.setCheckOut(request.checkOut());
        reservation.setAdults(request.adults());
        reservation.setChildren(request.children());
        reservation.setStatus(ReservationStatus.HELD);
        reservation.setExpiresAt(now.plusMinutes(settings.getHoldDurationMinutes()));
        reservation.setCurrency(price.currency());
        reservation.setRoomSubtotal(price.roomSubtotal());
        reservation.setResortFee(price.resortFee());
        reservation.setTaxAmount(price.taxes());
        reservation.setTotalAmount(price.total());
        reservation.setPriceBreakdown(serialize(price));
        Reservation saved = reservationRepository.saveAndFlush(reservation);
        return new HoldDto(saved.getId(), saved.getExpiresAt(), accommodationMapper.toDto(type), saved.getCheckIn(),
                saved.getCheckOut(), saved.getAdults(), saved.getChildren(), price);
    }

    @Transactional
    public CreatedReservationDto createReservation(UUID holdId, String idempotencyKey, ReservationRequest request) {
        String key = validateIdempotencyKey(idempotencyKey);
        Optional<Reservation> prior = reservationRepository.findByReservationCreationKey(key);
        if (prior.isPresent()) {
            if (!prior.get().getId().equals(holdId)) {
                throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED", "This idempotency key was already used for a different reservation.");
            }
            return created(prior.get(), null);
        }
        Reservation hold = reservationRepository.findByIdForUpdate(holdId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "HOLD_NOT_FOUND", "The reservation hold was not found."));
        Optional<Reservation> completedDuringLock = reservationRepository.findByReservationCreationKey(key);
        if (completedDuringLock.isPresent()) {
            if (!completedDuringLock.get().getId().equals(holdId)) {
                throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED", "This idempotency key was already used for a different reservation.");
            }
            return created(completedDuringLock.get(), null);
        }
        ensureActiveHold(hold);
        if (hold.getGuest() != null || hold.getReservationCreationKey() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "HOLD_ALREADY_USED", "This hold has already been used to create a reservation.");
        }

        Guest guest = new Guest();
        guest.setFirstName(request.firstName().trim());
        guest.setLastName(request.lastName().trim());
        guest.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        guest.setPhone(request.phone().trim());
        guest.setCountry(request.country().trim());
        hold.setGuest(guestRepository.save(guest));
        hold.setArrivalNotes(clean(request.arrivalNotes()));
        hold.setSpecialRequests(clean(request.specialRequests()));
        hold.setEstimatedArrivalTime(clean(request.estimatedArrivalTime()));
        hold.setCelebrationType(request.celebrationType() == null ? null : request.celebrationType().name());
        hold.setReservationCreationKey(key);
        byte[] rawToken = new byte[32];
        secureRandom.nextBytes(rawToken);
        String managementToken = Base64.getUrlEncoder().withoutPadding().encodeToString(rawToken);
        hold.setManagementTokenHash(hash(managementToken));
        Reservation saved = reservationRepository.saveAndFlush(hold);
        return created(saved, managementToken);
    }

    @Transactional
    public PaymentSimulationDto simulatePayment(UUID reservationId, String idempotencyKey, PaymentSimulationRequest request) {
        String key = validateIdempotencyKey(idempotencyKey);
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "The reservation was not found."));
        if (key.equals(reservation.getLastPaymentKey())) {
            return paymentResponse(reservation);
        }
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return new PaymentSimulationDto(reservation.getId(), reservation.getStatus().name(), "SUCCESS",
                    reservation.getSimulatedPaymentReference(), reservation.getConfirmationNumber(),
                    "This reservation was already confirmed; no additional simulated payment was created.");
        }
        if (reservation.getStatus() != ReservationStatus.HELD || reservation.getGuest() == null) {
            throw new ApiException(HttpStatus.CONFLICT, "RESERVATION_NOT_PAYABLE", "Only an active reservation with guest details can be submitted to simulated payment.");
        }
        if (reservation.getExpiresAt() == null || !reservation.getExpiresAt().isAfter(OffsetDateTime.now())) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            throw new ApiException(HttpStatus.GONE, "HOLD_EXPIRED", "The reservation hold has expired.");
        }

        reservation.setLastPaymentKey(key);
        reservation.setLastPaymentResult(request.result().name());
        if (request.result() == PaymentResult.DECLINED) {
            reservationRepository.saveAndFlush(reservation);
            return paymentResponse(reservation);
        }
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setExpiresAt(null);
        reservation.setConfirmationNumber(generateConfirmationNumber());
        reservation.setSimulatedPaymentReference("SIM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT));
        reservationRepository.saveAndFlush(reservation);
        return paymentResponse(reservation);
    }

    @Transactional(readOnly = true)
    public ManagedReservationDto findManaged(String token) {
        Reservation reservation = reservationRepository.findByManagementTokenHash(hash(token))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "No reservation was found for this management token."));
        String guestName = reservation.getGuest() == null ? null :
                reservation.getGuest().getFirstName() + " " + reservation.getGuest().getLastName();
        String status = reservation.getStatus() == ReservationStatus.HELD
                && (reservation.getExpiresAt() == null || !reservation.getExpiresAt().isAfter(OffsetDateTime.now()))
                ? ReservationStatus.EXPIRED.name() : reservation.getStatus().name();
        return new ManagedReservationDto(reservation.getConfirmationNumber(),
                reservation.getAccommodationUnit().getAccommodationType().getName(), reservation.getCheckIn(),
                reservation.getCheckOut(), reservation.getAdults(), reservation.getChildren(), status,
                guestName, reservation.getSpecialRequests(), deserialize(reservation.getPriceBreakdown()));
    }

    @Transactional
    public CancellationDto cancel(String token) {
        Reservation reservation = reservationRepository.findByManagementTokenHash(hash(token))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "No reservation was found for this management token."));
        reservation = reservationRepository.findByIdForUpdate(reservation.getId()).orElseThrow();
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ApiException(HttpStatus.CONFLICT, "RESERVATION_ALREADY_CANCELLED", "This reservation has already been cancelled.");
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED && reservation.getStatus() != ReservationStatus.HELD) {
            throw new ApiException(HttpStatus.CONFLICT, "RESERVATION_CANNOT_BE_CANCELLED", "This reservation cannot be cancelled in its current state.");
        }
        ResortSettings cancellationPolicy = settings();
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime checkInTime = reservation.getCheckIn().atTime(cancellationPolicy.getCheckInTime())
                .atOffset(now.getOffset());
        boolean freeCancellation = reservation.getStatus() == ReservationStatus.HELD
                || Duration.between(now, checkInTime).toHours() >= cancellationPolicy.getCancellationWindowHours();
        BigDecimal penalty = BigDecimal.ZERO;
        if (!freeCancellation) {
            PriceQuote quote = deserialize(reservation.getPriceBreakdown());
            penalty = quote.nightlyCharges().getFirst().nightlyRate();
        }
        BigDecimal simulatedRefund = reservation.getStatus() == ReservationStatus.CONFIRMED
                ? reservation.getTotalAmount().subtract(penalty).max(BigDecimal.ZERO) : BigDecimal.ZERO;
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setExpiresAt(null);
        reservationRepository.save(reservation);
        return new CancellationDto("CANCELLED", freeCancellation, penalty, simulatedRefund,
                "Cancellation and any refund or penalty are simulated only; no real payment or refund was processed.");
    }

    private void ensureActiveHold(Reservation hold) {
        if (hold.getStatus() != ReservationStatus.HELD) {
            throw new ApiException(HttpStatus.CONFLICT, "HOLD_NOT_ACTIVE", "The reservation hold is no longer active.");
        }
        if (hold.getExpiresAt() == null || !hold.getExpiresAt().isAfter(OffsetDateTime.now())) {
            hold.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(hold);
            throw new ApiException(HttpStatus.GONE, "HOLD_EXPIRED", "The reservation hold has expired.");
        }
    }

    private CreatedReservationDto created(Reservation reservation, String managementToken) {
        PriceQuote price = deserialize(reservation.getPriceBreakdown());
        return new CreatedReservationDto(reservation.getId(), reservation.getConfirmationNumber(), managementToken,
                reservation.getStatus().name(), reservation.getAccommodationUnit().getAccommodationType().getName(),
                reservation.getCheckIn(), reservation.getCheckOut(), reservation.getAdults(), reservation.getChildren(), price);
    }

    private PaymentSimulationDto paymentResponse(Reservation reservation) {
        return new PaymentSimulationDto(reservation.getId(), reservation.getStatus().name(),
                reservation.getLastPaymentResult(), reservation.getSimulatedPaymentReference(),
                reservation.getConfirmationNumber(),
                "Payment simulation only: no payment provider or real financial transaction was used.");
    }

    private ResortSettings settings() {
        return settingsRepository.findById((short) 1)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "RESORT_SETTINGS_UNAVAILABLE", "Resort settings are unavailable."));
    }

    private String serialize(PriceQuote quote) {
        try {
            return objectMapper.writeValueAsString(quote);
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "PRICE_BREAKDOWN_ERROR", "The price breakdown could not be stored.");
        }
    }

    private PriceQuote deserialize(String value) {
        try {
            return objectMapper.readValue(value, PriceQuote.class);
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "PRICE_BREAKDOWN_ERROR", "The saved price breakdown could not be read.");
        }
    }

    private String validateIdempotencyKey(String key) {
        if (key == null || key.length() < 8 || key.length() > 120 || !key.matches("[A-Za-z0-9._:-]+")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_IDEMPOTENCY_KEY", "Idempotency-Key must be 8–120 letters, numbers, dots, underscores, colons, or hyphens.");
        }
        return key;
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String hash(String token) {
        if (token == null || token.length() < 32 || token.length() > 100) {
            throw new ApiException(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "No reservation was found for this management token.");
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by the Java runtime.", exception);
        }
    }

    private String generateConfirmationNumber() {
        StringBuilder value = new StringBuilder("VC-");
        for (int i = 0; i < 6; i++) value.append(CONFIRMATION_ALPHABET[secureRandom.nextInt(CONFIRMATION_ALPHABET.length)]);
        return value.toString();
    }

    private ApiException unavailable() {
        return new ApiException(HttpStatus.CONFLICT, "ACCOMMODATION_NO_LONGER_AVAILABLE",
                "The selected accommodation is no longer available for these dates.");
    }
}
