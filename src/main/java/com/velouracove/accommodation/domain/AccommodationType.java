package com.velouracove.accommodation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "accommodation_types")
@Getter
@Setter
@NoArgsConstructor
public class AccommodationType {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 100)
    private String slug;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(name = "short_description", nullable = false, length = 500)
    private String shortDescription;
    @Column(name = "long_description", nullable = false, columnDefinition = "text")
    private String longDescription;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private AccommodationCategory category;
    @Column(name = "max_guests", nullable = false)
    private int maxGuests;
    @Column(name = "max_adults", nullable = false)
    private int maxAdults;
    @Column(name = "max_children", nullable = false)
    private int maxChildren;
    @Column(name = "bed_configuration", nullable = false, length = 150)
    private String bedConfiguration;
    @Column(name = "room_size_sq_ft", nullable = false)
    private int roomSizeSqFt;
    @Column(name = "base_nightly_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseNightlyRate;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "view_type", nullable = false, length = 100)
    private String viewType;
    @Column(nullable = false)
    private boolean featured;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "check_in_time", nullable = false)
    private LocalTime checkInTime;
    @Column(name = "check_out_time", nullable = false)
    private LocalTime checkOutTime;
    @Column(name = "hero_image", nullable = false, length = 1000)
    private String heroImage;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "accommodation_amenities", joinColumns = @JoinColumn(name = "accommodation_type_id"), inverseJoinColumns = @JoinColumn(name = "amenity_id"))
    private Set<Amenity> amenities = new LinkedHashSet<>();
    @ElementCollection
    @CollectionTable(name = "accommodation_features", joinColumns = @JoinColumn(name = "accommodation_type_id"))
    @OrderColumn(name = "display_order")
    @Column(name = "feature", nullable = false, length = 180)
    private List<String> features = new ArrayList<>();
    @OneToMany(mappedBy = "accommodationType", fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<AccommodationImage> images = new ArrayList<>();
}
