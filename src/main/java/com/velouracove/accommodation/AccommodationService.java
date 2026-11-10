package com.velouracove.accommodation;

import com.velouracove.accommodation.domain.AccommodationCategory;
import com.velouracove.accommodation.domain.AccommodationType;
import com.velouracove.accommodation.persistence.AccommodationTypeRepository;
import com.velouracove.api.ApiDtos.AccommodationDto;
import com.velouracove.api.ApiDtos.ComparisonDto;
import com.velouracove.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccommodationService {
    private final AccommodationTypeRepository repository;
    private final AccommodationMapper mapper;

    public AccommodationService(AccommodationTypeRepository repository, AccommodationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<AccommodationDto> list(AccommodationCategory category, Integer guests, Boolean featured) {
        return repository.findByActiveTrueOrderBySortOrderAsc().stream()
                .filter(type -> category == null || type.getCategory() == category)
                .filter(type -> guests == null || type.getMaxGuests() >= guests)
                .filter(type -> featured == null || !featured || type.isFeatured())
                .map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AccommodationDto findBySlug(String slug) {
        return repository.findBySlugAndActiveTrue(slug).map(mapper::toDto)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "The requested accommodation was not found."));
    }

    @Transactional(readOnly = true)
    public List<ComparisonDto> compare(List<UUID> ids) {
        if (ids == null || ids.size() < 2 || ids.size() > 3 || ids.stream().distinct().count() != ids.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_COMPARISON", "Select two or three different accommodation types to compare.");
        }
        List<AccommodationType> types = repository.findByIdInAndActiveTrue(ids);
        if (types.size() != ids.size()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "One or more selected accommodations were not found.");
        }
        return ids.stream().map(id -> types.stream().filter(type -> type.getId().equals(id)).findFirst().orElseThrow())
                .map(mapper::toComparison).toList();
    }
}
