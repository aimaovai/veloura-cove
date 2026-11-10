package com.velouracove.pricing.persistence;

import com.velouracove.pricing.domain.SeasonalRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SeasonalRateRepository extends JpaRepository<SeasonalRate, UUID> {
    @Query("select s from SeasonalRate s where (s.accommodationType is null or s.accommodationType.id = :typeId) and s.startDate <= :night and s.endDate > :night order by s.priority desc, case when s.accommodationType is null then 1 else 0 end")
    List<SeasonalRate> findApplicable(@Param("typeId") UUID typeId, @Param("night") LocalDate night);
}
