package com.directharvest.backend.orders.service;

import com.directharvest.backend.common.enums.CancellationBy;
import com.directharvest.backend.common.enums.ListingStatus;
import com.directharvest.backend.common.enums.OrderStatus;
import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.common.exception.BadRequestException;
import com.directharvest.backend.common.exception.ForbiddenException;
import com.directharvest.backend.common.exception.ResourceNotFoundException;
import com.directharvest.backend.common.exception.UnauthorizedException;
import com.directharvest.backend.common.enums.NegotiationEventType;
import com.directharvest.backend.listings.entity.Listing;
import com.directharvest.backend.listings.repository.ListingRepository;
import com.directharvest.backend.negotiations.entity.Negotiation;
import com.directharvest.backend.negotiations.entity.NegotiationEvent;
import com.directharvest.backend.orders.entity.Order;
import com.directharvest.backend.orders.repository.OrderRepository;
import com.directharvest.backend.orders.response.OrderDetailsResponse;
import com.directharvest.backend.orders.response.OrderNegotiationDetailsResponse;
import com.directharvest.backend.orders.request.CancelOrderRequest;
import com.directharvest.backend.orders.response.OrderResponse;
import com.directharvest.backend.ratings.entity.Rating;
import com.directharvest.backend.ratings.repository.RatingRepository;
import com.directharvest.backend.users.entity.User;
import com.directharvest.backend.users.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Duration;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OrderService {

    private static final Set<OrderStatus> COMPLETABLE_STATUSES = Set.of(OrderStatus.CONFIRMED, OrderStatus.ACTIVE);
    private static final Duration CANCELLATION_WINDOW = Duration.ofHours(24);

    private final OrderRepository orderRepository;
    private final ListingRepository listingRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            ListingRepository listingRepository,
            RatingRepository ratingRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.listingRepository = listingRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(List<OrderStatus> statuses) {
        User currentUser = getCurrentUser();
        ensureBuyerOrFarmerRole(currentUser);
        List<Order> orders;
        if (statuses == null || statuses.isEmpty()) {
            orders = orderRepository.findAllByBuyerIdOrFarmerIdOrderByUpdatedAtDesc(currentUser.getId(), currentUser.getId());
        } else if (statuses.size() == 1) {
            orders = orderRepository.findAllByBuyerIdOrFarmerIdAndStatusInOrderByUpdatedAtDesc(currentUser.getId(), currentUser.getId(), statuses);
        } else {
            orders = orderRepository.findAllByBuyerIdOrFarmerIdAndStatusInOrderByUpdatedAtDesc(currentUser.getId(), currentUser.getId(), statuses);
        }

        Map<Long, Rating> ratingsByOrderId = buildRatingsByOrderId(orders);

        return orders
            .stream()
            .map(order -> toResponse(order, ratingsByOrderId.get(order.getId())))
            .toList();
    }

    @Transactional(readOnly = true)
    public OrderDetailsResponse getById(Long orderId) {
        User currentUser = getCurrentUser();
        ensureBuyerOrFarmerRole(currentUser);

        Order order = orderRepository.findDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        ensureParticipant(order, currentUser);
        Rating rating = ratingRepository.findByOrderId(order.getId()).orElse(null);
        List<NegotiationEvent> negotiationEvents = order.getNegotiation().getEvents()
                .stream()
            .filter(event -> event.getStatusAfter() != com.directharvest.backend.common.enums.NegotiationStatus.ACCEPTED)
            .filter(event -> event.getEventType() != NegotiationEventType.ACCEPTED)
                .toList();

        return toDetailsResponse(order, rating, negotiationEvents);
    }

    @Transactional
    public OrderResponse completeOrder(Long orderId) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        ensureParticipant(order, currentUser);
        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only buyer can mark an order as completed");
        }

        ensureCompletionAllowed(order.getStatus());
        order.setStatus(OrderStatus.COMPLETED);
        if (order.getCompletedAt() == null) {
            order.setCompletedAt(Instant.now());
        }
        Rating rating = ratingRepository.findByOrderId(order.getId()).orElse(null);
        return toResponse(orderRepository.save(order), rating);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, CancelOrderRequest request) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        ensureParticipant(order, currentUser);
        return cancelAndSave(order, currentUser, request);
    }

    private void ensureParticipant(Order order, User currentUser) {
        Long userId = currentUser.getId();
        boolean participant = order.getBuyer().getId().equals(userId) || order.getFarmer().getId().equals(userId);
        if (!participant) {
            throw new ForbiddenException("You are not part of this order");
        }
    }

    private OrderResponse toResponse(Order order, Rating rating) {
        Integer ratingScore = rating != null ? rating.getScore() : null;

        return new OrderResponse(
                order.getId(),
                buildDisplayOrderId(order),
                order.getListing().getId(),
                order.getListing().getCropName(),
                order.getNegotiation().getId(),
                order.getBuyer().getId(),
                order.getBuyer().getName(),
                order.getFarmer().getId(),
                order.getFarmer().getName(),
                order.getAgreedPrice(),
                order.getAgreedQuantity(),
                order.getStatus(),
                order.getCancelledBy(),
                order.getCancelledReason(),
                order.getCancelledAt(),
                order.getActivatedAt(),
                order.getCompletedAt(),
                ratingScore != null,
                ratingScore,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private Map<Long, Rating> buildRatingsByOrderId(List<Order> orders) {
        if (orders.isEmpty()) {
            return Map.of();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        List<Rating> ratings = ratingRepository.findAllByOrderIdIn(orderIds);
        Map<Long, Rating> ratingsByOrderId = new HashMap<>();

        for (Rating rating : ratings) {
            Long orderId = rating.getOrder().getId();
            ratingsByOrderId.putIfAbsent(orderId, rating);
        }

        return ratingsByOrderId;
    }

    private OrderDetailsResponse toDetailsResponse(Order order, Rating rating, List<NegotiationEvent> negotiationEvents) {
        List<OrderNegotiationDetailsResponse> negotiationDetails = negotiationEvents
                .stream()
                .map(this::toNegotiationDetailsResponse)
                .toList();

        return new OrderDetailsResponse(
                order.getId(),
                buildDisplayOrderId(order),
                order.getListing().getId(),
                order.getListing().getCropName(),
                order.getListing().getDescription(),
                order.getListing().getStreet(),
                order.getListing().getCity(),
                order.getListing().getState(),
                order.getListing().getPincode(),
                order.getNegotiation().getId(),
                order.getBuyer().getId(),
                order.getBuyer().getName(),
                order.getBuyer().getEmail(),
                order.getFarmer().getId(),
                order.getFarmer().getName(),
                order.getFarmer().getEmail(),
                order.getFarmer().getAverageRating(),
                order.getFarmer().getRatingCount(),
                order.getAgreedPrice(),
                order.getAgreedQuantity(),
                order.getStatus(),
                order.getCancelledBy(),
                order.getCancelledReason(),
                order.getCancelledAt(),
                order.getActivatedAt(),
                order.getCompletedAt(),
                rating != null,
                rating == null ? null : rating.getScore(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                negotiationDetails
        );
    }

    private OrderNegotiationDetailsResponse toNegotiationDetailsResponse(NegotiationEvent event) {
        return new OrderNegotiationDetailsResponse(
                event.getOfferedPrice(),
                event.getRequestedQuantity(),
                event.getCreatedAt()
        );
    }

    private String buildDisplayOrderId(Order order) {
        if (order.getId() == null) {
            return "ORD-PENDING";
        }

        return "ORD-" + order.getId();
    }

    private OrderResponse cancelAndSave(Order order, User currentUser, CancelOrderRequest request) {
        ensureCancellationAllowed(order);
        restoreListingQuantity(order);
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledBy(resolveCancellationBy(order, currentUser));
        order.setCancelledAt(Instant.now());
        order.setCancelledReason(resolveCancellationReason(request));
        return toResponse(orderRepository.save(order), null);
    }

    private void restoreListingQuantity(Order order) {
        Listing listing = order.getListing();
        BigDecimal restoredQuantity = listing.getQuantity().add(order.getAgreedQuantity());
        listing.setQuantity(restoredQuantity);

        if (listing.getStatus() == ListingStatus.OUT_OF_STOCK && restoredQuantity.compareTo(BigDecimal.ZERO) > 0) {
            listing.setStatus(ListingStatus.ACTIVE);
        }

        listingRepository.save(listing);
    }

    private void ensureCancellationAllowed(Order order) {
        OrderStatus current = order.getStatus();
        if (current == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already in status CANCELLED");
        }
        if (current != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Cannot cancel order in status " + current);
        }

        Instant cancellationCutoff = order.getCreatedAt().plus(CANCELLATION_WINDOW);
        if (Instant.now().isAfter(cancellationCutoff)) {
            throw new BadRequestException("Cancellation window is over; order can only be cancelled within 24 hours of confirmation");
        }
    }

    private void ensureCompletionAllowed(OrderStatus current) {
        if (current == OrderStatus.COMPLETED) {
            throw new BadRequestException("Order is already in status COMPLETED");
        }
        if (current == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot complete order in status CANCELLED");
        }
        if (!COMPLETABLE_STATUSES.contains(current)) {
            throw new BadRequestException("Cannot complete order in status " + current);
        }
    }

    private CancellationBy resolveCancellationBy(Order order, User currentUser) {
        if (order.getBuyer().getId().equals(currentUser.getId())) {
            return CancellationBy.BUYER;
        }
        if (order.getFarmer().getId().equals(currentUser.getId())) {
            return CancellationBy.FARMER;
        }
        return CancellationBy.SYSTEM;
    }

    private String resolveCancellationReason(CancelOrderRequest request) {
        if (request == null || request.cancellationReason() == null) {
            return null;
        }

        String reason = request.cancellationReason().trim();
        return reason.isEmpty() ? null : reason;
    }

    private void ensureBuyerOrFarmerRole(User user) {
        if (user.getRole() != UserRole.BUYER && user.getRole() != UserRole.FARMER) {
            throw new ForbiddenException("Only BUYER and FARMER users can access orders");
        }
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
