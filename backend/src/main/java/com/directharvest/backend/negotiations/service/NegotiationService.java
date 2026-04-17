package com.directharvest.backend.negotiations.service;

import com.directharvest.backend.common.enums.ListingStatus;
import com.directharvest.backend.common.enums.NegotiationEventType;
import com.directharvest.backend.common.enums.NegotiationStatus;
import com.directharvest.backend.common.enums.OrderStatus;
import com.directharvest.backend.common.enums.ProposedBy;
import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.common.exception.BadRequestException;
import com.directharvest.backend.common.exception.ForbiddenException;
import com.directharvest.backend.common.exception.ResourceNotFoundException;
import com.directharvest.backend.common.exception.UnauthorizedException;
import com.directharvest.backend.listings.entity.Listing;
import com.directharvest.backend.listings.repository.ListingRepository;
import com.directharvest.backend.negotiations.entity.Negotiation;
import com.directharvest.backend.negotiations.entity.NegotiationEvent;
import com.directharvest.backend.negotiations.repository.NegotiationEventRepository;
import com.directharvest.backend.negotiations.repository.NegotiationRepository;
import com.directharvest.backend.negotiations.request.CounterOfferRequest;
import com.directharvest.backend.negotiations.request.CreateNegotiationRequest;
import com.directharvest.backend.negotiations.request.RejectNegotiationRequest;
import com.directharvest.backend.negotiations.response.NegotiationEventResponse;
import com.directharvest.backend.negotiations.response.NegotiationResponse;
import com.directharvest.backend.orders.entity.Order;
import com.directharvest.backend.orders.repository.OrderRepository;
import com.directharvest.backend.users.entity.User;
import com.directharvest.backend.users.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.time.Duration;
import java.time.Instant;

@Service
public class NegotiationService {

    private static final EnumSet<NegotiationStatus> ACTIVE_STATUSES =
            EnumSet.of(NegotiationStatus.PENDING_BUYER, NegotiationStatus.PENDING_FARMER);
    private static final Duration NEGOTIATION_EXPIRY_WINDOW = Duration.ofDays(3);

    private final NegotiationRepository negotiationRepository;
    private final NegotiationEventRepository negotiationEventRepository;
    private final OrderRepository orderRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public NegotiationService(
            NegotiationRepository negotiationRepository,
            NegotiationEventRepository negotiationEventRepository,
            OrderRepository orderRepository,
            ListingRepository listingRepository,
            UserRepository userRepository,
            EntityManager entityManager
    ) {
        this.negotiationRepository = negotiationRepository;
        this.negotiationEventRepository = negotiationEventRepository;
        this.orderRepository = orderRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public NegotiationResponse create(CreateNegotiationRequest request) {
        User buyer = getCurrentUser();
        ensureBuyerRole(buyer);

        Listing listing = listingRepository.findById(request.listingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new BadRequestException("Negotiation can only be created for ACTIVE listing");
        }
        if (listing.getFarmer().getId().equals(buyer.getId())) {
            throw new BadRequestException("You cannot negotiate on your own listing");
        }

        boolean alreadyExists = negotiationRepository.existsByListingIdAndBuyerIdAndStatusIn(
                listing.getId(),
                buyer.getId(),
                ACTIVE_STATUSES
        );
        if (alreadyExists) {
            throw new BadRequestException("An active negotiation already exists for this listing");
        }

        ensureRequestedQuantityWithinListing(request.requestedQuantity(), listing);

        Negotiation negotiation = new Negotiation();
        negotiation.setListing(listing);
        negotiation.setBuyer(buyer);
        negotiation.setFarmer(listing.getFarmer());
        negotiation.setOfferedPrice(request.offeredPrice());
        negotiation.setRequestedQuantity(request.requestedQuantity());
        negotiation.setProposedBy(ProposedBy.BUYER);
        negotiation.setStatus(NegotiationStatus.PENDING_FARMER);
        negotiation.setExpiresAt(Instant.now().plus(NEGOTIATION_EXPIRY_WINDOW));

        Negotiation saved = negotiationRepository.save(negotiation);
        recordEvent(saved, buyer, NegotiationEventType.CREATED);
        return toResponse(saved);
    }

    @Transactional
    public NegotiationResponse counterOffer(Long negotiationId, CounterOfferRequest request) {
        User currentUser = getCurrentUser();
        Negotiation negotiation = getNegotiationOrThrow(negotiationId);

        ensureParticipant(negotiation, currentUser);
        ensureCounterOfferAllowed(negotiation, currentUser);

        if (currentUser.getId().equals(negotiation.getBuyer().getId())) {
            negotiation.setProposedBy(ProposedBy.BUYER);
            negotiation.setStatus(NegotiationStatus.PENDING_FARMER);
        } else {
            negotiation.setProposedBy(ProposedBy.FARMER);
            negotiation.setStatus(NegotiationStatus.PENDING_BUYER);
        }

        ensureRequestedQuantityWithinListing(request.requestedQuantity(), negotiation.getListing());
        negotiation.setOfferedPrice(request.offeredPrice());
        negotiation.setRequestedQuantity(request.requestedQuantity());
        negotiation.setExpiresAt(Instant.now().plus(NEGOTIATION_EXPIRY_WINDOW));

        Negotiation saved = negotiationRepository.save(negotiation);
        entityManager.flush();
        entityManager.refresh(saved);
        recordEvent(saved, currentUser, NegotiationEventType.COUNTERED);
        return toResponse(saved);
    }

    @Transactional
    public NegotiationResponse accept(Long negotiationId) {
        User currentUser = getCurrentUser();
        Negotiation negotiation = getNegotiationOrThrow(negotiationId);

        ensureParticipant(negotiation, currentUser);
        ensureActionAllowedForCurrentTurn(negotiation, currentUser, "accept");

        Listing listing = negotiation.getListing();
        ensureRequestedQuantityWithinAvailableStock(negotiation.getRequestedQuantity(), listing);
        applyAcceptedQuantity(listing, negotiation.getRequestedQuantity());

        negotiation.setStatus(NegotiationStatus.ACCEPTED);
        Negotiation savedNegotiation = negotiationRepository.save(negotiation);
        listingRepository.save(listing);
        entityManager.flush();
        entityManager.refresh(savedNegotiation);
        recordEvent(savedNegotiation, currentUser, NegotiationEventType.ACCEPTED);
        createOrderIfMissing(savedNegotiation);
        return toResponse(savedNegotiation);
    }

    @Transactional
    public NegotiationResponse reject(Long negotiationId, RejectNegotiationRequest request) {
        User currentUser = getCurrentUser();
        Negotiation negotiation = getNegotiationOrThrow(negotiationId);

        ensureParticipant(negotiation, currentUser);
        ensureActionAllowedForCurrentTurn(negotiation, currentUser, "reject");

        negotiation.setStatus(NegotiationStatus.REJECTED);
        negotiation.setCancelledBy(currentUser.getRole());
        negotiation.setCancellationReason(trimToNull(request == null ? null : request.cancellationReason()));
        Negotiation saved = negotiationRepository.save(negotiation);
        entityManager.flush();
        entityManager.refresh(saved);
        recordEvent(saved, currentUser, NegotiationEventType.REJECTED);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public NegotiationResponse getById(Long negotiationId) {
        User currentUser = getCurrentUser();
        Negotiation negotiation = getNegotiationOrThrow(negotiationId);
        ensureParticipant(negotiation, currentUser);
        return toResponse(negotiation);
    }

    @Transactional(readOnly = true)
    public List<NegotiationResponse> getMyNegotiations(List<NegotiationStatus> statuses) {
        User currentUser = getCurrentUser();
        List<Negotiation> negotiations;

        if (statuses == null || statuses.isEmpty()) {
            negotiations = negotiationRepository.findAllByBuyerIdOrFarmerIdOrderByUpdatedAtDesc(currentUser.getId(), currentUser.getId());
        } else {
            negotiations = negotiationRepository.findAllByParticipantIdAndStatusInOrderByUpdatedAtDesc(currentUser.getId(), statuses);
        }

        return negotiations.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NegotiationEventResponse> getHistory(Long negotiationId) {
        User currentUser = getCurrentUser();
        Negotiation negotiation = getNegotiationOrThrow(negotiationId);
        ensureParticipant(negotiation, currentUser);

        return negotiationEventRepository.findAllByNegotiationIdOrderByCreatedAtAsc(negotiationId)
                .stream()
                .map(this::toEventResponse)
                .toList();
    }

    private Negotiation getNegotiationOrThrow(Long negotiationId) {
        return negotiationRepository.findDetailsById(negotiationId)
                .orElseThrow(() -> new ResourceNotFoundException("Negotiation not found"));
    }

    private void ensureBuyerRole(User user) {
        if (user.getRole() != UserRole.BUYER) {
            throw new ForbiddenException("Only BUYER users can create negotiations");
        }
    }

    private void ensureParticipant(Negotiation negotiation, User currentUser) {
        Long userId = currentUser.getId();
        boolean participant = negotiation.getBuyer().getId().equals(userId) || negotiation.getFarmer().getId().equals(userId);
        if (!participant) {
            throw new ForbiddenException("You are not part of this negotiation");
        }
    }

    private void ensureCounterOfferAllowed(Negotiation negotiation, User currentUser) {
        ensureNegotiationIsPending(negotiation);

        boolean buyerTurn = negotiation.getStatus() == NegotiationStatus.PENDING_BUYER;
        boolean farmerTurn = negotiation.getStatus() == NegotiationStatus.PENDING_FARMER;

        if (buyerTurn && !currentUser.getId().equals(negotiation.getBuyer().getId())) {
            throw new BadRequestException("It is buyer turn to respond");
        }
        if (farmerTurn && !currentUser.getId().equals(negotiation.getFarmer().getId())) {
            throw new BadRequestException("It is farmer turn to respond");
        }
    }

    private void ensureRequestedQuantityWithinListing(java.math.BigDecimal requestedQuantity, Listing listing) {
        if (requestedQuantity.compareTo(listing.getQuantity()) > 0) {
            throw new BadRequestException("Requested quantity cannot exceed available listing quantity");
        }
    }

    private void ensureRequestedQuantityWithinAvailableStock(BigDecimal requestedQuantity, Listing listing) {
        if (requestedQuantity.compareTo(listing.getQuantity()) > 0) {
            throw new BadRequestException("Not enough listing quantity available to accept negotiation");
        }
    }

    private void applyAcceptedQuantity(Listing listing, BigDecimal acceptedQuantity) {
        BigDecimal remaining = listing.getQuantity().subtract(acceptedQuantity);
        listing.setQuantity(remaining);
        listing.setStatus(remaining.compareTo(BigDecimal.ZERO) == 0 ? ListingStatus.OUT_OF_STOCK : ListingStatus.ACTIVE);
    }

    private void ensureActionAllowedForCurrentTurn(Negotiation negotiation, User currentUser, String action) {
        ensureNegotiationIsPending(negotiation);

        boolean isBuyer = currentUser.getId().equals(negotiation.getBuyer().getId());
        boolean expectedBuyer = negotiation.getStatus() == NegotiationStatus.PENDING_BUYER;

        if (isBuyer != expectedBuyer) {
            throw new BadRequestException("You cannot " + action + " at this stage");
        }
    }

    private void ensureNegotiationIsPending(Negotiation negotiation) {
        if (!ACTIVE_STATUSES.contains(negotiation.getStatus())) {
            throw new BadRequestException("Negotiation is already closed");
        }
    }

    private NegotiationResponse toResponse(Negotiation negotiation) {
        // Get latest event values to ensure we always show the most recent counter-offer values
        NegotiationEvent latestEvent = getLatestEvent(negotiation);
        BigDecimal offerPrice = latestEvent != null ? latestEvent.getOfferedPrice() : negotiation.getOfferedPrice();
        BigDecimal reqQuantity = latestEvent != null ? latestEvent.getRequestedQuantity() : negotiation.getRequestedQuantity();
        
        return new NegotiationResponse(
                negotiation.getId(),
                negotiation.getListing().getId(),
                negotiation.getListing().getCropName(),
                negotiation.getBuyer().getId(),
                negotiation.getBuyer().getName(),
            negotiation.getBuyer().getEmail(),
                negotiation.getFarmer().getId(),
                negotiation.getFarmer().getName(),
            negotiation.getFarmer().getEmail(),
                offerPrice,
                reqQuantity,
                negotiation.getStatus(),
                negotiation.getProposedBy(),
                negotiation.getCancellationReason(),
                negotiation.getCancelledBy(),
                negotiation.getExpiresAt(),
                negotiation.getCreatedAt(),
                negotiation.getUpdatedAt()
        );
    }
    
    private NegotiationEvent getLatestEvent(Negotiation negotiation) {
        List<NegotiationEvent> events = negotiationEventRepository.findAllByNegotiationIdOrderByCreatedAtAsc(negotiation.getId());
        return events.isEmpty() ? null : events.get(events.size() - 1);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private NegotiationEventResponse toEventResponse(NegotiationEvent event) {
        return new NegotiationEventResponse(
                event.getId(),
                event.getNegotiation().getId(),
                event.getEventType(),
                event.getActor() == null ? null : event.getActor().getId(),
                event.getActor() == null ? null : event.getActor().getName(),
                event.getActorRole(),
                event.getOfferedPrice(),
                event.getRequestedQuantity(),
                event.getStatusAfter(),
                event.getCreatedAt()
        );
    }

    private void recordEvent(Negotiation negotiation, User actor, NegotiationEventType eventType) {
        recordEvent(negotiation, actor, eventType, actor == null ? null : actor.getRole());
    }

    private void recordEvent(Negotiation negotiation, User actor, NegotiationEventType eventType, UserRole actorRole) {
        NegotiationEvent event = new NegotiationEvent();
        event.setNegotiation(negotiation);
        event.setActor(actor);
        event.setActorRole(actorRole);
        event.setEventType(eventType);
        event.setOfferedPrice(negotiation.getOfferedPrice());
        event.setRequestedQuantity(negotiation.getRequestedQuantity());
        event.setStatusAfter(negotiation.getStatus());
        negotiationEventRepository.save(event);
    }

    private void createOrderIfMissing(Negotiation negotiation) {
        if (orderRepository.existsByNegotiationId(negotiation.getId())) {
            return;
        }

        Order order = new Order();
        order.setNegotiation(negotiation);
        order.setListing(negotiation.getListing());
        order.setBuyer(negotiation.getBuyer());
        order.setFarmer(negotiation.getFarmer());
        order.setAgreedPrice(negotiation.getOfferedPrice());
        order.setAgreedQuantity(negotiation.getRequestedQuantity());
        order.setStatus(OrderStatus.CONFIRMED);

        orderRepository.save(order);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found for token"));
    }
}
