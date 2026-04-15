package com.directharvest.backend.negotiations.service;

import com.directharvest.backend.negotiations.repository.NegotiationRepository;
import com.directharvest.backend.negotiations.repository.NegotiationEventRepository;
import com.directharvest.backend.orders.repository.OrderRepository;
import com.directharvest.backend.listings.repository.ListingRepository;
import com.directharvest.backend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

class NegotiationServiceTest {
    @Mock
    private NegotiationRepository negotiationRepository;
    @Mock
    private NegotiationEventRepository negotiationEventRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ListingRepository listingRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private NegotiationService negotiationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void negotiationExpiryWindow_constant_isThreeDays() throws Exception {
        var field = NegotiationService.class.getDeclaredField("NEGOTIATION_EXPIRY_WINDOW");
        field.setAccessible(true);
        var duration = (java.time.Duration) field.get(null);
        assertThat(duration.toDays()).isEqualTo(3);
    }
}
