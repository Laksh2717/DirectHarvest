package com.directharvest.backend.ratings;

import com.directharvest.backend.common.enums.AuthProvider;
import com.directharvest.backend.common.enums.ListingStatus;
import com.directharvest.backend.common.enums.OrderStatus;
import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.listings.entity.Listing;
import com.directharvest.backend.listings.repository.ListingRepository;
import com.directharvest.backend.negotiations.repository.NegotiationRepository;
import com.directharvest.backend.orders.entity.Order;
import com.directharvest.backend.orders.repository.OrderRepository;
import com.directharvest.backend.ratings.repository.RatingRepository;
import com.directharvest.backend.security.jwt.JwtService;
import com.directharvest.backend.security.service.CustomUserDetails;
import com.directharvest.backend.users.entity.User;
import com.directharvest.backend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RatingControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private NegotiationRepository negotiationRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private JwtService jwtService;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void cleanState() {
        ratingRepository.deleteAll();
        orderRepository.deleteAll();
        negotiationRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createRating_onCompletedOrder_andFetchUserRatings() throws Exception {
        User farmer = saveUser("Farmer R1", "farmer.r1@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer R1", "buyer.r1@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Grapes Batch");

        Order order = createAcceptedOrder(listing, buyer, farmer);
        order.setStatus(OrderStatus.COMPLETED);
        order = orderRepository.save(order);

        HttpResponse<String> createResponse = send(
                "POST",
                "/orders/" + order.getId() + "/rating",
                """
                        {
                          "score": 5
                        }
                        """,
                bearerToken(buyer)
        );

        assertThat(createResponse.statusCode()).isEqualTo(201);
        assertThat(createResponse.body()).contains("\"score\":5");
        assertThat(createResponse.body()).contains("\"farmerId\":" + farmer.getId());
        assertThat(createResponse.body()).contains("\"listingId\":" + listing.getId());

        User updatedFarmer = userRepository.findById(farmer.getId()).orElseThrow();
        assertThat(updatedFarmer.getRatingCount()).isEqualTo(1);
        assertThat(updatedFarmer.getAverageRating()).isEqualByComparingTo("5.00");

        HttpResponse<String> getResponse = send(
                "GET",
                "/users/" + farmer.getId() + "/ratings",
                null,
                bearerToken(farmer)
        );

        assertThat(getResponse.statusCode()).isEqualTo(200);
        assertThat(getResponse.body()).contains("\"raterId\":" + buyer.getId());
        assertThat(getResponse.body()).contains("\"farmerId\":" + farmer.getId());
    }

    @Test
    void createRating_onNonCompletedOrder_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer R2", "farmer.r2@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer R2", "buyer.r2@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Rice Batch");

        Order order = createAcceptedOrder(listing, buyer, farmer);

        HttpResponse<String> response = send(
                "POST",
                "/orders/" + order.getId() + "/rating",
                """
                        {
                          "score": 4
                        }
                        """,
                bearerToken(buyer)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("only for COMPLETED orders");
    }

    @Test
    void createRating_byFarmer_returnsForbidden() throws Exception {
        User farmer = saveUser("Farmer R2F", "farmer.r2f@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer R2F", "buyer.r2f@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Rice Batch F");

        Order order = createAcceptedOrder(listing, buyer, farmer);
        order.setStatus(OrderStatus.COMPLETED);
        order = orderRepository.save(order);

        HttpResponse<String> response = send(
                "POST",
                "/orders/" + order.getId() + "/rating",
                """
                        {
                          "score": 4
                        }
                        """,
                bearerToken(farmer)
        );

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).contains("Only buyer can rate the farmer");
    }

    @Test
    void createRating_duplicateBySameBuyerToSameFarmerForSameListing_succeedsForDifferentOrders() throws Exception {
        User farmer = saveUser("Farmer R3", "farmer.r3@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer R3", "buyer.r3@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Maize Batch");

        Order firstOrder = createAcceptedOrder(listing, buyer, farmer);
        firstOrder.setStatus(OrderStatus.COMPLETED);
        firstOrder = orderRepository.save(firstOrder);

        HttpResponse<String> first = send(
                "POST",
                "/orders/" + firstOrder.getId() + "/rating",
                """
                        {
                          "score": 5
                        }
                        """,
                bearerToken(buyer)
        );
        assertThat(first.statusCode()).isEqualTo(201);

        // Different order for the same listing should allow a new rating
        Order secondOrder = createAcceptedOrder(listing, buyer, farmer);
        secondOrder.setStatus(OrderStatus.COMPLETED);
        secondOrder = orderRepository.save(secondOrder);

        HttpResponse<String> second = send(
                "POST",
                "/orders/" + secondOrder.getId() + "/rating",
                """
                        {
                          "score": 4
                        }
                        """,
                bearerToken(buyer)
        );

        assertThat(second.statusCode()).isEqualTo(201);
    }

    @Test
    void createRating_duplicateForSameOrder_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer R3B", "farmer.r3b@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer R3B", "buyer.r3b@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Wheat Batch");

        Order order = createAcceptedOrder(listing, buyer, farmer);
        order.setStatus(OrderStatus.COMPLETED);
        order = orderRepository.save(order);

        HttpResponse<String> first = send(
                "POST",
                "/orders/" + order.getId() + "/rating",
                """
                        {
                          "score": 5
                        }
                        """,
                bearerToken(buyer)
        );
        assertThat(first.statusCode()).isEqualTo(201);

        // Attempting to rate the same order again should fail
        HttpResponse<String> second = send(
                "POST",
                "/orders/" + order.getId() + "/rating",
                """
                        {
                          "score": 4
                        }
                        """,
                bearerToken(buyer)
        );

        assertThat(second.statusCode()).isEqualTo(400);
        assertThat(second.body()).contains("already rated this farmer for this order");
    }

    @Test
    void createRating_updatesFarmerAverageAndCountAcrossMultipleOrders() throws Exception {
        User farmer = saveUser("Farmer R4", "farmer.r4@test.com", UserRole.FARMER);
        User buyer1 = saveUser("Buyer R4A", "buyer.r4a@test.com", UserRole.BUYER);
        User buyer2 = saveUser("Buyer R4B", "buyer.r4b@test.com", UserRole.BUYER);
        Listing listing1 = saveListing(farmer, "Cotton Batch 1");
        Listing listing2 = saveListing(farmer, "Cotton Batch 2");

        Order order1 = createAcceptedOrder(listing1, buyer1, farmer);
        order1.setStatus(OrderStatus.COMPLETED);
        order1 = orderRepository.save(order1);

        Order order2 = createAcceptedOrder(listing2, buyer2, farmer);
        order2.setStatus(OrderStatus.COMPLETED);
        order2 = orderRepository.save(order2);

        HttpResponse<String> first = send(
                "POST",
                "/orders/" + order1.getId() + "/rating",
                """
                        {
                          "score": 5
                        }
                        """,
                bearerToken(buyer1)
        );
        assertThat(first.statusCode()).isEqualTo(201);

        HttpResponse<String> second = send(
                "POST",
                "/orders/" + order2.getId() + "/rating",
                """
                        {
                          "score": 3
                        }
                        """,
                bearerToken(buyer2)
        );
        assertThat(second.statusCode()).isEqualTo(201);

        User updatedFarmer = userRepository.findById(farmer.getId()).orElseThrow();
        assertThat(updatedFarmer.getRatingCount()).isEqualTo(2);
        assertThat(updatedFarmer.getAverageRating()).isEqualByComparingTo("4.00");
    }

    private Order createAcceptedOrder(Listing listing, User buyer, User farmer) throws Exception {
        HttpResponse<String> createNegotiationResponse = send(
                "POST",
                "/negotiations",
                """
                        {
                          "listingId": %d,
                          "offeredPrice": 33.00,
                          "requestedQuantity": 15.00
                        }
                        """.formatted(listing.getId()),
                bearerToken(buyer)
        );
        assertThat(createNegotiationResponse.statusCode()).isEqualTo(201);

        Long negotiationId = extractFirstId(createNegotiationResponse.body());
        assertThat(negotiationId).isNotNull();

        HttpResponse<String> acceptResponse = send(
                "POST",
                "/negotiations/" + negotiationId + "/accept",
                null,
                bearerToken(farmer)
        );
        assertThat(acceptResponse.statusCode()).isEqualTo(200);

        return orderRepository.findAll().stream().max(Comparator.comparing(Order::getId)).orElseThrow();
    }

    private Long extractFirstId(String json) {
        int index = json.indexOf("\"id\":");
        if (index < 0) {
            return null;
        }
        int start = index + 5;
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        if (end == start) {
            return null;
        }
        return Long.parseLong(json.substring(start, end));
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

    private HttpResponse<String> send(String method, String path, String body, String bearerToken) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Authorization", bearerToken);

        if (body != null) {
            builder.header("Content-Type", "application/json");
        }

        HttpRequest request = switch (method) {
            case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body)).build();
            case "GET" -> builder.GET().build();
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}


