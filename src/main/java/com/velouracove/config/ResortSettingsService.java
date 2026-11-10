package com.velouracove.config;

import com.velouracove.api.ApiDtos.ResortSettingsDto;
import com.velouracove.common.ApiException;
import com.velouracove.config.persistence.ResortSettingsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResortSettingsService {
    private final ResortSettingsRepository repository;

    public ResortSettingsService(ResortSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ResortSettingsDto get() {
        var settings = repository.findById((short) 1)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "RESORT_SETTINGS_UNAVAILABLE", "Resort settings are unavailable."));
        return new ResortSettingsDto(settings.getResortName(), settings.getPhone(), settings.getEmail(),
                settings.getAddressText(), settings.getCheckInTime().toString(), settings.getCheckOutTime().toString(),
                settings.getDefaultCurrency(), settings.getResortFeePerNight(), settings.getTaxPercentage(),
                settings.getCancellationWindowHours(), settings.getHoldDurationMinutes(), settings.getMaxStayNights());
    }
}
