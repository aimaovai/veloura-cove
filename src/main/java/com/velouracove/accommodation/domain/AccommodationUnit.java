package com.velouracove.accommodation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "accommodation_units")
@Getter @Setter @NoArgsConstructor
public class AccommodationUnit {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accommodation_type_id", nullable = false)
    private AccommodationType accommodationType;
    @Column(name = "unit_code", nullable = false, unique = true, length = 30)
    private String unitCode;
    @Column(name = "floor_location", nullable = false, length = 100)
    private String floorLocation;
    @Column(nullable = false)
    private boolean active;
    @Enumerated(EnumType.STRING) @Column(name = "maintenance_status", nullable = false, length = 20)
    private MaintenanceStatus maintenanceStatus;

    public enum MaintenanceStatus { READY, MAINTENANCE }
}
