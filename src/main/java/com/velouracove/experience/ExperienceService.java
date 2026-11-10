package com.velouracove.experience;

import com.velouracove.api.ApiDtos.ExperienceDto;
import com.velouracove.api.ApiDtos.ImageDto;
import com.velouracove.common.ApiException;
import com.velouracove.experience.domain.Experience;
import com.velouracove.experience.domain.Experience.ExperienceCategory;
import com.velouracove.experience.persistence.ExperienceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExperienceService {
    private final ExperienceRepository repository;

    public ExperienceService(ExperienceRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ExperienceDto> list(ExperienceCategory category) {
        List<Experience> experiences = category == null ? repository.findByActiveTrueOrderByFeaturedDescNameAsc()
                : repository.findByActiveTrueAndCategoryOrderByFeaturedDescNameAsc(category);
        return experiences.stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public ExperienceDto get(String slug) {
        return repository.findBySlugAndActiveTrue(slug).map(this::map)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EXPERIENCE_NOT_FOUND", "The requested experience was not found."));
    }

    private ExperienceDto map(Experience experience) {
        List<ImageDto> images = experience.getImages().stream()
                .map(image -> new ImageDto(image.getImageUrl(), image.getAltText(), "GALLERY", image.getDisplayOrder()))
                .toList();
        return new ExperienceDto(experience.getSlug(), experience.getName(), experience.getCategory(),
                experience.getShortDescription(), experience.getLongDescription(), experience.getDurationMinutes(),
                experience.getStartingPrice(), experience.getCurrency(), experience.getHeroImage(),
                experience.isFeatured(), images);
    }
}
