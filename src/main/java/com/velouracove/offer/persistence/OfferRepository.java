package com.velouracove.offer.persistence;

import com.velouracove.offer.domain.Offer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {
    List<Offer> findByActiveTrueOrderByNameAsc();
    @EntityGraph(attributePaths = "applicableAccommodationTypes")
    Optional<Offer> findBySlugAndActiveTrue(String slug);
}
