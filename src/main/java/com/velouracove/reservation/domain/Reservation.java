package com.velouracove.reservation.domain;

import com.velouracove.accommodation.domain.AccommodationUnit;
import com.velouracove.guest.domain.Guest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Getter @Setter @NoArgsConstructor
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accommodation_unit_id", nullable = false)
    private AccommodationUnit accommodationUnit;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id")
    private Guest guest;
    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;
    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;
    @Column(nullable = false)
    private int adults;
    @Column(nullable = false)
    private int children;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private ReservationStatus status;
    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;
    @Column(name = "arrival_notes", length = 1000)
    private String arrivalNotes;
    @Column(name = "special_requests", length = 2000)
    private String specialRequests;
    @Column(name = "estimated_arrival_time", length = 40)
    private String estimatedArrivalTime;
    @Column(name = "celebration_type", length = 40)
    private String celebrationType;
    @Column(name = "confirmation_number", unique = true, length = 20)
    private String confirmationNumber;
    @Column(name = "management_token_hash", unique = true, length = 64)
    private String managementTokenHash;
    @Column(name = "reservation_creation_key", unique = true, length = 120)
    private String reservationCreationKey;
    @Column(name = "last_payment_key", length = 120)
    private String lastPaymentKey;
    @Column(name = "last_payment_result", length = 20)
    private String lastPaymentResult;
    @Column(name = "simulated_payment_reference", length = 40)
    private String simulatedPaymentReference;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "room_subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal roomSubtotal;
    @Column(name = "resort_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal resortFee;
    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;
    @Column(name = "price_breakdown", nullable = false, columnDefinition = "text")
    private String priceBreakdown;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public enum ReservationStatus { HELD, CONFIRMED, CANCELLED, EXPIRED, CHECKED_IN, CHECKED_OUT }
}
