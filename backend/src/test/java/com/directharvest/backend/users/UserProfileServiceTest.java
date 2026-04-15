package com.directharvest.backend.users.service;

import com.directharvest.backend.negotiations.repository.NegotiationRepository;
import com.directharvest.backend.users.entity.User;
import com.directharvest.backend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserProfileServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private NegotiationRepository negotiationRepository;
    @InjectMocks
    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toProfileResponse_returnsExpectedFields() throws Exception {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getName()).thenReturn("Test User");
        when(user.getEmail()).thenReturn("test@example.com");
        var method = UserProfileService.class.getDeclaredMethod("toProfileResponse", User.class);
        method.setAccessible(true);
        var resp = method.invoke(userProfileService, user);
        assertThat(resp).isNotNull();
    }

    @Test
    void normalizeEmail_trimsAndLowercases() throws Exception {
        var method = UserProfileService.class.getDeclaredMethod("normalizeEmail", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(userProfileService, "  Test@Example.COM  ");
        assertThat(result).isEqualTo("test@example.com");
    }

    @Test
    void toProfileResponse_setsFarmerFields_whenUserIsFarmer() throws Exception {
        User user = mock(User.class);
        when(user.getRole()).thenReturn(com.directharvest.backend.common.enums.UserRole.FARMER);
        when(user.getName()).thenReturn("Farmer");
        when(user.getEmail()).thenReturn("farmer@example.com");
        when(user.getStreet()).thenReturn("Street");
        when(user.getCity()).thenReturn("City");
        when(user.getState()).thenReturn("State");
        when(user.getPincode()).thenReturn("12345");
        when(user.getAverageRating()).thenReturn(new java.math.BigDecimal("4.5"));
        when(user.getRatingCount()).thenReturn(10);
        var method = UserProfileService.class.getDeclaredMethod("toProfileResponse", User.class);
        method.setAccessible(true);
        var resp = (com.directharvest.backend.users.response.UserProfileResponse) method.invoke(userProfileService, user);
        assertThat(resp.averageRating()).isEqualTo(new java.math.BigDecimal("4.5"));
        assertThat(resp.ratingCount()).isEqualTo(10);
    }

    @Test
    void toProfileResponse_setsNullFarmerFields_whenUserIsNotFarmer() throws Exception {
        User user = mock(User.class);
        when(user.getRole()).thenReturn(com.directharvest.backend.common.enums.UserRole.BUYER);
        when(user.getName()).thenReturn("Buyer");
        when(user.getEmail()).thenReturn("buyer@example.com");
        when(user.getStreet()).thenReturn("Street");
        when(user.getCity()).thenReturn("City");
        when(user.getState()).thenReturn("State");
        when(user.getPincode()).thenReturn("12345");
        var method = UserProfileService.class.getDeclaredMethod("toProfileResponse", User.class);
        method.setAccessible(true);
        var resp = (com.directharvest.backend.users.response.UserProfileResponse) method.invoke(userProfileService, user);
        assertThat(resp.averageRating()).isNull();
        assertThat(resp.ratingCount()).isNull();
    }

    @Test
    void ensureNoActiveNegotiations_throws_whenActiveNegotiationsExist() throws Exception {
        when(negotiationRepository.existsActiveByParticipantId(anyLong(), any())).thenReturn(true);
        var method = UserProfileService.class.getDeclaredMethod("ensureNoActiveNegotiations", Long.class);
        method.setAccessible(true);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> method.invoke(userProfileService, 1L))
                .hasCauseInstanceOf(com.directharvest.backend.common.exception.BadRequestException.class);
    }
}
