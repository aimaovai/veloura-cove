package com.velouracove.inquiry;

import com.velouracove.api.ApiDtos.InquiryCreatedDto;
import com.velouracove.api.ApiDtos.InquiryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inquiries")
public class InquiryController {
    private final InquiryService service;

    public InquiryController(InquiryService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Submit a resort inquiry", description = "Stores a bounded inquiry only; an inquiry does not reserve accommodation or event inventory.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples =
                    @ExampleObject(value = "{\"name\":\"Ari Guest\",\"email\":\"ari@example.test\",\"phone\":\"+1-555-0100\",\"eventDate\":\"2027-08-14\",\"partySize\":24,\"inquiryType\":\"PRIVATE_EVENT\",\"message\":\"We are exploring a private celebration at the cove.\"}"))),
            responses = @ApiResponse(responseCode = "200", description = "Inquiry accepted",
                    content = @Content(examples = @ExampleObject(value = "{\"inquiryId\":\"b1a3eec0-5884-4dd0-9c2f-4e16cd14c113\",\"status\":\"RECEIVED\",\"createdAt\":\"2027-02-01T10:00:00Z\"}"))))
    public InquiryCreatedDto submit(@Valid @RequestBody InquiryRequest request) {
        return service.submit(request);
    }
}
