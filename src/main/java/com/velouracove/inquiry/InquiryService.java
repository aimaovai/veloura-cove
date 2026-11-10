package com.velouracove.inquiry;

import com.velouracove.api.ApiDtos.InquiryCreatedDto;
import com.velouracove.api.ApiDtos.InquiryRequest;
import com.velouracove.inquiry.domain.Inquiry;
import com.velouracove.inquiry.persistence.InquiryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class InquiryService {
    private final InquiryRepository repository;

    public InquiryService(InquiryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public InquiryCreatedDto submit(InquiryRequest request) {
        Inquiry inquiry = new Inquiry();
        inquiry.setName(request.name().trim());
        inquiry.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        inquiry.setPhone(request.phone() == null || request.phone().isBlank() ? null : request.phone().trim());
        inquiry.setEventDate(request.eventDate());
        inquiry.setPartySize(request.partySize());
        inquiry.setInquiryType(request.inquiryType());
        inquiry.setMessage(request.message().trim());
        Inquiry saved = repository.saveAndFlush(inquiry);
        return new InquiryCreatedDto(saved.getId(), "RECEIVED", saved.getCreatedAt());
    }
}
