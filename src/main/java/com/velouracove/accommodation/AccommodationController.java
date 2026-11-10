package com.velouracove.accommodation;

import com.velouracove.accommodation.domain.AccommodationCategory;
import com.velouracove.api.ApiDtos.AccommodationDto;
import com.velouracove.api.ApiDtos.ComparisonDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accommodation-types")
@Validated
public class AccommodationController {
    private final AccommodationService service;

    public AccommodationController(AccommodationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List bookable accommodation types", description = "Optional category, guest-count, and featured filters are applied to active accommodation types.")
    public List<AccommodationDto> list(@RequestParam(required = false) AccommodationCategory category,
                                       @RequestParam(required = false) @Min(1) Integer guests,
                                       @RequestParam(required = false) Boolean featured) {
        return service.list(category, guests, featured);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get accommodation details", description = "Includes descriptions, occupancy, images, amenities, features, and base starting rate.")
    public AccommodationDto get(@PathVariable String slug) {
        return service.findBySlug(slug);
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare accommodation types", description = "Accepts two or three distinct active accommodation UUIDs.")
    public List<ComparisonDto> compare(@RequestParam List<UUID> ids) {
        return service.compare(ids);
    }
}
