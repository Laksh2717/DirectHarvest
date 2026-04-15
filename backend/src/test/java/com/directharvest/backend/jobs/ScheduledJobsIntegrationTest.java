package com.directharvest.backend.jobs;

import com.directharvest.backend.common.enums.AuthProvider;
import com.directharvest.backend.common.enums.ListingStatus;
import com.directharvest.backend.common.enums.NegotiationStatus;
import com.directharvest.backend.common.enums.OrderStatus;
import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.listings.entity.Listing;
import com.directharvest.backend.listings.repository.ListingRepository;
import com.directharvest.backend.negotiations.entity.Negotiation;
import com.directharvest.backend.negotiations.repository.NegotiationEventRepository;
import com.directharvest.backend.negotiations.repository.NegotiationRepository;
import com.directharvest.backend.orders.entity.Order;
import com.directharvest.backend.orders.repository.OrderRepository;
import com.directharvest.backend.security.jwt.JwtService;
import com.directharvest.backend.security.service.CustomUserDetails;
import com.directharvest.backend.users.entity.User;
import com.directharvest.backend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ScheduledJobsIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private NegotiationRepository negotiationRepository;

    @Autowired
    private NegotiationEventRepository negotiationEventRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NegotiationExpiryJob negotiationExpiryJob;

    @Autowired
    private OrderActivationJob orderActivationJob;

    @Autowired
    private OrderAutoCompleteJob orderAutoCompleteJob;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void cleanState() {
        negotiationEventRepository.deleteAll();
        orderRepository.deleteAll();
        negotiationRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void expireNegotiations_marksOldOpenNegotiationExpired() {
        User farmer = saveUser("Farmer J1", "farmer.j1@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer J1", "buyer.j1@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Carrot Batch");

        Negotiation negotiation = new Negotiation();
        negotiation.setListing(listing);
        negotiation.setBuyer(buyer);
        negotiation.setFarmer(farmer);
        negotiation.setOfferedPrice(new BigDecimal("25.00"));
        negotiation.setRequestedQuantity(new BigDecimal("10.00"));
        negotiation.setProposedBy(com.directharvest.backend.common.enums.ProposedBy.BUYER);
        negotiation.setStatus(NegotiationStatus.PENDING_FARMER);
        negotiation.setExpiresAt(Instant.now().plusSeconds(300));
        negotiation = negotiationRepository.save(negotiation);

        jdbcTemplate.update(
                "update negotiations set expires_at = ? where id = ?",
                Timestamp.from(Instant.now().minusSeconds(10)),
                negotiation.getId()
        );

        negotiationExpiryJob.expireNegotiations();

        Negotiation updated = negotiationRepository.findById(negotiation.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(NegotiationStatus.EXPIRED);
        assertThat(negotiationEventRepository.findAllByNegotiationIdOrderByCreatedAtAsc(negotiation.getId()))
                .hasSize(1)
                .first()
                .extracting(com.directharvest.backend.negotiations.entity.NegotiationEvent::getEventType)
                .isEqualTo(com.directharvest.backend.common.enums.NegotiationEventType.EXPIRED);
    }

    @Test
    void activateOrders_movesOldConfirmedOrderToActive() throws Exception {
        User farmer = saveUser("Farmer J2", "farmer.j2@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer J2", "buyer.j2@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Potato Batch");

        Long orderId = createAcceptedOrder(listing.getId(), buyer, farmer);

        jdbcTemplate.update(
                "update orders set created_at = ? where id = ?",
                Timestamp.from(Instant.now().minusSeconds(60L * 60L * 25L)),
                orderId
        );

        orderActivationJob.activateOrders();

        Order updated = orderRepository.findById(orderId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.ACTIVE);
        assertThat(updated.getActivatedAt()).isNotNull();
    }

    @Test
    void autoCompleteOrders_movesOldConfirmedOrderToCompleted() throws Exception {
        User farmer = saveUser("Farmer J3", "farmer.j3@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer J3", "buyer.j3@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Tomato Batch J3");

        Long orderId = createAcceptedOrder(listing.getId(), buyer, farmer);

        jdbcTemplate.update(
                "update orders set created_at = ? where id = ?",
                Timestamp.from(Instant.now().minusSeconds(60L * 60L * 24L * 31L)),
                orderId
        );

        orderAutoCompleteJob.autoCompleteOrders();

        Order updated = orderRepository.findById(orderId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(updated.getCompletedAt()).isNotNull();
    }

    private Long createAcceptedOrder(Long listingId, User buyer, User farmer) {
        // Minimal direct path: create negotiation and then accept it via repositories/jobs-independent flow.
        Negotiation negotiation = new Negotiation();
        negotiation.setListing(listingRepository.findById(listingId).orElseThrow());
        negotiation.setBuyer(buyer);
        negotiation.setFarmer(farmer);
        negotiation.setOfferedPrice(new BigDecimal("25.00"));
        negotiation.setRequestedQuantity(new BigDecimal("10.00"));
        negotiation.setProposedBy(com.directharvest.backend.common.enums.ProposedBy.BUYER);
        negotiation.setStatus(NegotiationStatus.ACCEPTED);
        negotiation.setExpiresAt(Instant.now().plusSeconds(60L * 60L * 24L * 3L));
        negotiation = negotiationRepository.save(negotiation);

        Order order = new Order();
        order.setListing(negotiation.getListing());
        order.setNegotiation(negotiation);
        order.setBuyer(buyer);
        order.setFarmer(farmer);
        order.setAgreedPrice(new BigDecimal("25.00"));
        order.setAgreedQuantity(new BigDecimal("10.00"));
        order.setStatus(OrderStatus.CONFIRMED);
        order = orderRepository.save(order);

        return order.getId();
    }

    private Listing saveListing(User farmer, String title) {
        Listing listing = new Listing();
        listing.setFarmer(farmer);
        listing.setCropName(title);
        listing.setQuantity(new BigDecimal("50.00"));
        listing.setInitialQuantity(new BigDecimal("50.00"));
        listing.setPricePerKg(new BigDecimal("35.00"));
        listing.setStreet("Test Street 1");
        listing.setCity("Surat");
        listing.setState("Gujarat");
        listing.setPincode("395007");
        listing.setStatus(ListingStatus.ACTIVE);
        return listingRepository.save(listing);
    }

    private User saveUser(String name, String email, UserRole role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setRole(role);
        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @SuppressWarnings("unused")
    private String bearerToken(User user) {
        CustomUserDetails details = new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.isEnabled()
        );
        return "Bearer " + jwtService.generateAccessToken(details);
    }
}

