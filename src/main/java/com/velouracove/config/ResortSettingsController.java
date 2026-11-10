package com.velouracove.config;

import com.velouracove.api.ApiDtos.ResortSettingsDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/resort-settings")
public class ResortSettingsController {
    private final ResortSettingsService service;

    public ResortSettingsController(ResortSettingsService service) {
        this.service = service;
    }

    @GetMapping
    public ResortSettingsDto get() {
        return service.get();
    }
}
