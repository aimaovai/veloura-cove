package com.velouracove.inquiry.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "inquiries")
@Getter @Setter @NoArgsConstructor
public class Inquiry {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(nullable = false, length = 254)
    private String email;
    @Column(length = 50)
    private String phone;
    @Column(name = "event_date")
    private LocalDate eventDate;
    @Column(name = "party_size")
    private Integer partySize;
    @Enumerated(EnumType.STRING) @Column(name = "inquiry_type", nullable = false, length = 30)
    private InquiryType inquiryType;
    @Column(nullable = false, length = 3000)
    private String message;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    public enum InquiryType { WEDDING, PRIVATE_EVENT, CORPORATE_RETREAT, GENERAL, TRANSPORTATION }
}
