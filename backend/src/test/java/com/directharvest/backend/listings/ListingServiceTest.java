package com.directharvest.backend.listings.service;

import com.directharvest.backend.listings.repository.ListingRepository;
import com.directharvest.backend.users.repository.UserRepository;
import com.directharvest.backend.negotiations.repository.NegotiationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;

class ListingServiceTest {
    @Mock
    private ListingRepository listingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private NegotiationRepository negotiationRepository;
    @InjectMocks
    private ListingService listingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void maxListingImages_constant_isFive() throws Exception {
        var field = ListingService.class.getDeclaredField("MAX_LISTING_IMAGES");
        field.setAccessible(true);
        assertThat(field.getInt(null)).isEqualTo(5);
    }
}
