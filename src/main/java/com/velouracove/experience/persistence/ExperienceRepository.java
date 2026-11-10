package com.velouracove.experience.persistence;

import com.velouracove.experience.domain.Experience;
import com.velouracove.experience.domain.Experience.ExperienceCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExperienceRepository extends JpaRepository<Experience, UUID> {
    @EntityGraph(attributePaths = "images")
    List<Experience> findByActiveTrueOrderByFeaturedDescNameAsc();
    @EntityGraph(attributePaths = "images")
    List<Experience> findByActiveTrueAndCategoryOrderByFeaturedDescNameAsc(ExperienceCategory category);
    @EntityGraph(attributePaths = "images")
    Optional<Experience> findBySlugAndActiveTrue(String slug);
}
