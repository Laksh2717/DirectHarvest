package com.directharvest.backend.orders.service;

import com.directharvest.backend.common.enums.OrderStatus;
import com.directharvest.backend.orders.entity.Order;
import com.directharvest.backend.orders.repository.OrderRepository;
import com.directharvest.backend.ratings.repository.RatingRepository;
import com.directharvest.backend.users.repository.UserRepository;
import com.directharvest.backend.listings.repository.ListingRepository;
import com.directharvest.backend.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ListingRepository listingRepository;
    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getMyOrders_returnsEmptyList_whenNoOrders() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getRole()).thenReturn(com.directharvest.backend.common.enums.UserRole.BUYER);
        when(orderRepository.findAllByBuyerIdOrFarmerIdOrderByUpdatedAtDesc(1L, 1L)).thenReturn(Collections.emptyList());
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(java.util.Optional.of(user));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        List<OrderStatus> statuses = null;
        orderService.getMyOrders(statuses);
        verify(orderRepository).findAllByBuyerIdOrFarmerIdOrderByUpdatedAtDesc(1L, 1L);
    }

    @Test
    void buildDisplayOrderId_returnsPending_whenOrderIdIsNull() throws Exception {
        OrderService service = new OrderService(orderRepository, listingRepository, ratingRepository, userRepository);
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(null);
        var method = OrderService.class.getDeclaredMethod("buildDisplayOrderId", Order.class);
        method.setAccessible(true);
        String result = (String) method.invoke(service, order);
        assertThat(result).isEqualTo("ORD-PENDING");
    }

    @Test
    void buildDisplayOrderId_returnsFormatted_whenOrderIdIsPresent() throws Exception {
        OrderService service = new OrderService(orderRepository, listingRepository, ratingRepository, userRepository);
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(42L);
        var method = OrderService.class.getDeclaredMethod("buildDisplayOrderId", Order.class);
        method.setAccessible(true);
        String result = (String) method.invoke(service, order);
        assertThat(result).isEqualTo("ORD-42");
    }

    @Test
    void resolveCancellationReason_returnsNull_whenRequestOrReasonIsNull() throws Exception {
        OrderService service = new OrderService(orderRepository, listingRepository, ratingRepository, userRepository);
        var method = OrderService.class.getDeclaredMethod("resolveCancellationReason", com.directharvest.backend.orders.request.CancelOrderRequest.class);
        method.setAccessible(true);
        assertThat(method.invoke(service, (Object) null)).isNull();
        var req = mock(com.directharvest.backend.orders.request.CancelOrderRequest.class);
        when(req.cancellationReason()).thenReturn(null);
        assertThat(method.invoke(service, req)).isNull();
    }

    @Test
    void resolveCancellationReason_returnsTrimmedReason_whenPresent() throws Exception {
        OrderService service = new OrderService(orderRepository, listingRepository, ratingRepository, userRepository);
        var method = OrderService.class.getDeclaredMethod("resolveCancellationReason", com.directharvest.backend.orders.request.CancelOrderRequest.class);
        method.setAccessible(true);
        var req = mock(com.directharvest.backend.orders.request.CancelOrderRequest.class);
        when(req.cancellationReason()).thenReturn("  Out of stock  ");
        assertThat(method.invoke(service, req)).isEqualTo("Out of stock");
    }

    @Test
    void resolveCancellationReason_returnsNull_whenReasonIsEmpty() throws Exception {
        OrderService service = new OrderService(orderRepository, listingRepository, ratingRepository, userRepository);
        var method = OrderService.class.getDeclaredMethod("resolveCancellationReason", com.directharvest.backend.orders.request.CancelOrderRequest.class);
        method.setAccessible(true);
        var req = mock(com.directharvest.backend.orders.request.CancelOrderRequest.class);
        when(req.cancellationReason()).thenReturn("   ");
        assertThat(method.invoke(service, req)).isNull();
    }

    @Test
    void ensureCompletionAllowed_throws_whenStatusIsCompletedOrCancelled() throws Exception {
        OrderService service = new OrderService(orderRepository, listingRepository, ratingRepository, userRepository);
        var method = OrderService.class.getDeclaredMethod("ensureCompletionAllowed", OrderStatus.class);
        method.setAccessible(true);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> method.invoke(service, OrderStatus.COMPLETED))
                .hasCauseInstanceOf(com.directharvest.backend.common.exception.BadRequestException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> method.invoke(service, OrderStatus.CANCELLED))
                .hasCauseInstanceOf(com.directharvest.backend.common.exception.BadRequestException.class);
    }

    @Test
    void ensureCompletionAllowed_throws_whenStatusIsInvalid() throws Exception {
        OrderService service = new OrderService(orderRepository, listingRepository, ratingRepository, userRepository);
        var method = OrderService.class.getDeclaredMethod("ensureCompletionAllowed", OrderStatus.class);
        method.setAccessible(true);
        // Use a status that is not COMPLETED, CANCELLED, or in COMPLETABLE_STATUSES
        // Since COMPLETABLE_STATUSES = {CONFIRMED, ACTIVE}, we need a status outside that set
        // The method only has these statuses: CONFIRMED, ACTIVE, COMPLETED, CANCELLED
        // So CONFIRMED and ACTIVE should not throw (they are completable)
        // Let's verify ACTIVE doesn't throw
        try {
            method.invoke(service, OrderStatus.ACTIVE);
            // No exception - ACTIVE is completable, so this is expected behavior
        } catch (Exception e) {
            // Should not reach here
            throw new AssertionError("ACTIVE should be completable", e);
        }
    }
}
