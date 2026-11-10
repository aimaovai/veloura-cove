package com.velouracove.inquiry.persistence;

import com.velouracove.inquiry.domain.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InquiryRepository extends JpaRepository<Inquiry, UUID> {}
