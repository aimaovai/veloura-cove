package com.velouracove.accommodation.persistence;

import com.velouracove.accommodation.domain.AccommodationCategory;
import com.velouracove.accommodation.domain.AccommodationType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccommodationTypeRepository extends JpaRepository<AccommodationType, UUID> {
    @EntityGraph(attributePaths = {"amenities", "features", "images"})
    List<AccommodationType> findByActiveTrueOrderBySortOrderAsc();

    @EntityGraph(attributePaths = {"amenities", "features", "images"})
    Optional<AccommodationType> findBySlugAndActiveTrue(String slug);

    @EntityGraph(attributePaths = {"amenities", "features", "images"})
    List<AccommodationType> findByIdInAndActiveTrue(List<UUID> ids);

    @EntityGraph(attributePaths = {"amenities", "features", "images"})
    List<AccommodationType> findByActiveTrueAndFeaturedTrueOrderBySortOrderAsc();

    @EntityGraph(attributePaths = {"amenities", "features", "images"})
    List<AccommodationType> findByActiveTrueAndCategoryOrderBySortOrderAsc(AccommodationCategory category);
}
