package com.velouracove.accommodation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "accommodation_images")
@Getter @Setter @NoArgsConstructor
public class AccommodationImage {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accommodation_type_id", nullable = false)
    private AccommodationType accommodationType;
    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;
    @Column(name = "alt_text", nullable = false, length = 250)
    private String altText;
    @Column(name = "image_type", nullable = false, length = 30)
    private String imageType;
    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
