package com.velouracove.guest.persistence;

import com.velouracove.guest.domain.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GuestRepository extends JpaRepository<Guest, UUID> {}
