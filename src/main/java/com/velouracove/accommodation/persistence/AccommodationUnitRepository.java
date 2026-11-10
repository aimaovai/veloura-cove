package com.velouracove.accommodation.persistence;

import com.velouracove.accommodation.domain.AccommodationUnit;
import com.velouracove.accommodation.domain.AccommodationUnit.MaintenanceStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AccommodationUnitRepository extends JpaRepository<AccommodationUnit, UUID> {
    @Query("select u from AccommodationUnit u where u.accommodationType.id = :typeId and u.active = true and u.maintenanceStatus = :status order by u.unitCode")
    List<AccommodationUnit> findEligible(@Param("typeId") UUID typeId, @Param("status") MaintenanceStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from AccommodationUnit u where u.accommodationType.id = :typeId and u.active = true and u.maintenanceStatus = :status order by u.unitCode")
    List<AccommodationUnit> lockEligible(@Param("typeId") UUID typeId, @Param("status") MaintenanceStatus status);
}
