package com.velouracove.availability;

import com.velouracove.accommodation.AccommodationMapper;
import com.velouracove.accommodation.domain.AccommodationType;
import com.velouracove.accommodation.domain.AccommodationUnit;
import com.velouracove.accommodation.domain.AccommodationUnit.MaintenanceStatus;
import com.velouracove.accommodation.persistence.AccommodationTypeRepository;
import com.velouracove.accommodation.persistence.AccommodationUnitRepository;
import com.velouracove.api.ApiDtos.*;
import com.velouracove.common.ApiException;
import com.velouracove.common.StayRules;
import com.velouracove.config.persistence.ResortSettingsRepository;
import com.velouracove.pricing.PricingService;
import com.velouracove.reservation.domain.Reservation.ReservationStatus;
import com.velouracove.reservation.persistence.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AvailabilityService {
    private static final List<ReservationStatus> BLOCKING_STATUSES = List.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN);
    private final AccommodationTypeRepository typeRepository;
    private final AccommodationUnitRepository unitRepository;
    private final ReservationRepository reservationRepository;
    private final PricingService pricingService;
    private final AccommodationMapper mapper;
    private final ResortSettingsRepository settingsRepository;

    public AvailabilityService(AccommodationTypeRepository typeRepository, AccommodationUnitRepository unitRepository,
                               ReservationRepository reservationRepository, PricingService pricingService,
                               AccommodationMapper mapper, ResortSettingsRepository settingsRepository) {
        this.typeRepository = typeRepository;
        this.unitRepository = unitRepository;
        this.reservationRepository = reservationRepository;
        this.pricingService = pricingService;
        this.mapper = mapper;
        this.settingsRepository = settingsRepository;
    }

    @Transactional(readOnly = true)
    public List<AvailabilityDto> search(LocalDate checkIn, LocalDate checkOut, int adults, int children, UUID typeId) {
        StayRules.validate(checkIn, checkOut, adults, children, maxStayNights());
        List<AccommodationType> types;
        if (typeId != null) {
            AccommodationType type = typeRepository.findByIdInAndActiveTrue(List.of(typeId)).stream().findFirst()
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "The requested accommodation was not found."));
            if (!fits(type, adults, children)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OCCUPANCY", "Guest count exceeds the selected accommodation's capacity.");
            }
            types = List.of(type);
        } else {
            types = typeRepository.findByActiveTrueOrderBySortOrderAsc().stream().filter(type -> fits(type, adults, children)).toList();
        }
        OffsetDateTime now = OffsetDateTime.now();
        List<AvailabilityDto> results = new ArrayList<>();
        for (AccommodationType type : types) {
            List<AccommodationUnit> units = unitRepository.findEligible(type.getId(), MaintenanceStatus.READY);
            int remaining = remaining(units, checkIn, checkOut, now);
            PriceQuote quote = pricingService.quote(type, checkIn, checkOut);
            String status = remaining == 0 ? "SOLD_OUT"
                    : remaining < units.size() && (remaining <= 2 || remaining * 4 <= units.size()) ? "LIMITED" : "AVAILABLE";
            results.add(new AvailabilityDto(mapper.toDto(type), units.size(), remaining,
                    quote.nightlyCharges().stream().map(PriceLine::nightlyRate).min(Comparator.naturalOrder()).orElseThrow(),
                    quote, status));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<CalendarDay> calendar(String slug, LocalDate startDate, LocalDate endDate, int adults, int children) {
        if (startDate == null || endDate == null || startDate.isBefore(LocalDate.now()) || !startDate.isBefore(endDate)
                || ChronoUnit.DAYS.between(startDate, endDate) > 90) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CALENDAR_RANGE", "Calendar dates must be future dates in a range of at most 90 days.");
        }
        StayRules.validate(startDate, startDate.plusDays(1), adults, children, maxStayNights());
        AccommodationType type = typeRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "The requested accommodation was not found."));
        if (!fits(type, adults, children)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OCCUPANCY", "Guest count exceeds the selected accommodation's capacity.");
        }
        List<AccommodationUnit> units = unitRepository.findEligible(type.getId(), MaintenanceStatus.READY);
        OffsetDateTime now = OffsetDateTime.now();
        List<CalendarDay> days = new ArrayList<>();
        for (LocalDate day = startDate; day.isBefore(endDate); day = day.plusDays(1)) {
            int count = remaining(units, day, day.plusDays(1), now);
            days.add(new CalendarDay(day, count > 0, count, pricingService.nightlyRate(type, day), 1, count > 0, true));
        }
        return days;
    }

    public int remaining(List<AccommodationUnit> units, LocalDate checkIn, LocalDate checkOut, OffsetDateTime now) {
        if (units.isEmpty()) return 0;
        Set<UUID> unitIds = new HashSet<>();
        units.forEach(unit -> unitIds.add(unit.getId()));
        List<com.velouracove.reservation.domain.Reservation> reservations =
                reservationRepository.findBlockingOverlaps(unitIds, checkIn, checkOut, BLOCKING_STATUSES, ReservationStatus.HELD, now);
        Set<UUID> occupied = new HashSet<>();
        reservations.forEach(reservation -> occupied.add(reservation.getAccommodationUnit().getId()));
        return (int) units.stream().filter(unit -> !occupied.contains(unit.getId())).count();
    }

    public boolean fits(AccommodationType type, int adults, int children) {
        return adults <= type.getMaxAdults() && children <= type.getMaxChildren() && adults + children <= type.getMaxGuests();
    }

    private int maxStayNights() {
        return settingsRepository.findById((short) 1)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "RESORT_SETTINGS_UNAVAILABLE", "Resort settings are unavailable."))
                .getMaxStayNights();
    }
}
