package com.directharvest.backend.orders;

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
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NegotiationRepository negotiationRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    void getMyOrders_returnsOrderCreatedAfterNegotiationAccept() throws Exception {
        User farmer = saveUser("Farmer O1", "farmer.o1@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer O1", "buyer.o1@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Onion Batch");

        Long negotiationId = createAndAcceptNegotiation(listing.getId(), buyer, farmer);

        assertThat(orderRepository.findAll()).hasSize(1);
        assertThat(orderRepository.findAll().getFirst().getNegotiation().getId()).isEqualTo(negotiationId);

        HttpResponse<String> response = send("GET", "/orders/me", null, bearerToken(buyer));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"CONFIRMED\"");
        assertThat(response.body()).contains("\"listingTitle\":\"Onion Batch\"");
        assertThat(response.body()).contains("\"agreedQuantity\":20.00");
    }

    @Test
    void getMyOrders_withAdminToken_returnsForbidden() throws Exception {
        User admin = saveUser("Admin O1", "admin.o1@test.com", UserRole.ADMIN);

        HttpResponse<String> response = send("GET", "/orders/me", null, bearerToken(admin));

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).contains("Access Denied");
    }

    @Test
    void getMyOrders_withStatusFilter_returnsOnlyMatchingOrders() throws Exception {
        User farmer = saveUser("Farmer Filter O", "farmer.filter.o@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Filter O", "buyer.filter.o@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Filtered Onion Batch");

        createAndAcceptNegotiation(listing.getId(), buyer, farmer);
        Order order = orderRepository.findAll().getFirst();
        order.setStatus(OrderStatus.ACTIVE);
        orderRepository.save(order);

        HttpResponse<String> response = send("GET", "/orders/me?status=ACTIVE", null, bearerToken(buyer));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"ACTIVE\"");
        assertThat(response.body()).doesNotContain("\"status\":\"CONFIRMED\"");
    }

    @Test
    void getMyOrders_withoutStatusFilter_returnsAllStatuses() throws Exception {
        User farmer = saveUser("Farmer All O", "farmer.all.o@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer All O", "buyer.all.o@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "All Onion Batch");

        createAndAcceptNegotiation(listing.getId(), buyer, farmer);
        Order order = orderRepository.findAll().getFirst();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        HttpResponse<String> response = send("GET", "/orders/me", null, bearerToken(buyer));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"CANCELLED\"");
    }

    @Test
    void getOrderById_allowsBuyerAndFarmer() throws Exception {
        User farmer = saveUser("Farmer Detail O", "farmer.detail.o@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Detail O", "buyer.detail.o@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Detail Wheat Batch");

        createAndAcceptNegotiation(listing.getId(), buyer, farmer);
        Order order = orderRepository.findAll().getFirst();

        HttpResponse<String> buyerResponse = send("GET", "/orders/" + order.getId(), null, bearerToken(buyer));
        HttpResponse<String> farmerResponse = send("GET", "/orders/" + order.getId(), null, bearerToken(farmer));

        assertThat(buyerResponse.statusCode()).isEqualTo(200);
        assertThat(farmerResponse.statusCode()).isEqualTo(200);
        assertThat(buyerResponse.body()).contains("\"listingTitle\":\"Detail Wheat Batch\"");
        assertThat(buyerResponse.body()).doesNotContain("\"negotiation\"");
    }

    @Test
    void completeEndpoint_marksOrderCompletedByBuyerFromConfirmed() throws Exception {
        User farmer = saveUser("Farmer O2", "farmer.o2@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer O2", "buyer.o2@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Wheat Batch");

        createAndAcceptNegotiation(listing.getId(), buyer, farmer);
        Order order = orderRepository.findAll().getFirst();

        HttpResponse<String> completedResponse = send("POST", "/orders/" + order.getId() + "/complete", null, bearerToken(buyer));

        assertThat(completedResponse.statusCode()).isEqualTo(200);
        assertThat(completedResponse.body()).contains("\"status\":\"COMPLETED\"");
        assertThat(completedResponse.body()).contains("\"completedAt\":");
    }

    @Test
    void completeEndpoint_withFarmerToken_returnsForbidden() throws Exception {
        User farmer = saveUser("Farmer O3", "farmer.o3@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer O3", "buyer.o3@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Corn Batch");

        createAndAcceptNegotiation(listing.getId(), buyer, farmer);
        Order order = orderRepository.findAll().getFirst();

        HttpResponse<String> response = send("POST", "/orders/" + order.getId() + "/complete", null, bearerToken(farmer));

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).contains("Only buyer can mark an order as completed");
    }

    @Test
    void cancelEndpoint_after24Hours_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer O4", "farmer.o4@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer O4", "buyer.o4@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Cabbage Batch");

        createAndAcceptNegotiation(listing.getId(), buyer, farmer);
        Order order = orderRepository.findAll().getFirst();

        jdbcTemplate.update(
                "update orders set created_at = ? where id = ?",
                Timestamp.from(Instant.now().minusSeconds(60L * 60L * 25L)),
                order.getId()
        );

        HttpResponse<String> response = send("POST", "/orders/" + order.getId() + "/cancel", null, bearerToken(buyer));

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Cancellation window is over");
    }

    @Test
    void cancelEndpoint_whenOrderActive_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer O5", "farmer.o5@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer O5", "buyer.o5@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Lemon Batch");

        createAndAcceptNegotiation(listing.getId(), buyer, farmer);
        Order order = orderRepository.findAll().getFirst();
        order.setStatus(com.directharvest.backend.common.enums.OrderStatus.ACTIVE);
        orderRepository.save(order);

        HttpResponse<String> response = send("POST", "/orders/" + order.getId() + "/cancel", null, bearerToken(farmer));

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Cannot cancel order in status ACTIVE");
    }

    @Test
    void cancelEndpoint_cancelledByBuyer_setsCancellationMetadata() throws Exception {
        User farmer = saveUser("Farmer O6", "farmer.o6@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer O6", "buyer.o6@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Pepper Batch");

        createAndAcceptNegotiation(listing.getId(), buyer, farmer);
        Order order = orderRepository.findAll().getFirst();

        HttpResponse<String> response = send(
                "POST",
                "/orders/" + order.getId() + "/cancel",
                null,
                bearerToken(buyer)
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"CANCELLED\"");
        assertThat(response.body()).contains("\"cancelledBy\":\"BUYER\"");
        assertThat(response.body()).contains("\"cancelledAt\":");

        Listing refreshedListing = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(refreshedListing.getQuantity()).isEqualByComparingTo("50.00");
    }

    @Test
    void cancelEndpoint_restoresOutOfStockListingToActive() throws Exception {
        User farmer = saveUser("Farmer O6A", "farmer.o6a@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer O6A", "buyer.o6a@test.com", UserRole.BUYER);

        Listing listing = new Listing();
        listing.setFarmer(farmer);
        listing.setCropName("Stock Reset Batch");
        listing.setQuantity(new BigDecimal("20.00"));
        listing.setInitialQuantity(new BigDecimal("20.00"));
        listing.setPricePerKg(new BigDecimal("35.00"));
        listing.setStreet("Test Street 1");
        listing.setCity("Surat");
        listing.setState("Gujarat");
        listing.setPincode("395007");
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        createAndAcceptNegotiation(listing.getId(), buyer, farmer);
        Order order = orderRepository.findAll().getFirst();

        Listing afterAccept = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(afterAccept.getQuantity()).isEqualByComparingTo("0.00");
        assertThat(afterAccept.getStatus()).isEqualTo(ListingStatus.OUT_OF_STOCK);

        HttpResponse<String> response = send(
                "POST",
                "/orders/" + order.getId() + "/cancel",
                null,
                bearerToken(buyer)
        );

        assertThat(response.statusCode()).isEqualTo(200);

        Listing afterCancel = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(afterCancel.getQuantity()).isEqualByComparingTo("20.00");
        assertThat(afterCancel.getStatus()).isEqualTo(ListingStatus.ACTIVE);
    }

    @Test
    void cancelEndpoint_withReason_persistsCancellationReason() throws Exception {
        User farmer = saveUser("Farmer O6R", "farmer.o6r@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer O6R", "buyer.o6r@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Pepper Batch Reason");

        createAndAcceptNegotiation(listing.getId(), buyer, farmer);
        Order order = orderRepository.findAll().getFirst();

        HttpResponse<String> response = send(
                "POST",
                "/orders/" + order.getId() + "/cancel",
                """
                        {
                          "cancellationReason": "Buyer will pick up tomorrow"
                        }
                        """,
                bearerToken(buyer)
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"cancelledReason\":\"Buyer will pick up tomorrow\"");
    }

    @Test
    void cancelEndpoint_cancelledByFarmer_setsCancellationMetadata() throws Exception {
        User farmer = saveUser("Farmer O7", "farmer.o7@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer O7", "buyer.o7@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Tomato Batch 2");

        createAndAcceptNegotiation(listing.getId(), buyer, farmer);
        Order order = orderRepository.findAll().getFirst();

        HttpResponse<String> response = send(
                "POST",
                "/orders/" + order.getId() + "/cancel",
                null,
                bearerToken(farmer)
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"CANCELLED\"");
        assertThat(response.body()).contains("\"cancelledBy\":\"FARMER\"");
        assertThat(response.body()).contains("\"cancelledAt\":");
    }

    private Long createAndAcceptNegotiation(Long listingId, User buyer, User farmer) throws Exception {
        HttpResponse<String> createResponse = send(
                "POST",
                "/negotiations",
                """
                        {
                          "listingId": %d,
                          "offeredPrice": 28.50,
                          "requestedQuantity": 20.00
                        }
                        """.formatted(listingId),
                bearerToken(buyer)
        );
        assertThat(createResponse.statusCode()).isEqualTo(201);

        Long negotiationId = extractFirstId(createResponse.body());

        HttpResponse<String> acceptResponse = send(
                "POST",
                "/negotiations/" + negotiationId + "/accept",
                null,
                bearerToken(farmer)
        );
        assertThat(acceptResponse.statusCode()).isEqualTo(200);

        return negotiationId;
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
            case "PATCH" -> builder.method("PATCH", HttpRequest.BodyPublishers.ofString(body == null ? "" : body)).build();
            case "GET" -> builder.GET().build();
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}


