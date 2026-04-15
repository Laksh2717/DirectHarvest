package com.directharvest.backend.listings.service;

import com.directharvest.backend.common.enums.ListingStatus;
import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.common.exception.BadRequestException;
import com.directharvest.backend.common.exception.ForbiddenException;
import com.directharvest.backend.common.exception.ResourceNotFoundException;
import com.directharvest.backend.common.exception.UnauthorizedException;
import com.directharvest.backend.listings.entity.Listing;
import com.directharvest.backend.listings.entity.ListingImage;
import com.directharvest.backend.listings.event.ListingImagesCleanupRequestedEvent;
import com.directharvest.backend.listings.repository.ListingRepository;
import com.directharvest.backend.listings.request.AddListingQuantityRequest;
import com.directharvest.backend.listings.request.AddListingImagesRequest;
import com.directharvest.backend.listings.request.BrowseListingsSortBy;
import com.directharvest.backend.listings.request.CreateListingRequest;
import com.directharvest.backend.listings.request.ListingImageRequest;
import com.directharvest.backend.listings.request.UpdateListingPriceRequest;
import com.directharvest.backend.listings.request.UpdateListingRequest;
import com.directharvest.backend.listings.response.BrowseListingsResponse;
import com.directharvest.backend.listings.response.ListingImageResponse;
import com.directharvest.backend.listings.response.ListingResponse;
import com.directharvest.backend.common.enums.NegotiationStatus;
import com.directharvest.backend.negotiations.repository.NegotiationRepository;
import com.directharvest.backend.users.entity.User;
import com.directharvest.backend.users.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.List;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ListingService {

    private static final int MAX_LISTING_IMAGES = 5;
    private static final int BROWSE_PAGE_SIZE = 12;

    private static final EnumSet<NegotiationStatus> ACTIVE_NEGOTIATION_STATUSES =
            EnumSet.of(NegotiationStatus.PENDING_BUYER, NegotiationStatus.PENDING_FARMER);

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final NegotiationRepository negotiationRepository;

    public ListingService(
            ListingRepository listingRepository,
            UserRepository userRepository,
            ApplicationEventPublisher applicationEventPublisher,
            NegotiationRepository negotiationRepository
    ) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.negotiationRepository = negotiationRepository;
    }

    @Transactional
    public ListingResponse create(CreateListingRequest request) {
        User currentUser = getCurrentUser();
        ensureFarmerRole(currentUser);
        ensureCreateImageLimit(request.images());

        Listing listing = new Listing();
        listing.setFarmer(currentUser);
        applyCreatePayload(listing, request);

        return toResponse(listingRepository.save(listing));
    }

    @Transactional(readOnly = true)
    public ListingResponse getById(Long listingId) {
        Listing listing = listingRepository.findWithImagesById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        return toResponse(listing);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getMyListings(List<ListingStatus> statuses) {
        User currentUser = getCurrentUser();
        ensureFarmerRole(currentUser);

        List<Listing> listings;
        if (statuses == null || statuses.isEmpty()) {
            listings = listingRepository.findAllByFarmerIdOrderByCreatedAtDesc(currentUser.getId());
        } else if (statuses.size() == 1) {
            listings = listingRepository.findAllByFarmerIdAndStatusOrderByCreatedAtDesc(currentUser.getId(), statuses.getFirst());
        } else {
            listings = listingRepository.findAllByFarmerIdAndStatusInOrderByCreatedAtDesc(currentUser.getId(), statuses);
        }

        return listings.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BrowseListingsResponse browseListings(String search, int page, BrowseListingsSortBy sortBy, Sort.Direction sortDirection) {
        int normalizedPage = Math.max(page, 0);
        BrowseListingsSortBy normalizedSortBy = sortBy == null ? BrowseListingsSortBy.LISTING_DATE : sortBy;
        Sort.Direction normalizedDirection = sortDirection == null ? Sort.Direction.DESC : sortDirection;

        Pageable pageable = PageRequest.of(normalizedPage, BROWSE_PAGE_SIZE, resolveBrowseSort(normalizedSortBy, normalizedDirection));

        Page<Listing> listingsPage;
        if (search == null || search.isBlank()) {
            listingsPage = listingRepository.findAllForBrowse(pageable);
        } else {
            String keyword = search.trim();
            listingsPage = listingRepository.findByStatusAndCropNameContainingIgnoreCase(ListingStatus.ACTIVE, keyword, pageable);
        }

        List<ListingResponse> content = listingsPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new BrowseListingsResponse(
                content,
                listingsPage.getNumber(),
                listingsPage.getSize(),
                listingsPage.getTotalElements(),
                listingsPage.getTotalPages(),
                listingsPage.isFirst(),
                listingsPage.isLast()
        );
    }

    private Sort resolveBrowseSort(BrowseListingsSortBy sortBy, Sort.Direction sortDirection) {
        BrowseListingsSortBy normalizedSortBy = sortBy == null ? BrowseListingsSortBy.LISTING_DATE : sortBy;
        Sort.Direction normalizedDirection = sortDirection == null ? Sort.Direction.DESC : sortDirection;

        Sort.Order primaryOrder = switch (normalizedSortBy) {
            case PRICE -> new Sort.Order(normalizedDirection, "pricePerKg");
            case FARMER_RATING -> new Sort.Order(normalizedDirection, "farmer.averageRating").nullsLast();
            case LISTING_DATE -> new Sort.Order(normalizedDirection, "createdAt");
        };

        if (normalizedSortBy == BrowseListingsSortBy.LISTING_DATE) {
            return Sort.by(primaryOrder, new Sort.Order(Sort.Direction.DESC, "id"));
        }
        return Sort.by(primaryOrder, new Sort.Order(Sort.Direction.DESC, "createdAt"), new Sort.Order(Sort.Direction.DESC, "id"));
    }

    @Transactional
    public ListingResponse update(Long listingId, UpdateListingRequest request) {
        User currentUser = getCurrentUser();
        Listing listing = getOwnedListingOrThrow(listingId, currentUser);
        ensureListingIsActive(listing, "Listing details can only be updated when listing is ACTIVE");
        ensureNoActiveNegotiations(listing);
        applyUpdatePayload(listing, request);
        return toResponse(listingRepository.save(listing));
    }

    @Transactional
    public ListingResponse updatePrice(Long listingId, UpdateListingPriceRequest request) {
        User currentUser = getCurrentUser();
        Listing listing = getOwnedListingOrThrow(listingId, currentUser);

        ensureListingIsActive(listing, "Listing price can only be updated when listing is ACTIVE");
        ensureNoActiveNegotiations(listing);
        validatePriceUpdateAllowed(currentUser);
        listing.setPricePerKg(request.pricePerKg());

        return toResponse(listingRepository.save(listing));
    }


    @Transactional
    public ListingResponse addQuantity(Long listingId, AddListingQuantityRequest request) {
        User currentUser = getCurrentUser();
        ensureFarmerRole(currentUser);

        Listing listing = listingRepository.findWithImagesById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (!listing.getFarmer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only update your own listing");
        }


        // Allow add quantity if listing is ACTIVE or OUT_OF_STOCK
        if (listing.getStatus() != ListingStatus.ACTIVE && listing.getStatus() != ListingStatus.OUT_OF_STOCK) {
            throw new BadRequestException("Quantity can only be added when listing is ACTIVE or OUT_OF_STOCK");
        }

        BigDecimal newQuantity = listing.getQuantity().add(request.quantity());
        listing.setQuantity(newQuantity);

        // If previously OUT_OF_STOCK and now quantity > 0, set status to ACTIVE
        if (listing.getStatus() == ListingStatus.OUT_OF_STOCK && newQuantity.compareTo(BigDecimal.ZERO) > 0) {
            listing.setStatus(ListingStatus.ACTIVE);
        }

        return toResponse(listingRepository.save(listing));
    }

    @Transactional
    public ListingResponse addImages(Long listingId, AddListingImagesRequest request) {
        User currentUser = getCurrentUser();
        ensureFarmerRole(currentUser);

        Listing listing = getOwnedListingOrThrow(listingId, currentUser);
        ensureListingIsActive(listing, "Listing images can only be modified when listing is ACTIVE");
        ensureNoActiveNegotiations(listing);

        List<ListingImage> newImages = toImages(request.images());
        ensureTotalImageLimit(listing, newImages);
        ensureNoDuplicatePublicIds(listing, newImages);
        applyPrimaryImagePolicy(listing, newImages);
        newImages.forEach(listing::addImage);

        return toResponse(listingRepository.save(listing));
    }

    @Transactional
    public ListingResponse removeImage(Long listingId, Long imageId) {
        User currentUser = getCurrentUser();
        ensureFarmerRole(currentUser);

        Listing listing = getOwnedListingOrThrow(listingId, currentUser);
        ensureListingIsActive(listing, "Listing images can only be modified when listing is ACTIVE");
        ensureNoActiveNegotiations(listing);

        ListingImage imageToRemove = listing.getImages().stream()
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Listing image not found"));

        String publicId = imageToRemove.getCloudinaryPublicId();
        boolean wasPrimary = imageToRemove.isPrimary();

        listing.removeImage(imageToRemove);
        if (wasPrimary && !listing.getImages().isEmpty()) {
            listing.getImages().get(0).setPrimary(true);
        }

        Listing saved = listingRepository.save(listing);
        publishCleanupEvent(hasText(publicId) ? Set.of(publicId.trim()) : Set.of());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long listingId) {
        User currentUser = getCurrentUser();

        Listing listing = listingRepository.findWithImagesById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (!listing.getFarmer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only delete your own listing");
        }


        // Only allow delete if listing is ACTIVE
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new BadRequestException("Listing can only be deleted if it is ACTIVE");
        }

        // Only allow delete if there are no negotiations for this listing
        if (negotiationRepository.existsByListingIdAndStatusIn(listingId, EnumSet.allOf(com.directharvest.backend.common.enums.NegotiationStatus.class))) {
            throw new BadRequestException("Listing cannot be deleted because there are negotiations/offers for it");
        }

        Set<String> publicIds = extractPublicIds(listing);

        listingRepository.delete(listing);
        publishCleanupEvent(publicIds);
    }

    @Transactional
    public ListingResponse markInactive(Long listingId) {
        User currentUser = getCurrentUser();
        Listing listing = getOwnedListingOrThrow(listingId, currentUser);
        ensureNoActiveNegotiations(listing);
        listing.setStatus(ListingStatus.INACTIVE);
        return toResponse(listingRepository.save(listing));
    }

    private Set<String> extractPublicIds(Listing listing) {
        return listing.getImages().stream()
                .map(ListingImage::getCloudinaryPublicId)
                .filter(this::hasText)
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    private void publishCleanupEvent(Set<String> publicIds) {
        if (!publicIds.isEmpty()) {
            applicationEventPublisher.publishEvent(new ListingImagesCleanupRequestedEvent(publicIds));
        }
    }

    private void applyCreatePayload(Listing listing, CreateListingRequest request) {
        listing.setCropName(request.cropName().trim());
        listing.setQuantity(request.quantity());
        listing.setInitialQuantity(request.quantity());
        listing.setPricePerKg(request.pricePerKg());
        listing.setDescription(trimDescription(request.description()));
        listing.setStreet(request.street().trim());
        listing.setCity(request.city().trim());
        listing.setState(request.state().trim());
        listing.setPincode(request.pincode().trim());
        listing.setStatus(ListingStatus.ACTIVE);
        listing.replaceImages(toImages(request.images()));
    }

    private void applyUpdatePayload(Listing listing, UpdateListingRequest request) {
        listing.setCropName(request.cropName().trim());
        listing.setDescription(trimDescription(request.description()));
        listing.setStreet(request.street().trim());
        listing.setCity(request.city().trim());
        listing.setState(request.state().trim());
        listing.setPincode(request.pincode().trim());
    }

    private List<ListingImage> toImages(List<ListingImageRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        long primaryCount = requests.stream().filter(image -> Boolean.TRUE.equals(image.primary())).count();
        if (primaryCount > 1) {
            throw new BadRequestException("Only one image can be marked as primary");
        }

        return requests.stream().map(this::toImage).toList();
    }

    private void ensureNoDuplicatePublicIds(Listing listing, List<ListingImage> newImages) {
        Set<String> existingPublicIds = listing.getImages().stream()
                .map(ListingImage::getCloudinaryPublicId)
                .collect(Collectors.toCollection(HashSet::new));

        Set<String> incomingPublicIds = new HashSet<>();
        for (ListingImage image : newImages) {
            String publicId = image.getCloudinaryPublicId();
            if (!incomingPublicIds.add(publicId) || existingPublicIds.contains(publicId)) {
                throw new BadRequestException("Duplicate image public id is not allowed");
            }
        }
    }

    private void ensureCreateImageLimit(List<ListingImageRequest> images) {
        if (images != null && images.size() > MAX_LISTING_IMAGES) {
            throw new BadRequestException("At most 5 images are allowed per listing");
        }
    }

    private void ensureTotalImageLimit(Listing listing, List<ListingImage> newImages) {
        int totalImages = listing.getImages().size() + newImages.size();
        if (totalImages > MAX_LISTING_IMAGES) {
            throw new BadRequestException("At most 5 images are allowed per listing");
        }
    }

    private void applyPrimaryImagePolicy(Listing listing, List<ListingImage> newImages) {
        boolean incomingHasPrimary = newImages.stream().anyMatch(ListingImage::isPrimary);
        if (incomingHasPrimary) {
            listing.getImages().forEach(image -> image.setPrimary(false));
            return;
        }

        boolean listingHasPrimary = listing.getImages().stream().anyMatch(ListingImage::isPrimary);
        if (!listingHasPrimary && !newImages.isEmpty()) {
            newImages.get(0).setPrimary(true);
        }
    }

    private ListingImage toImage(ListingImageRequest request) {
        ListingImage image = new ListingImage();
        image.setCloudinaryPublicId(request.cloudinaryPublicId().trim());
        image.setCloudinarySecureUrl(request.cloudinarySecureUrl().trim());
        image.setFormat(trimToNull(request.format()));
        image.setWidth(request.width());
        image.setHeight(request.height());
        image.setBytes(request.bytes());
        image.setPrimary(Boolean.TRUE.equals(request.primary()));
        return image;
    }

    private ListingResponse toResponse(Listing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getFarmer().getId(),
                listing.getFarmer().getName(),
                listing.getFarmer().getEmail(),
                listing.getFarmer().getAverageRating(),
                listing.getFarmer().getRatingCount(),
                listing.getCropName(),
                listing.getQuantity(),
                listing.getInitialQuantity(),
                listing.getPricePerKg(),
                listing.getDescription(),
                listing.getStreet(),
                listing.getCity(),
                listing.getState(),
                listing.getPincode(),
                listing.getStatus(),
                listing.getImages().stream().map(this::toResponse).toList(),
                listing.getCreatedAt(),
                listing.getUpdatedAt()
        );
    }

    private ListingImageResponse toResponse(ListingImage image) {
        return new ListingImageResponse(
                image.getId(),
                image.getCloudinaryPublicId(),
                image.getCloudinarySecureUrl(),
                image.getFormat(),
                image.getWidth(),
                image.getHeight(),
                image.getBytes(),
                image.isPrimary()
        );
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found for token"));
    }

    private void ensureFarmerRole(User user) {
        if (user.getRole() != UserRole.FARMER) {
            throw new ForbiddenException("Only FARMER users can create listings");
        }
    }

    private void ensureBuyerRole(User user) {
        if (user.getRole() != UserRole.BUYER) {
            throw new ForbiddenException("Only BUYER users can browse listings");
        }
    }

    private Listing getOwnedListingOrThrow(Long listingId, User currentUser) {
        Listing listing = listingRepository.findWithImagesById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (!listing.getFarmer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only update your own listing");
        }
        return listing;
    }

    private void ensureNoActiveNegotiations(Listing listing) {
        boolean hasActiveNegotiations = negotiationRepository.existsByListingIdAndStatusIn(
                listing.getId(),
                ACTIVE_NEGOTIATION_STATUSES
        );
        if (hasActiveNegotiations) {
            throw new BadRequestException("Cannot modify listing while active negotiations exist");
        }
    }

    private void ensureListingIsActive(Listing listing, String message) {
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new BadRequestException(message);
        }
    }

    private void validatePriceUpdateAllowed(User currentUser) {
        if (currentUser.getRole() != UserRole.FARMER) {
            throw new ForbiddenException("Only FARMER users can update listing price");
        }
    }

    private String trimDescription(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().isEmpty() ? null : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}