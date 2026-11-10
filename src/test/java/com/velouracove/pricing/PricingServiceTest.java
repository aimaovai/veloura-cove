package com.velouracove.pricing;

import com.velouracove.accommodation.domain.AccommodationType;
import com.velouracove.api.ApiDtos.PriceQuote;
import com.velouracove.config.domain.ResortSettings;
import com.velouracove.config.persistence.ResortSettingsRepository;
import com.velouracove.pricing.domain.SeasonalRate;
import com.velouracove.pricing.persistence.SeasonalRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PricingServiceTest {
    private SeasonalRateRepository seasonalRates;
    private ResortSettingsRepository settingsRepository;
    private PricingService service;
    private AccommodationType accommodation;

    @BeforeEach
    void setUp() {
        seasonalRates = mock(SeasonalRateRepository.class);
        settingsRepository = mock(ResortSettingsRepository.class);
        service = new PricingService(seasonalRates, settingsRepository);

        accommodation = new AccommodationType();
        accommodation.setId(UUID.randomUUID());
        accommodation.setBaseNightlyRate(new BigDecimal("100.00"));
        accommodation.setCurrency("USD");

        ResortSettings settings = new ResortSettings();
        settings.setResortFeePerNight(new BigDecimal("10.00"));
        settings.setTaxPercentage(new BigDecimal("10.0000"));
        when(settingsRepository.findById((short) 1)).thenReturn(Optional.of(settings));
        when(seasonalRates.findApplicable(any(), any())).thenReturn(List.of());
    }

    @Test
    void calculatesMultiNightSubtotalFeesTaxesAndTotal() {
        PriceQuote quote = service.quote(accommodation, LocalDate.of(2027, 5, 10), LocalDate.of(2027, 5, 12));

        assertEquals(2, quote.nightlyCharges().size());
        assertEquals(new BigDecimal("200.00"), quote.roomSubtotal());
        assertEquals(new BigDecimal("20.00"), quote.resortFee());
        assertEquals(new BigDecimal("22.00"), quote.taxes());
        assertEquals(new BigDecimal("242.00"), quote.total());
    }

    @Test
    void appliesHighestPriorityApplicableSeasonalPercentage() {
        SeasonalRate seasonal = new SeasonalRate();
        seasonal.setPercentageAdjustment(new BigDecimal("25.0000"));
        when(seasonalRates.findApplicable(any(), any())).thenReturn(List.of(seasonal));

        PriceQuote quote = service.quote(accommodation, LocalDate.of(2027, 12, 21), LocalDate.of(2027, 12, 22));

        assertEquals(new BigDecimal("125.00"), quote.roomSubtotal());
        assertEquals(new BigDecimal("148.50"), quote.total());
    }
}
