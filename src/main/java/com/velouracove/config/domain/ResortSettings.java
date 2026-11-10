package com.velouracove.config.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "resort_settings")
@Getter @Setter @NoArgsConstructor
public class ResortSettings {
    @Id
    private Short id;
    @Column(name = "resort_name", nullable = false, length = 150)
    private String resortName;
    @Column(nullable = false, length = 50)
    private String phone;
    @Column(nullable = false, length = 254)
    private String email;
    @Column(name = "address_text", nullable = false, length = 500)
    private String addressText;
    @Column(name = "check_in_time", nullable = false)
    private LocalTime checkInTime;
    @Column(name = "check_out_time", nullable = false)
    private LocalTime checkOutTime;
    @Column(name = "default_currency", nullable = false, length = 3)
    private String defaultCurrency;
    @Column(name = "resort_fee_per_night", nullable = false, precision = 12, scale = 2)
    private BigDecimal resortFeePerNight;
    @Column(name = "tax_percentage", nullable = false, precision = 7, scale = 4)
    private BigDecimal taxPercentage;
    @Column(name = "cancellation_window_hours", nullable = false)
    private int cancellationWindowHours;
    @Column(name = "hold_duration_minutes", nullable = false)
    private int holdDurationMinutes;
    @Column(name = "max_stay_nights", nullable = false)
    private int maxStayNights;
}
