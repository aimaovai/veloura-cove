package com.velouracove.accommodation;

import com.velouracove.accommodation.domain.AccommodationType;
import com.velouracove.api.ApiDtos.AccommodationDto;
import com.velouracove.api.ApiDtos.ComparisonDto;
import com.velouracove.api.ApiDtos.ImageDto;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class AccommodationMapper {
    public AccommodationDto toDto(AccommodationType type) {
        List<ImageDto> images = type.getImages().stream()
                .map(image -> new ImageDto(image.getImageUrl(), image.getAltText(), image.getImageType(), image.getDisplayOrder()))
                .toList();
        return new AccommodationDto(type.getId(), type.getSlug(), type.getName(), type.getShortDescription(),
                type.getLongDescription(), type.getCategory(), type.getMaxGuests(), type.getMaxAdults(),
                type.getMaxChildren(), type.getBedConfiguration(), type.getRoomSizeSqFt(), type.getBaseNightlyRate(),
                type.getCurrency(), type.getViewType(), type.isFeatured(), type.getHeroImage(), images,
                type.getAmenities().stream().map(a -> a.getName()).sorted().toList(),
                type.getFeatures(), type.getCheckInTime().toString(), type.getCheckOutTime().toString());
    }

    public ComparisonDto toComparison(AccommodationType type) {
        List<String> names = type.getAmenities().stream().map(a -> a.getName()).sorted().toList();
        List<String> notable = names.stream().filter(name -> List.of("Private pool", "Ocean view", "Rain shower",
                "Deep soaking tub", "Butler service", "Breakfast included", "Terrace", "Outdoor shower").contains(name))
                .sorted(Comparator.naturalOrder()).toList();
        return new ComparisonDto(type.getId(), type.getName(), type.getCategory(), type.getBaseNightlyRate(),
                type.getCurrency(), type.getRoomSizeSqFt(), type.getMaxGuests(), type.getBedConfiguration(),
                type.getViewType(), names.contains("Private pool"), names.contains("Terrace"),
                names.contains("Breakfast included"), names.contains("Butler service"), notable, type.getHeroImage());
    }
}
