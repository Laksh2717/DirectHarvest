package com.directharvest.backend.listings.controller;

import com.directharvest.backend.common.enums.ListingStatus;
import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.listings.request.AddListingQuantityRequest;
import com.directharvest.backend.listings.request.AddListingImagesRequest;
import com.directharvest.backend.listings.request.BrowseListingsSortBy;
import com.directharvest.backend.listings.request.CreateListingRequest;
import com.directharvest.backend.listings.request.UpdateListingPriceRequest;
import com.directharvest.backend.listings.request.UpdateListingRequest;
import com.directharvest.backend.listings.response.BrowseListingsResponse;
import com.directharvest.backend.listings.response.ListingResponse;
import com.directharvest.backend.listings.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/listings")
@Tag(name = "Listings", description = "Listing management endpoints. Auth uses cookie-based JWT (access_token).")
@SecurityRequirement(name = "cookieAuth")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping
    @Operation(summary = "Create listing", description = "Creates a listing for authenticated FARMER using crop, quantity, price, address, optional description and images.")
    public ResponseEntity<ListingResponse> create(@Valid @RequestBody CreateListingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listingService.create(request));
    }

    @GetMapping("/{listingId}")
    @Operation(summary = "Get listing by ID", description = "Retrieves listing details with address and images for authenticated BUYER/FARMER users.")
    public ResponseEntity<ListingResponse> getById(@PathVariable Long listingId) {
        return ResponseEntity.ok(listingService.getById(listingId));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my listings", description = "Returns listings created by the authenticated FARMER. If status is omitted, all statuses are returned.")
    public ResponseEntity<List<ListingResponse>> getMyListings(
            @Parameter(
                    description = "Optional repeatable status filter. Example: ?status=ACTIVE&status=INACTIVE",
                    schema = @Schema(implementation = ListingStatus.class)
            )
            @RequestParam(name = "status", required = false) List<ListingStatus> status
    ) {
        return ResponseEntity.ok(listingService.getMyListings(status));
    }

    @GetMapping("/browse")
    @Operation(summary = "Browse listings", description = "Public endpoint. Returns paginated listings (fixed size 12) with optional crop-name search and sorting.")
    public ResponseEntity<BrowseListingsResponse> browseListings(
            @Parameter(description = "Optional case-insensitive keyword for crop name")
            @RequestParam(name = "search", required = false) String search,
            @Parameter(description = "Zero-based page index")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Sort field: LISTING_DATE, PRICE, FARMER_RATING")
            @RequestParam(name = "sortBy", defaultValue = "LISTING_DATE") BrowseListingsSortBy sortBy,
            @Parameter(description = "Sort direction: ASC or DESC")
            @RequestParam(name = "sortDir", defaultValue = "DESC") Sort.Direction sortDir
    ) {
        return ResponseEntity.ok(listingService.browseListings(search, page, sortBy, sortDir));
    }

    @PatchMapping("/{listingId}")
    @Operation(summary = "Update listing details", description = "Updates only crop name, description, and address fields. Only owner can update.")
    public ResponseEntity<ListingResponse> update(
            @PathVariable Long listingId,
            @Valid @RequestBody UpdateListingRequest request
    ) {
        return ResponseEntity.ok(listingService.update(listingId, request));
    }

    @PatchMapping("/{listingId}/price")
    @Operation(summary = "Update listing price", description = "Owner-only price-per-kg update for ACTIVE listings with active-negotiation guard.")
    public ResponseEntity<ListingResponse> updatePrice(
            @PathVariable Long listingId,
            @Valid @RequestBody UpdateListingPriceRequest request
    ) {
        return ResponseEntity.ok(listingService.updatePrice(listingId, request));
    }

    @PostMapping("/{listingId}/quantity/add")
    @Operation(summary = "Add quantity", description = "Increases owner listing quantity only when listing status is ACTIVE.")
    public ResponseEntity<ListingResponse> addQuantity(
            @PathVariable Long listingId,
            @Valid @RequestBody AddListingQuantityRequest request
    ) {
        return ResponseEntity.ok(listingService.addQuantity(listingId, request));
    }

    @PostMapping("/{listingId}/images")
    @Operation(summary = "Add listing images", description = "Owner-only endpoint to append Cloudinary images to an ACTIVE listing.")
    public ResponseEntity<ListingResponse> addImages(
            @PathVariable Long listingId,
            @Valid @RequestBody AddListingImagesRequest request
    ) {
        return ResponseEntity.ok(listingService.addImages(listingId, request));
    }

    @DeleteMapping("/{listingId}/images/{imageId}")
    @Operation(summary = "Remove listing image", description = "Owner-only endpoint to remove a specific listing image and enqueue Cloudinary cleanup.")
    public ResponseEntity<ListingResponse> removeImage(
            @PathVariable Long listingId,
            @PathVariable Long imageId
    ) {
        return ResponseEntity.ok(listingService.removeImage(listingId, imageId));
    }

    @PatchMapping("/{listingId}/inactive")
    @Operation(summary = "Mark listing inactive", description = "Owner-only endpoint to set listing status to INACTIVE.")
    public ResponseEntity<ListingResponse> markInactive(@PathVariable Long listingId) {
        return ResponseEntity.ok(listingService.markInactive(listingId));
    }

    @DeleteMapping("/{listingId}")
    @Operation(summary = "Delete listing", description = "Deletes listing and queues stale Cloudinary image cleanup.")
    public ResponseEntity<Void> delete(@PathVariable Long listingId) {
        listingService.delete(listingId);
        return ResponseEntity.noContent().build();
    }
}
