package com.velouracove.accommodation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "amenities")
@Getter @Setter @NoArgsConstructor
public class Amenity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    @Column(nullable = false, unique = true, length = 120)
    private String slug;
}
