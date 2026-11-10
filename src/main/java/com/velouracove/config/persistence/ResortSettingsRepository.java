package com.velouracove.config.persistence;

import com.velouracove.config.domain.ResortSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResortSettingsRepository extends JpaRepository<ResortSettings, Short> {}
