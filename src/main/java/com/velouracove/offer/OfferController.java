package com.velouracove.offer;

import com.velouracove.api.ApiDtos.OfferDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/offers")
public class OfferController {
    private final OfferService service;

    public OfferController(OfferService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List active offers")
    public List<OfferDto> list() {
        return service.list();
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get offer details")
    public OfferDto get(@PathVariable String slug) {
        return service.get(slug);
    }
}
