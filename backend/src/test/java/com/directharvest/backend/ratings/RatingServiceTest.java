package com.directharvest.backend.ratings.service;

import com.directharvest.backend.ratings.repository.RatingRepository;
import com.directharvest.backend.orders.repository.OrderRepository;
import com.directharvest.backend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

class RatingServiceTest {
    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void ratingService_isNotNull() {
        assertThat(ratingService).isNotNull();
    }
}
