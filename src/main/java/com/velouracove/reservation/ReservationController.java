package com.velouracove.reservation;

import com.velouracove.api.ApiDtos.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
public class ReservationController {
    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping("/reservation-holds")
    @Operation(summary = "Place a temporary inventory hold",
            description = "Creates a configured short-lived hold after rechecking inventory under database locks.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples =
                    @ExampleObject(value = "{\"accommodationTypeId\":\"10000000-0000-0000-0000-000000000003\",\"checkIn\":\"2027-02-10\",\"checkOut\":\"2027-02-13\",\"adults\":2,\"children\":0}"))),
            responses = @ApiResponse(responseCode = "200", description = "Inventory held and itemized estimate returned",
                    content = @Content(examples = @ExampleObject(value = "{\"holdId\":\"b1a3eec0-5884-4dd0-9c2f-4e16cd14c113\",\"expiresAt\":\"2027-02-01T10:15:00Z\",\"checkIn\":\"2027-02-10\",\"checkOut\":\"2027-02-13\",\"adults\":2,\"children\":0,\"price\":{\"nightlyCharges\":[{\"date\":\"2027-02-10\",\"nightlyRate\":890.00}],\"roomSubtotal\":2670.00,\"resortFee\":105.00,\"taxes\":346.88,\"total\":3121.88,\"currency\":\"USD\"}}"))))
    public HoldDto createHold(@Valid @RequestBody HoldRequest request) {
        return service.createHold(request);
    }

    @PostMapping("/reservations")
    @Operation(summary = "Attach guest details to a hold",
            description = "Requires an Idempotency-Key. Returns a cryptographically random private management token once; store it securely.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples =
                    @ExampleObject(value = "{\"holdId\":\"b1a3eec0-5884-4dd0-9c2f-4e16cd14c113\",\"firstName\":\"Ari\",\"lastName\":\"Guest\",\"email\":\"ari@example.test\",\"phone\":\"+1-555-0100\",\"country\":\"US\",\"arrivalNotes\":\"Arriving after 6 pm\",\"specialRequests\":\"Feather-free pillows\",\"celebrationType\":\"ANNIVERSARY\"}"))),
            responses = @ApiResponse(responseCode = "200", description = "Reservation guest details stored",
                    content = @Content(examples = @ExampleObject(value = "{\"reservationId\":\"b1a3eec0-5884-4dd0-9c2f-4e16cd14c113\",\"confirmationNumber\":null,\"managementToken\":\"random-private-token-returned-once\",\"status\":\"HELD\",\"accommodationName\":\"Horizon Suite\",\"checkIn\":\"2027-02-10\",\"checkOut\":\"2027-02-13\",\"adults\":2,\"children\":0,\"price\":{\"roomSubtotal\":2670.00,\"resortFee\":105.00,\"taxes\":346.88,\"total\":3121.88,\"currency\":\"USD\"}}"))))
    public CreatedReservationDto createReservation(@RequestHeader("Idempotency-Key") @Size(min = 8, max = 120) String idempotencyKey,
                                                    @Valid @RequestBody ReservationRequest request) {
        return service.createReservation(request.holdId(), idempotencyKey, request);
    }

    @PostMapping("/reservations/{id}/payment-simulation")
    @Operation(summary = "Simulate payment", description = "Demo-only SUCCESS/DECLINED outcome. This endpoint never contacts a payment provider.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples =
                    @ExampleObject(value = "{\"result\":\"SUCCESS\"}"))),
            responses = @ApiResponse(responseCode = "200", description = "Simulated result and reservation state",
                    content = @Content(examples = @ExampleObject(value = "{\"reservationId\":\"b1a3eec0-5884-4dd0-9c2f-4e16cd14c113\",\"status\":\"CONFIRMED\",\"outcome\":\"SUCCESS\",\"paymentReference\":\"SIM-74C2845F0E12\",\"confirmationNumber\":\"VC-7K29M4\",\"message\":\"Payment simulation only: no payment provider or real financial transaction was used.\"}"))))
    public PaymentSimulationDto simulatePayment(@PathVariable UUID id,
                                                 @RequestHeader("Idempotency-Key") @Size(min = 8, max = 120) String idempotencyKey,
                                                 @Valid @RequestBody PaymentSimulationRequest request) {
        return service.simulatePayment(id, idempotencyKey, request);
    }

    @GetMapping("/reservations/manage/{token}")
    public ManagedReservationDto getManaged(@PathVariable String token) {
        return service.findManaged(token);
    }

    @PostMapping("/reservations/manage/{token}/cancel")
    @Operation(summary = "Cancel a reservation", description = "Any refund or cancellation penalty in the response is simulated only.")
    public CancellationDto cancel(@PathVariable String token) {
        return service.cancel(token);
    }
}
