package com.velouracove.experience.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "experiences")
@Getter @Setter @NoArgsConstructor
public class Experience {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 100)
    private String slug;
    @Column(nullable = false, length = 150)
    private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private ExperienceCategory category;
    @Column(name = "short_description", nullable = false, length = 500)
    private String shortDescription;
    @Column(name = "long_description", nullable = false, columnDefinition = "text")
    private String longDescription;
    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;
    @Column(name = "starting_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal startingPrice;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "hero_image", nullable = false, length = 1000)
    private String heroImage;
    @Column(nullable = false)
    private boolean featured;
    @Column(nullable = false)
    private boolean active;
    @OneToMany(mappedBy = "experience", fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<ExperienceImage> images = new ArrayList<>();
    public enum ExperienceCategory { WELLNESS, DINING, ADVENTURE, ROMANCE, WATER, CULTURE }
}
