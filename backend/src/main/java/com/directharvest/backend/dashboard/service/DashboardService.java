package com.directharvest.backend.dashboard.service;

import com.directharvest.backend.common.enums.NegotiationStatus;
import com.directharvest.backend.common.enums.OrderStatus;
import com.directharvest.backend.common.enums.ListingStatus;
import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.common.exception.BadRequestException;
import com.directharvest.backend.common.exception.UnauthorizedException;
import com.directharvest.backend.dashboard.request.OverviewChartGranularity;
import com.directharvest.backend.dashboard.response.BuyerKpisResponse;
import com.directharvest.backend.dashboard.response.FarmerKpisResponse;
import com.directharvest.backend.dashboard.response.OverviewChartPointResponse;
import com.directharvest.backend.dashboard.response.OverviewChartResponse;
import com.directharvest.backend.dashboard.response.OverviewQuickActionResponse;
import com.directharvest.backend.dashboard.response.OverviewQuickActionsResponse;
import com.directharvest.backend.dashboard.response.OverviewResponse;
import com.directharvest.backend.listings.entity.Listing;
import com.directharvest.backend.negotiations.entity.Negotiation;
import com.directharvest.backend.orders.entity.Order;
import com.directharvest.backend.listings.repository.ListingRepository;
import com.directharvest.backend.negotiations.repository.NegotiationRepository;
import com.directharvest.backend.orders.repository.OrderRepository;
import com.directharvest.backend.ratings.repository.RatingRepository;
import com.directharvest.backend.users.entity.User;
import com.directharvest.backend.users.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class DashboardService {

    private static final int QUICK_ACTIONS_LIMIT = 3;
    private static final BigDecimal LOW_STOCK_THRESHOLD_RATIO = new BigDecimal("0.25");

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final OrderRepository orderRepository;
    private final RatingRepository ratingRepository;
    private final NegotiationRepository negotiationRepository;

    public DashboardService(
            UserRepository userRepository,
            ListingRepository listingRepository,
            OrderRepository orderRepository,
            RatingRepository ratingRepository,
            NegotiationRepository negotiationRepository
    ) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.orderRepository = orderRepository;
        this.ratingRepository = ratingRepository;
        this.negotiationRepository = negotiationRepository;
    }

    @Transactional(readOnly = true)
    public OverviewResponse getOverview() {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == UserRole.FARMER) {
            FarmerKpisResponse farmerKpis = getFarmerKpis(currentUser.getId());
            return new OverviewResponse("FARMER", farmerKpis, null);
        } else if (currentUser.getRole() == UserRole.BUYER) {
            BuyerKpisResponse buyerKpis = getBuyerKpis(currentUser.getId());
            return new OverviewResponse("BUYER", null, buyerKpis);
        } else {
            throw new UnauthorizedException("Invalid user role");
        }
    }

    @Transactional(readOnly = true)
    public OverviewChartResponse getOverviewChart(OverviewChartGranularity granularity, Integer year) {
        User currentUser = getCurrentUser();

        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime joinedAtUtc = currentUser.getCreatedAt().atZone(ZoneOffset.UTC);

        int currentYear = nowUtc.getYear();
        int currentMonth = nowUtc.getMonthValue();
        int joinedYear = joinedAtUtc.getYear();
        int joinedMonth = joinedAtUtc.getMonthValue();

        List<Integer> availableYears = new ArrayList<>();
        for (int y = joinedYear; y <= currentYear; y++) {
            availableYears.add(y);
        }

        if (granularity == OverviewChartGranularity.YEARLY) {
            return buildYearlyChart(currentUser, joinedYear, currentYear, availableYears);
        }

        int selectedYear = year == null ? currentYear : year;
        if (selectedYear < joinedYear || selectedYear > currentYear) {
            throw new BadRequestException("Year must be between joining year and current year");
        }

        return buildMonthlyChart(
                currentUser,
                selectedYear,
                joinedYear,
                joinedMonth,
                currentYear,
                currentMonth,
                availableYears
        );
    }

    @Transactional(readOnly = true)
    public OverviewQuickActionsResponse getOverviewQuickActions() {
        User currentUser = getCurrentUser();
        List<OverviewQuickActionResponse> actions = new ArrayList<>();

        if (currentUser.getRole() == UserRole.FARMER) {
            fillFarmerQuickActions(currentUser, actions);
            return new OverviewQuickActionsResponse("FARMER", actions);
        }

        if (currentUser.getRole() == UserRole.BUYER) {
            fillBuyerQuickActions(currentUser, actions);
            return new OverviewQuickActionsResponse("BUYER", actions);
        }

        throw new UnauthorizedException("Invalid user role");
    }

    private FarmerKpisResponse getFarmerKpis(Long farmerId) {
        Long totalActiveListings = listingRepository.countByFarmerIdAndStatus(farmerId, ListingStatus.ACTIVE);
        Long activeOrders = orderRepository.countByFarmerIdAndStatus(farmerId, OrderStatus.ACTIVE);
        Long totalCompletedOrders = orderRepository.countByFarmerIdAndStatus(farmerId, OrderStatus.COMPLETED);
        Long totalCancelledOrders = orderRepository.countByFarmerIdAndStatus(farmerId, OrderStatus.CANCELLED);

        Double averageRating = ratingRepository.findAverageScoreByRatedUserId(farmerId).orElse(null);
        Long ratingCount = ratingRepository.countByRatedUserId(farmerId);

        return new FarmerKpisResponse(
                totalActiveListings,
                activeOrders,
                totalCompletedOrders,
                averageRating,
                ratingCount,
                totalCancelledOrders
        );
    }

    private BuyerKpisResponse getBuyerKpis(Long buyerId) {
        Collection<NegotiationStatus> activeNegotiationStatuses = Set.of(
                NegotiationStatus.PENDING_BUYER,
                NegotiationStatus.PENDING_FARMER
        );
        Long totalActiveNegotiations = negotiationRepository.countByParticipantIdAndStatusIn(buyerId, activeNegotiationStatuses);

        Long activeOrders = orderRepository.countByBuyerIdAndStatus(buyerId, OrderStatus.ACTIVE);
        Long totalCompletedOrders = orderRepository.countByBuyerIdAndStatus(buyerId, OrderStatus.COMPLETED);
        Long totalCancelledOrders = orderRepository.countByBuyerIdAndStatus(buyerId, OrderStatus.CANCELLED);

        return new BuyerKpisResponse(
                totalActiveNegotiations,
                activeOrders,
                totalCompletedOrders,
                totalCancelledOrders
        );
    }

    private OverviewChartResponse buildYearlyChart(
            User currentUser,
            int joinedYear,
            int currentYear,
            List<Integer> availableYears
    ) {
        List<Object[]> rows;
        String role;
        String metric;

        if (currentUser.getRole() == UserRole.FARMER) {
            rows = orderRepository.sumCompletedAmountByFarmerGroupedByYear(currentUser.getId());
            role = "FARMER";
            metric = "TOTAL_REVENUE";
        } else if (currentUser.getRole() == UserRole.BUYER) {
            rows = orderRepository.sumCompletedAmountByBuyerGroupedByYear(currentUser.getId());
            role = "BUYER";
            metric = "TOTAL_SPENDING";
        } else {
            throw new UnauthorizedException("Invalid user role");
        }

        Map<Integer, BigDecimal> totalsByYear = toPeriodAmountMap(rows);
        List<OverviewChartPointResponse> points = new ArrayList<>();

        for (int y = joinedYear; y <= currentYear; y++) {
            BigDecimal amount = totalsByYear.getOrDefault(y, BigDecimal.ZERO);
            points.add(new OverviewChartPointResponse(y, null, String.valueOf(y), amount));
        }

        return new OverviewChartResponse(
                role,
                metric,
                OverviewChartGranularity.YEARLY,
                null,
                availableYears,
                points
        );
    }

    private OverviewChartResponse buildMonthlyChart(
            User currentUser,
            int selectedYear,
            int joinedYear,
            int joinedMonth,
            int currentYear,
            int currentMonth,
            List<Integer> availableYears
    ) {
        List<Object[]> rows;
        String role;
        String metric;

        if (currentUser.getRole() == UserRole.FARMER) {
            rows = orderRepository.sumCompletedAmountByFarmerGroupedByMonth(currentUser.getId(), selectedYear);
            role = "FARMER";
            metric = "TOTAL_REVENUE";
        } else if (currentUser.getRole() == UserRole.BUYER) {
            rows = orderRepository.sumCompletedAmountByBuyerGroupedByMonth(currentUser.getId(), selectedYear);
            role = "BUYER";
            metric = "TOTAL_SPENDING";
        } else {
            throw new UnauthorizedException("Invalid user role");
        }

        int startMonth = selectedYear == joinedYear ? joinedMonth : 1;
        int endMonth = selectedYear == currentYear ? currentMonth : 12;

        Map<Integer, BigDecimal> totalsByMonth = toPeriodAmountMap(rows);
        List<OverviewChartPointResponse> points = new ArrayList<>();

        for (int month = startMonth; month <= endMonth; month++) {
            BigDecimal amount = totalsByMonth.getOrDefault(month, BigDecimal.ZERO);
            String label = monthName(month);
            points.add(new OverviewChartPointResponse(selectedYear, month, label, amount));
        }

        return new OverviewChartResponse(
                role,
                metric,
                OverviewChartGranularity.MONTHLY,
                selectedYear,
                availableYears,
                points
        );
    }

    private Map<Integer, BigDecimal> toPeriodAmountMap(List<Object[]> rows) {
        Map<Integer, BigDecimal> values = new HashMap<>();
        for (Object[] row : rows) {
            Integer period = ((Number) row[0]).intValue();
            BigDecimal amount;
            if (row[1] instanceof BigDecimal bigDecimal) {
                amount = bigDecimal;
            } else {
                amount = BigDecimal.valueOf(((Number) row[1]).doubleValue());
            }
            values.put(period, amount);
        }
        return values;
    }

    private String monthName(int month) {
        return java.time.Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }

    private void fillFarmerQuickActions(User currentUser, List<OverviewQuickActionResponse> actions) {
        List<Negotiation> pendingFarmerNegotiations = negotiationRepository.findAllByParticipantIdAndStatusInOrderByUpdatedAtDesc(
                currentUser.getId(),
                List.of(NegotiationStatus.PENDING_FARMER)
        );

        for (Negotiation negotiation : pendingFarmerNegotiations) {
            if (actions.size() >= QUICK_ACTIONS_LIMIT) {
                return;
            }
            actions.add(new OverviewQuickActionResponse(
                    "NEGOTIATION_MY_TURN",
                    "Respond to Offer",
                    "%s offer from %s is waiting for your response.".formatted(
                            negotiation.getListing().getCropName(),
                            negotiation.getBuyer().getName()
                    ),
                    "Open Offers",
                    "/farmer/offers",
                    negotiation.getId()
            ));
        }

        fillActiveOrderActions(currentUser, actions, "/farmer/active-orders", "PICKUP_PENDING");

        if (actions.size() < QUICK_ACTIONS_LIMIT) {
            findLowStockListing(currentUser.getId())
                .ifPresent(listing -> actions.add(buildLowStockAction(listing)));
        }

        if (actions.size() < QUICK_ACTIONS_LIMIT) {
            actions.add(new OverviewQuickActionResponse(
                    "ALL_CAUGHT_UP",
                    "All Caught Up",
                    "No urgent action needed right now. You can add a new listing to keep growing.",
                    "Create Listing",
                    "/farmer/create-listing",
                    null
            ));
        }

        while (actions.size() < QUICK_ACTIONS_LIMIT) {
            actions.add(new OverviewQuickActionResponse(
                    "REVIEW_LISTINGS",
                    "Review Listings",
                    "Keep your listings updated with latest quantity and prices.",
                    "Go to Listings",
                    "/farmer/listings",
                    null
            ));
        }

        replaceDuplicateActionWithProfile(actions, "FARMER");
    }

    private void fillBuyerQuickActions(User currentUser, List<OverviewQuickActionResponse> actions) {
        List<Negotiation> pendingBuyerNegotiations = negotiationRepository.findAllByParticipantIdAndStatusInOrderByUpdatedAtDesc(
                currentUser.getId(),
                List.of(NegotiationStatus.PENDING_BUYER)
        );

        for (Negotiation negotiation : pendingBuyerNegotiations) {
            if (actions.size() >= QUICK_ACTIONS_LIMIT) {
                return;
            }
            actions.add(new OverviewQuickActionResponse(
                    "NEGOTIATION_MY_TURN",
                    "Respond to Offer",
                    "%s offer from %s is waiting for your response.".formatted(
                            negotiation.getListing().getCropName(),
                            negotiation.getFarmer().getName()
                    ),
                    "Open Offers",
                    "/buyer/offers",
                    negotiation.getId()
            ));
        }

        fillActiveOrderActions(currentUser, actions, "/buyer/active-orders", "PICKUP_PENDING");

        if (actions.size() < QUICK_ACTIONS_LIMIT) {
            actions.add(new OverviewQuickActionResponse(
                    "BROWSE_PRODUCTS",
                    "Browse New Products",
                    "Explore fresh listings and place new offers.",
                    "Browse Products",
                    "/buyer/browse-products",
                    null
            ));
        }

        if (actions.size() < QUICK_ACTIONS_LIMIT) {
            actions.add(new OverviewQuickActionResponse(
                    "NO_URGENT_ACTIONS",
                    "No Urgent Actions",
                    "You are up to date. Check new products when you are ready.",
                    "Browse Products",
                    "/buyer/browse-products",
                    null
            ));
        }

        while (actions.size() < QUICK_ACTIONS_LIMIT) {
            actions.add(new OverviewQuickActionResponse(
                    "VIEW_COMPLETED",
                    "View Past Orders",
                    "Review your completed purchases and ratings.",
                    "Completed Orders",
                    "/buyer/completed-orders",
                    null
            ));
        }

        replaceDuplicateActionWithProfile(actions, "BUYER");
    }

    private void fillActiveOrderActions(
            User currentUser,
            List<OverviewQuickActionResponse> actions,
            String ctaPath,
            String actionType
    ) {
        if (actions.size() >= QUICK_ACTIONS_LIMIT) {
            return;
        }

        List<Order> activeOrders = orderRepository.findAllByBuyerIdOrFarmerIdAndStatusInOrderByUpdatedAtDesc(
                currentUser.getId(),
                currentUser.getId(),
                List.of(OrderStatus.ACTIVE)
        );

        for (Order order : activeOrders) {
            if (actions.size() >= QUICK_ACTIONS_LIMIT) {
                return;
            }

            actions.add(new OverviewQuickActionResponse(
                    actionType,
                    "Pending Pickup",
                    "Order #%d for %s is active and needs pickup coordination.".formatted(
                            order.getId(),
                            order.getListing().getCropName()
                    ),
                    "View Active Orders",
                    ctaPath,
                    order.getId()
            ));
        }
    }

    private void replaceDuplicateActionWithProfile(
            List<OverviewQuickActionResponse> actions,
            String role
    ) {
        Set<String> seenActionTypes = new java.util.HashSet<>();
        Set<String> seenCtaPaths = new java.util.HashSet<>();

        for (int i = 0; i < actions.size(); i++) {
            OverviewQuickActionResponse action = actions.get(i);

            // Check if we've already seen this actionType or ctaPath
            if (seenActionTypes.contains(action.actionType()) ||
                seenCtaPaths.contains(action.ctaPath())) {
                // Replace with GO_TO_PROFILE
                String profilePath = role.equals("FARMER") ? "/farmer/profile" : "/buyer/profile";
                actions.set(i, new OverviewQuickActionResponse(
                        "GO_TO_PROFILE",
                        "Go to Profile",
                        "Update your profile and account information.",
                        "View Profile",
                        profilePath,
                        null
                ));
            } else {
                // Mark this action as seen
                seenActionTypes.add(action.actionType());
                seenCtaPaths.add(action.ctaPath());
            }
        }
    }

    private OverviewQuickActionResponse buildLowStockAction(Listing listing) {
        BigDecimal initial = listing.getInitialQuantity();
        BigDecimal ratio = initial == null || initial.compareTo(BigDecimal.ZERO) <= 0
            ? BigDecimal.ONE
            : listing.getQuantity().divide(initial, 4, java.math.RoundingMode.HALF_UP);
        BigDecimal remainingPercent = ratio.multiply(new BigDecimal("100")).setScale(0, java.math.RoundingMode.HALF_UP);

        return new OverviewQuickActionResponse(
                "LOW_STOCK",
                "Low Stock Alert",
            "%s is running low (%s%% left, %.2f kg remaining).".formatted(
                        listing.getCropName(),
                remainingPercent.toPlainString(),
                        listing.getQuantity()
                ),
                "Manage Listing",
                "/farmer/listings",
                listing.getId()
        );
    }

        private Optional<Listing> findLowStockListing(Long farmerId) {
        List<Listing> activeListings = listingRepository.findAllByFarmerIdAndStatusOrderByCreatedAtDesc(
            farmerId,
            ListingStatus.ACTIVE
        );

        return activeListings.stream()
            .filter(listing -> listing.getInitialQuantity() != null)
            .filter(listing -> listing.getInitialQuantity().compareTo(BigDecimal.ZERO) > 0)
            .filter(listing -> listing.getQuantity()
                .divide(listing.getInitialQuantity(), 4, java.math.RoundingMode.HALF_UP)
                .compareTo(LOW_STOCK_THRESHOLD_RATIO) <= 0)
            .min((a, b) -> {
                BigDecimal ratioA = a.getQuantity().divide(a.getInitialQuantity(), 4, java.math.RoundingMode.HALF_UP);
                BigDecimal ratioB = b.getQuantity().divide(b.getInitialQuantity(), 4, java.math.RoundingMode.HALF_UP);
                return ratioA.compareTo(ratioB);
            });
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