package com.velouracove.experience;

import com.velouracove.api.ApiDtos.ExperienceDto;
import com.velouracove.experience.domain.Experience.ExperienceCategory;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/experiences")
public class ExperienceController {
    private final ExperienceService service;

    public ExperienceController(ExperienceService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List resort experiences", description = "Optionally filter experiences by category.")
    public List<ExperienceDto> list(@RequestParam(required = false) ExperienceCategory category) {
        return service.list(category);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get experience details", description = "Returns active experience content, pricing, and gallery.")
    public ExperienceDto get(@PathVariable String slug) {
        return service.get(slug);
    }
}
