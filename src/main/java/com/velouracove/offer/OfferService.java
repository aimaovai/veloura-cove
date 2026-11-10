package com.velouracove.offer;

import com.velouracove.api.ApiDtos.OfferDto;
import com.velouracove.common.ApiException;
import com.velouracove.offer.domain.Offer;
import com.velouracove.offer.persistence.OfferRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfferService {
    private final OfferRepository repository;

    public OfferService(OfferRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<OfferDto> list() {
        return repository.findByActiveTrueOrderByNameAsc().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public OfferDto get(String slug) {
        return repository.findBySlugAndActiveTrue(slug).map(this::map)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "OFFER_NOT_FOUND", "The requested offer was not found."));
    }

    private OfferDto map(Offer offer) {
        return new OfferDto(offer.getSlug(), offer.getName(), offer.getDescription(), offer.getHeroImage(),
                offer.getDiscountType().name(), offer.getDiscountValue(), offer.getMinimumNights(),
                offer.getValidFrom(), offer.getValidUntil(), offer.getTerms(),
                offer.getApplicableAccommodationTypes().stream().map(type -> type.getSlug()).sorted().toList());
    }
}
