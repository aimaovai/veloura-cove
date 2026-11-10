package com.velouracove.availability;

import com.velouracove.api.ApiDtos.AvailabilityDto;
import com.velouracove.api.ApiDtos.CalendarDay;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
public class AvailabilityController {
    private final AvailabilityService service;

    public AvailabilityController(AvailabilityService service) {
        this.service = service;
    }

    @GetMapping("/availability")
    @Operation(summary = "Search accommodation availability",
            description = "Checks physical eligible units against overlapping held/confirmed stays. Checkout is exclusive; expired holds do not block inventory.")
    public List<AvailabilityDto> search(@RequestParam @NotNull LocalDate checkIn,
                                        @RequestParam @NotNull LocalDate checkOut,
                                        @RequestParam @Min(1) @Max(12) int adults,
                                        @RequestParam @Min(0) @Max(8) int children,
                                        @RequestParam(required = false) UUID accommodationTypeId) {
        return service.search(checkIn, checkOut, adults, children, accommodationTypeId);
    }

    @GetMapping("/accommodation-types/{slug}/availability-calendar")
    @Operation(summary = "Get nightly calendar availability",
            description = "Returns one inventory-backed result per date in the half-open [startDate, endDate) range. The range is limited to 90 days.")
    public List<CalendarDay> calendar(@PathVariable String slug, @RequestParam @NotNull LocalDate startDate,
                                      @RequestParam @NotNull LocalDate endDate, @RequestParam @Min(1) @Max(12) int adults,
                                      @RequestParam @Min(0) @Max(8) int children) {
        return service.calendar(slug, startDate, endDate, adults, children);
    }
}
