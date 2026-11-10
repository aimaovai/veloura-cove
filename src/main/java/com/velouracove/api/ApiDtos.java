package com.velouracove.api;

import com.velouracove.accommodation.domain.AccommodationCategory;
import com.velouracove.experience.domain.Experience.ExperienceCategory;
import com.velouracove.inquiry.domain.Inquiry.InquiryType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ApiDtos {
    private ApiDtos() {}

    public record ImageDto(String url, String altText, String type, int displayOrder) {}
    public record AccommodationDto(UUID id, String slug, String name, String shortDescription, String longDescription,
                                  AccommodationCategory category, int maxGuests, int maxAdults, int maxChildren,
                                  String bedConfiguration, int roomSizeSqFt, BigDecimal startingNightlyRate,
                                  String currency, String viewType, boolean featured, String heroImage,
                                  List<ImageDto> gallery, List<String> amenities, List<String> features,
                                  String checkInTime, String checkOutTime) {}
    public record ComparisonDto(UUID id, String name, AccommodationCategory category, BigDecimal startingNightlyRate,
                                String currency, int roomSizeSqFt, int maxGuests, String bedConfiguration,
                                String viewType, boolean privatePool, boolean terrace, boolean breakfastIncluded,
                                boolean butlerService, List<String> notableAmenities, String heroImage) {}
    public record PriceLine(LocalDate date, BigDecimal nightlyRate) {}
    public record PriceQuote(List<PriceLine> nightlyCharges, BigDecimal roomSubtotal, BigDecimal resortFee,
                             BigDecimal taxes, BigDecimal total, String currency) {}
    public record AvailabilityDto(AccommodationDto accommodation, int totalUnits, int unitsRemaining,
                                  BigDecimal nightlyStartingRate, PriceQuote estimatedPrice, String status) {}
    public record CalendarDay(LocalDate date, boolean available, int remainingInventory, BigDecimal nightlyRate,
                              int minimumStay, boolean arrivalAllowed, boolean departureAllowed) {}
    public record HoldRequest(@NotNull UUID accommodationTypeId, @NotNull LocalDate checkIn, @NotNull LocalDate checkOut,
                              @Min(1) @Max(12) int adults, @Min(0) @Max(8) int children) {}
    public record HoldDto(UUID holdId, OffsetDateTime expiresAt, AccommodationDto accommodation, LocalDate checkIn,
                          LocalDate checkOut, int adults, int children, PriceQuote price) {}
    public record ReservationRequest(@NotNull UUID holdId,
                                     @NotBlank @Size(max = 100) String firstName,
                                     @NotBlank @Size(max = 100) String lastName,
                                     @NotBlank @Email @Size(max = 254) String email,
                                     @NotBlank @Size(max = 50) String phone,
                                     @NotBlank @Size(max = 100) String country,
                                     @Size(max = 1000) String arrivalNotes,
                                     @Size(max = 2000) String specialRequests,
                                     @Size(max = 40) String estimatedArrivalTime,
                                     CelebrationType celebrationType) {}
    public enum CelebrationType { ANNIVERSARY, HONEYMOON, BIRTHDAY, NONE }
    public record CreatedReservationDto(UUID reservationId, String confirmationNumber, String managementToken,
                                        String status, String accommodationName, LocalDate checkIn, LocalDate checkOut,
                                        int adults, int children, PriceQuote price) {}
    public record PaymentSimulationRequest(@NotNull PaymentResult result) {}
    public enum PaymentResult { SUCCESS, DECLINED }
    public record PaymentSimulationDto(UUID reservationId, String status, String outcome, String paymentReference,
                                       String confirmationNumber, String message) {}
    public record ManagedReservationDto(String confirmationNumber, String accommodationName, LocalDate checkIn,
                                        LocalDate checkOut, int adults, int children, String status,
                                        String guestName, String specialRequests, PriceQuote price) {}
    public record CancellationDto(String status, boolean freeCancellation, BigDecimal simulatedPenalty,
                                  BigDecimal simulatedRefund, String message) {}
    public record InquiryRequest(@NotBlank @Size(max = 150) String name,
                                 @NotBlank @Email @Size(max = 254) String email,
                                 @Size(max = 50) String phone, LocalDate eventDate,
                                 @Min(1) @Max(10000) Integer partySize,
                                 @NotNull InquiryType inquiryType,
                                 @NotBlank @Size(max = 3000) String message) {}
    public record InquiryCreatedDto(UUID inquiryId, String status, OffsetDateTime createdAt) {}
    public record OfferDto(String slug, String name, String description, String heroImage, String discountType,
                           BigDecimal discountValue, int minimumNights, LocalDate validFrom, LocalDate validUntil,
                           String terms, List<String> applicableAccommodationSlugs) {}
    public record ExperienceDto(String slug, String name, ExperienceCategory category, String shortDescription,
                                String longDescription, int durationMinutes, BigDecimal startingPrice,
                                String currency, String heroImage, boolean featured, List<ImageDto> gallery) {}
    public record ResortSettingsDto(String resortName, String phone, String email, String address,
                                    String checkInTime, String checkOutTime, String currency,
                                    BigDecimal resortFeePerNight, BigDecimal taxPercentage,
                                    int cancellationWindowHours, int holdDurationMinutes, int maxStayNights) {}
}
