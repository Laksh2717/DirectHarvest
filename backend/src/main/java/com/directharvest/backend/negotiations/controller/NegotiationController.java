package com.directharvest.backend.negotiations.controller;

import com.directharvest.backend.common.enums.NegotiationStatus;
import com.directharvest.backend.negotiations.request.CounterOfferRequest;
import com.directharvest.backend.negotiations.request.CreateNegotiationRequest;
import com.directharvest.backend.negotiations.request.RejectNegotiationRequest;
import com.directharvest.backend.negotiations.response.NegotiationEventResponse;
import com.directharvest.backend.negotiations.response.NegotiationResponse;
import com.directharvest.backend.negotiations.service.NegotiationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/negotiations")
@Tag(name = "Negotiations", description = "Negotiation endpoints for buyers and farmers. Auth uses cookie-based JWT (access_token).")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "cookieAuth")
public class NegotiationController {

    private final NegotiationService negotiationService;

    public NegotiationController(NegotiationService negotiationService) {
        this.negotiationService = negotiationService;
    }

    @PostMapping
    @Operation(summary = "Create negotiation", description = "Creates a new negotiation for a listing by the authenticated user.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Negotiation creation payload",
            content = @Content(schema = @Schema(implementation = com.directharvest.backend.negotiations.request.CreateNegotiationRequest.class))
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Negotiation created successfully", content = @Content(schema = @Schema(implementation = com.directharvest.backend.negotiations.response.NegotiationResponse.class)))
        }
    )
    public ResponseEntity<NegotiationResponse> create(@Valid @RequestBody CreateNegotiationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(negotiationService.create(request));
    }

    @PostMapping("/{negotiationId}/counter")
    @Operation(summary = "Counter offer", description = "Submit a counter offer for an existing negotiation.",
        parameters = {
            @Parameter(name = "negotiationId", description = "ID of the negotiation to counter", required = true)
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Counter offer payload",
            content = @Content(schema = @Schema(implementation = com.directharvest.backend.negotiations.request.CounterOfferRequest.class))
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Counter offer submitted", content = @Content(schema = @Schema(implementation = com.directharvest.backend.negotiations.response.NegotiationResponse.class)))
        }
    )
    public ResponseEntity<NegotiationResponse> counterOffer(
            @PathVariable Long negotiationId,
            @Valid @RequestBody CounterOfferRequest request
    ) {
        return ResponseEntity.ok(negotiationService.counterOffer(negotiationId, request));
    }

    @PostMapping("/{negotiationId}/accept")
    @Operation(summary = "Accept negotiation", description = "Accept an existing negotiation.",
        parameters = {
            @Parameter(name = "negotiationId", description = "ID of the negotiation to accept", required = true)
        },
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Negotiation accepted", content = @Content(schema = @Schema(implementation = com.directharvest.backend.negotiations.response.NegotiationResponse.class)))
        }
    )
    public ResponseEntity<NegotiationResponse> accept(@PathVariable Long negotiationId) {
        return ResponseEntity.ok(negotiationService.accept(negotiationId));
    }

    @PostMapping("/{negotiationId}/reject")
    @Operation(summary = "Reject negotiation", description = "Reject an existing negotiation. Optionally provide a reason.",
        parameters = {
            @Parameter(name = "negotiationId", description = "ID of the negotiation to reject", required = true)
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = false,
            description = "Optional rejection reason",
            content = @Content(schema = @Schema(implementation = com.directharvest.backend.negotiations.request.RejectNegotiationRequest.class))
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Negotiation rejected", content = @Content(schema = @Schema(implementation = com.directharvest.backend.negotiations.response.NegotiationResponse.class)))
        }
    )
    public ResponseEntity<NegotiationResponse> reject(
            @PathVariable Long negotiationId,
            @Valid @RequestBody(required = false) RejectNegotiationRequest request
    ) {
        return ResponseEntity.ok(negotiationService.reject(negotiationId, request));
    }

    @GetMapping("/{negotiationId}")
    @Operation(summary = "Get negotiation by ID", description = "Retrieve a negotiation by its ID.",
        parameters = {
            @Parameter(name = "negotiationId", description = "ID of the negotiation", required = true)
        },
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Negotiation details", content = @Content(schema = @Schema(implementation = com.directharvest.backend.negotiations.response.NegotiationResponse.class)))
        }
    )
    public ResponseEntity<NegotiationResponse> getById(@PathVariable Long negotiationId) {
        return ResponseEntity.ok(negotiationService.getById(negotiationId));
    }

    @GetMapping("/{negotiationId}/history")
    @Operation(summary = "Get negotiation history", description = "Retrieve the event history for a negotiation.",
        parameters = {
            @Parameter(name = "negotiationId", description = "ID of the negotiation", required = true)
        },
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Negotiation event history", content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = com.directharvest.backend.negotiations.response.NegotiationEventResponse.class))))
        }
    )
    public ResponseEntity<List<NegotiationEventResponse>> getHistory(@PathVariable Long negotiationId) {
        return ResponseEntity.ok(negotiationService.getHistory(negotiationId));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my negotiations", description = "Returns all participant negotiations when status is omitted. If one or more status values are provided, returns only matching negotiations.",
        parameters = {
            @Parameter(name = "status", description = "Optional repeatable negotiation-status filter. Example: ?status=PENDING_FARMER&status=ACCEPTED", required = false, schema = @Schema(implementation = com.directharvest.backend.common.enums.NegotiationStatus.class))
        },
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of negotiations", content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = com.directharvest.backend.negotiations.response.NegotiationResponse.class))))
        }
    )
    public ResponseEntity<List<NegotiationResponse>> getMyNegotiations(
            @RequestParam(required = false) List<NegotiationStatus> status
    ) {
        return ResponseEntity.ok(negotiationService.getMyNegotiations(status));
    }
}

