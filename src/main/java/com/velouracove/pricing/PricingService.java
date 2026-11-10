package com.velouracove.pricing;

import com.velouracove.accommodation.domain.AccommodationType;
import com.velouracove.api.ApiDtos.PriceLine;
import com.velouracove.api.ApiDtos.PriceQuote;
import com.velouracove.common.ApiException;
import com.velouracove.config.domain.ResortSettings;
import com.velouracove.config.persistence.ResortSettingsRepository;
import com.velouracove.pricing.domain.SeasonalRate;
import com.velouracove.pricing.persistence.SeasonalRateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PricingService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private final SeasonalRateRepository seasonalRateRepository;
    private final ResortSettingsRepository settingsRepository;

    public PricingService(SeasonalRateRepository seasonalRateRepository, ResortSettingsRepository settingsRepository) {
        this.seasonalRateRepository = seasonalRateRepository;
        this.settingsRepository = settingsRepository;
    }

    @Transactional(readOnly = true)
    public PriceQuote quote(AccommodationType accommodation, LocalDate checkIn, LocalDate checkOut) {
        ResortSettings settings = settingsRepository.findById((short) 1)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "RESORT_SETTINGS_UNAVAILABLE", "Resort pricing settings are unavailable."));
        List<PriceLine> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
            BigDecimal rate = accommodation.getBaseNightlyRate();
            List<SeasonalRate> seasonalRates = seasonalRateRepository.findApplicable(accommodation.getId(), night);
            if (!seasonalRates.isEmpty()) {
                SeasonalRate seasonal = seasonalRates.getFirst();
                rate = seasonal.getNightlyRate() != null
                        ? seasonal.getNightlyRate()
                        : rate.multiply(BigDecimal.ONE.add(seasonal.getPercentageAdjustment().divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP)));
            }
            rate = money(rate);
            lines.add(new PriceLine(night, rate));
            subtotal = subtotal.add(rate);
        }
        BigDecimal resortFee = money(settings.getResortFeePerNight().multiply(BigDecimal.valueOf(lines.size())));
        BigDecimal taxes = money(subtotal.add(resortFee)
                .multiply(settings.getTaxPercentage()).divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP));
        return new PriceQuote(List.copyOf(lines), money(subtotal), resortFee, taxes,
                money(subtotal.add(resortFee).add(taxes)), accommodation.getCurrency());
    }

    public BigDecimal nightlyRate(AccommodationType accommodation, LocalDate date) {
        return quote(accommodation, date, date.plusDays(1)).nightlyCharges().getFirst().nightlyRate();
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
