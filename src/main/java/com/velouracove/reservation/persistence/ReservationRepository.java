package com.velouracove.reservation.persistence;

import com.velouracove.reservation.domain.Reservation;
import com.velouracove.reservation.domain.Reservation.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    @Query("select r from Reservation r where r.accommodationUnit.id in :unitIds and r.checkIn < :checkOut and r.checkOut > :checkIn and (r.status in :blockingStatuses or (r.status = :heldStatus and r.expiresAt > :now))")
    List<Reservation> findBlockingOverlaps(@Param("unitIds") Collection<UUID> unitIds,
                                           @Param("checkIn") LocalDate checkIn,
                                           @Param("checkOut") LocalDate checkOut,
                                           @Param("blockingStatuses") Collection<ReservationStatus> blockingStatuses,
                                           @Param("heldStatus") ReservationStatus heldStatus,
                                           @Param("now") OffsetDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"guest", "accommodationUnit", "accommodationUnit.accommodationType"})
    Optional<Reservation> findByManagementTokenHash(String tokenHash);

    @EntityGraph(attributePaths = {"guest", "accommodationUnit", "accommodationUnit.accommodationType"})
    Optional<Reservation> findByReservationCreationKey(String key);
}
