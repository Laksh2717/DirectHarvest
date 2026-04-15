package com.directharvest.backend.ratings.service;

import com.directharvest.backend.common.enums.OrderStatus;
import com.directharvest.backend.common.exception.BadRequestException;
import com.directharvest.backend.common.exception.ForbiddenException;
import com.directharvest.backend.common.exception.ResourceNotFoundException;
import com.directharvest.backend.common.exception.UnauthorizedException;
import com.directharvest.backend.orders.entity.Order;
import com.directharvest.backend.orders.repository.OrderRepository;
import com.directharvest.backend.ratings.entity.Rating;
import com.directharvest.backend.ratings.repository.RatingRepository;
import com.directharvest.backend.ratings.request.CreateRatingRequest;
import com.directharvest.backend.ratings.response.RatingResponse;
import com.directharvest.backend.users.entity.User;
import com.directharvest.backend.users.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository, OrderRepository orderRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RatingResponse createForOrder(Long orderId, CreateRatingRequest request) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        ensureParticipant(order, currentUser);
        ensureBuyer(order, currentUser);

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BadRequestException("Rating is allowed only for COMPLETED orders");
        }
        if (ratingRepository.existsByOrderIdAndRaterId(orderId, currentUser.getId())) {
            throw new BadRequestException("You already rated this farmer for this order");
        }

        User ratedUser = order.getFarmer();

        Rating rating = new Rating();
        rating.setOrder(order);
        rating.setListing(order.getListing());
        rating.setRater(currentUser);
        rating.setRatedUser(ratedUser);
        rating.setScore(request.score());

        Rating savedRating = ratingRepository.save(rating);
        updateUserRatingStats(ratedUser, request.score());

        return toResponse(savedRating);
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> getRatingsForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return ratingRepository.findAllByRatedUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void ensureParticipant(Order order, User currentUser) {
        Long userId = currentUser.getId();
        boolean participant = order.getBuyer().getId().equals(userId) || order.getFarmer().getId().equals(userId);
        if (!participant) {
            throw new ForbiddenException("You are not part of this order");
        }
    }

    private void ensureBuyer(Order order, User currentUser) {
        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only buyer can rate the farmer");
        }
    }

    private void updateUserRatingStats(User ratedUser, Integer newScore) {
        Integer currentCount = ratedUser.getRatingCount();
        BigDecimal currentAverage = ratedUser.getAverageRating();

        int previousCount = currentCount == null ? 0 : currentCount;
        BigDecimal previousAverage = currentAverage == null ? BigDecimal.ZERO : currentAverage;

        int updatedCount = previousCount + 1;
        BigDecimal updatedAverage = previousAverage.multiply(BigDecimal.valueOf(previousCount))
                .add(BigDecimal.valueOf(newScore))
                .divide(BigDecimal.valueOf(updatedCount), 2, RoundingMode.HALF_UP);

        ratedUser.setRatingCount(updatedCount);
        ratedUser.setAverageRating(updatedAverage);
        userRepository.save(ratedUser);
    }

    private RatingResponse toResponse(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getOrder().getId(),
                rating.getListing().getId(),
                rating.getRater().getId(),
                rating.getRater().getName(),
                rating.getRatedUser().getId(),
                rating.getRatedUser().getName(),
                rating.getScore(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
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

}

