package com.velouracove.pricing.domain;

import com.velouracove.accommodation.domain.AccommodationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "seasonal_rates")
@Getter @Setter @NoArgsConstructor
public class SeasonalRate {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_type_id")
    private AccommodationType accommodationType;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    @Column(name = "nightly_rate", precision = 12, scale = 2)
    private BigDecimal nightlyRate;
    @Column(name = "percentage_adjustment", precision = 7, scale = 4)
    private BigDecimal percentageAdjustment;
    @Column(nullable = false)
    private int priority;
}
