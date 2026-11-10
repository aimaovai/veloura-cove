package com.velouracove.experience.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "experience_images")
@Getter @Setter @NoArgsConstructor
public class ExperienceImage {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "experience_id", nullable = false)
    private Experience experience;
    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;
    @Column(name = "alt_text", nullable = false, length = 250)
    private String altText;
    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
