package com.velouracove.offer.domain;

import com.velouracove.accommodation.domain.AccommodationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "offers")
@Getter @Setter @NoArgsConstructor
public class Offer {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 100)
    private String slug;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(nullable = false, columnDefinition = "text")
    private String description;
    @Column(name = "hero_image", nullable = false, length = 1000)
    private String heroImage;
    @Enumerated(EnumType.STRING) @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;
    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;
    @Column(name = "minimum_nights", nullable = false)
    private int minimumNights;
    @Column(name = "valid_from")
    private LocalDate validFrom;
    @Column(name = "valid_until")
    private LocalDate validUntil;
    @Column(nullable = false)
    private boolean active;
    @Column(nullable = false, columnDefinition = "text")
    private String terms;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "offer_accommodation_types", joinColumns = @JoinColumn(name = "offer_id"), inverseJoinColumns = @JoinColumn(name = "accommodation_type_id"))
    private Set<AccommodationType> applicableAccommodationTypes = new LinkedHashSet<>();
    public enum DiscountType { PERCENTAGE, FIXED, PACKAGE }
}
