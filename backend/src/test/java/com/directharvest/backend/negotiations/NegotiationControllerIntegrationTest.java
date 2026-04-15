package com.directharvest.backend.negotiations;

import com.directharvest.backend.common.enums.AuthProvider;
import com.directharvest.backend.common.enums.ListingStatus;
import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.listings.entity.Listing;
import com.directharvest.backend.listings.repository.ListingRepository;
import com.directharvest.backend.negotiations.entity.Negotiation;
import com.directharvest.backend.negotiations.repository.NegotiationRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NegotiationControllerIntegrationTest {

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
    void createCounterAndAccept_flowCompletesWithExpectedStatuses() throws Exception {
        User farmer = saveUser("Farmer A", "farmerA@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer A", "buyerA@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Tomato Lot");

        HttpResponse<String> createResponse = send(
                "POST",
                "/negotiations",
                """
                        {
                          "listingId": %d,
                          "offeredPrice": 40.00,
                          "requestedQuantity": 20.00
                        }
                        """.formatted(listing.getId()),
                bearerToken(buyer)
        );

        assertThat(createResponse.statusCode()).isEqualTo(201);
        assertThat(createResponse.body()).contains("\"status\":\"PENDING_FARMER\"");
        assertThat(createResponse.body()).contains("\"proposedBy\":\"BUYER\"");
        assertThat(createResponse.body()).contains("\"requestedQuantity\":20.00");

        Negotiation negotiation = negotiationRepository.findAll().getFirst();

        HttpResponse<String> counterResponse = send(
                "POST",
                "/negotiations/" + negotiation.getId() + "/counter",
                """
                        {
                          "offeredPrice": 42.50,
                          "requestedQuantity": 18.00
                        }
                        """,
                bearerToken(farmer)
        );

        assertThat(counterResponse.statusCode()).isEqualTo(200);
        assertThat(counterResponse.body()).contains("\"status\":\"PENDING_BUYER\"");
        assertThat(counterResponse.body()).contains("\"proposedBy\":\"FARMER\"");
        assertThat(counterResponse.body()).contains("\"offeredPrice\":42.50");
        assertThat(counterResponse.body()).contains("\"requestedQuantity\":18.00");

        HttpResponse<String> acceptResponse = send(
                "POST",
                "/negotiations/" + negotiation.getId() + "/accept",
                null,
                bearerToken(buyer)
        );

        assertThat(acceptResponse.statusCode()).isEqualTo(200);
        assertThat(acceptResponse.body()).contains("\"status\":\"ACCEPTED\"");

        Listing persistedListing = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(persistedListing.getQuantity()).isEqualByComparingTo("32.00");
        assertThat(persistedListing.getStatus()).isEqualTo(ListingStatus.ACTIVE);

        HttpResponse<String> historyResponse = send(
                "GET",
                "/negotiations/" + negotiation.getId() + "/history",
                null,
                bearerToken(buyer)
        );

        assertThat(historyResponse.statusCode()).isEqualTo(200);
        assertThat(historyResponse.body()).contains("\"eventType\":\"CREATED\"");
        assertThat(historyResponse.body()).contains("\"eventType\":\"COUNTERED\"");
        assertThat(historyResponse.body()).contains("\"eventType\":\"ACCEPTED\"");
        assertThat(historyResponse.body()).contains("\"requestedQuantity\":20.00");
        assertThat(historyResponse.body()).contains("\"requestedQuantity\":18.00");

        int createdIndex = historyResponse.body().indexOf("\"eventType\":\"CREATED\"");
        int counteredIndex = historyResponse.body().indexOf("\"eventType\":\"COUNTERED\"");
        int acceptedIndex = historyResponse.body().indexOf("\"eventType\":\"ACCEPTED\"");
        assertThat(createdIndex).isLessThan(counteredIndex);
        assertThat(counteredIndex).isLessThan(acceptedIndex);
    }

    @Test
    void accept_withExactQuantity_depletesListingAndMarksOutOfStock() throws Exception {
        User farmer = saveUser("Farmer Exact", "farmer.exact@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Exact", "buyer.exact@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Rice Lot");

        HttpResponse<String> createResponse = send(
                "POST",
                "/negotiations",
                """
                        {
                          "listingId": %d,
                          "offeredPrice": 50.00,
                          "requestedQuantity": 50.00
                        }
                        """.formatted(listing.getId()),
                bearerToken(buyer)
        );

        assertThat(createResponse.statusCode()).isEqualTo(201);
        Long negotiationId = negotiationRepository.findAll().getFirst().getId();

        HttpResponse<String> acceptResponse = send(
                "POST",
                "/negotiations/" + negotiationId + "/accept",
                null,
                bearerToken(farmer)
        );

        assertThat(acceptResponse.statusCode()).isEqualTo(200);
        Listing persistedListing = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(persistedListing.getQuantity()).isEqualByComparingTo("0.00");
        assertThat(persistedListing.getStatus()).isEqualTo(ListingStatus.OUT_OF_STOCK);
    }

    @Test
    void accept_whenAvailableQuantityIsTooLow_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer Low Stock", "farmer.low.stock@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Low Stock", "buyer.low.stock@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Maize Lot");

        HttpResponse<String> createResponse = send(
                "POST",
                "/negotiations",
                """
                        {
                          "listingId": %d,
                          "offeredPrice": 44.00,
                          "requestedQuantity": 30.00
                        }
                        """.formatted(listing.getId()),
                bearerToken(buyer)
        );
        assertThat(createResponse.statusCode()).isEqualTo(201);

        Long negotiationId = negotiationRepository.findAll().getFirst().getId();
        listing.setQuantity(new BigDecimal("20.00"));
        listingRepository.save(listing);

        HttpResponse<String> acceptResponse = send(
                "POST",
                "/negotiations/" + negotiationId + "/accept",
                null,
                bearerToken(farmer)
        );

        assertThat(acceptResponse.statusCode()).isEqualTo(400);
        assertThat(acceptResponse.body()).contains("Not enough listing quantity available to accept negotiation");

        Listing persistedListing = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(persistedListing.getQuantity()).isEqualByComparingTo("20.00");
        assertThat(persistedListing.getStatus()).isEqualTo(ListingStatus.ACTIVE);
        Negotiation persistedNegotiation = negotiationRepository.findById(negotiationId).orElseThrow();
        assertThat(persistedNegotiation.getStatus()).isEqualTo(com.directharvest.backend.common.enums.NegotiationStatus.PENDING_FARMER);
        assertThat(orderRepository.findAll()).isEmpty();
    }

    @Test
    void accept_whenNotUsersTurn_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer B", "farmerB@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer B", "buyerB@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Potato Lot");

        send(
                "POST",
                "/negotiations",
                """
                        {
                          "listingId": %d,
                          "offeredPrice": 30.00,
                          "requestedQuantity": 15.00
                        }
                        """.formatted(listing.getId()),
                bearerToken(buyer)
        );

        Long negotiationId = negotiationRepository.findAll().getFirst().getId();

        HttpResponse<String> response = send(
                "POST",
                "/negotiations/" + negotiationId + "/accept",
                null,
                bearerToken(buyer)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("cannot accept");
    }

    @Test
    void createNegotiation_withRequestedQuantityGreaterThanListing_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer Qty Limit", "farmer.qty.limit@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Qty Limit", "buyer.qty.limit@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Onion Lot");

        HttpResponse<String> response = send(
                "POST",
                "/negotiations",
                """
                        {
                          "listingId": %d,
                          "offeredPrice": 34.00,
                          "requestedQuantity": 60.00
                        }
                        """.formatted(listing.getId()),
                bearerToken(buyer)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Requested quantity cannot exceed available listing quantity");
    }

    @Test
    void createNegotiation_withZeroRequestedQuantity_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer Qty Zero", "farmer.qty.zero@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Qty Zero", "buyer.qty.zero@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Potato Lot");

        HttpResponse<String> response = send(
                "POST",
                "/negotiations",
                """
                        {
                          "listingId": %d,
                          "offeredPrice": 31.00,
                          "requestedQuantity": 0
                        }
                        """.formatted(listing.getId()),
                bearerToken(buyer)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Requested quantity must be greater than 0");
    }

    @Test
    void createNegotiation_withInactiveListing_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer Inactive", "farmer.inactive@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Inactive", "buyer.inactive@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Inactive Lot");
        listing.setStatus(ListingStatus.INACTIVE);
        listingRepository.save(listing);

        HttpResponse<String> response = send(
                "POST",
                "/negotiations",
                """
                        {
                          "listingId": %d,
                          "offeredPrice": 30.00,
                          "requestedQuantity": 10.00
                        }
                        """.formatted(listing.getId()),
                bearerToken(buyer)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Negotiation can only be created for ACTIVE listing");
    }

    @Test
    void createNegotiation_withExistingActiveNegotiationForSameBuyerAndListing_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer Duplicate", "farmer.duplicate@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Duplicate", "buyer.duplicate@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Duplicate Lot");

        HttpResponse<String> first = send(
                "POST",
                "/negotiations",
                """
                        {
                          "listingId": %d,
                          "offeredPrice": 29.00,
                          "requestedQuantity": 12.00
                        }
                        """.formatted(listing.getId()),
                bearerToken(buyer)
        );
        assertThat(first.statusCode()).isEqualTo(201);

        HttpResponse<String> second = send(
                "POST",
                "/negotiations",
                """
                        {
                          "listingId": %d,
                          "offeredPrice": 28.50,
                          "requestedQuantity": 11.00
                        }
                        """.formatted(listing.getId()),
                bearerToken(buyer)
        );

        assertThat(second.statusCode()).isEqualTo(400);
        assertThat(second.body()).contains("An active negotiation already exists for this listing");
    }

    @Test
    void counterOffer_withRequestedQuantityGreaterThanListing_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer Counter Qty", "farmer.counter.qty@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Counter Qty", "buyer.counter.qty@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Counter Qty Lot");

        HttpResponse<String> create = send(
                "POST",
                "/negotiations",
                """
                        {
                          "listingId": %d,
                          "offeredPrice": 36.00,
                          "requestedQuantity": 20.00
                        }
                        """.formatted(listing.getId()),
                bearerToken(buyer)
        );
        assertThat(create.statusCode()).isEqualTo(201);

        Long negotiationId = negotiationRepository.findAll().getFirst().getId();
        HttpResponse<String> counter = send(
                "POST",
                "/negotiations/" + negotiationId + "/counter",
                """
                        {
                          "offeredPrice": 37.00,
                          "requestedQuantity": 70.00
                        }
                        """,
                bearerToken(farmer)
        );

        assertThat(counter.statusCode()).isEqualTo(400);
        assertThat(counter.body()).contains("Requested quantity cannot exceed available listing quantity");
    }

    @Test
    void counterOffer_withZeroRequestedQuantity_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer Counter Zero", "farmer.counter.zero@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Counter Zero", "buyer.counter.zero@test.com", UserRole.BUYER);
        Listing listing = saveListing(farmer, "Counter Zero Lot");

        HttpResponse<String> create = send(
                "POST",
                "/negotiations",
                """
                        {
                          "listingId": %d,
                          "offeredPrice": 36.00,
                          "requestedQuantity": 20.00
                        }
                        """.formatted(listing.getId()),
                bearerToken(buyer)
        );
        assertThat(create.statusCode()).isEqualTo(201);

        Long negotiationId = negotiationRepository.findAll().getFirst().getId();
        HttpResponse<String> counter = send(
                "POST",
                "/negotiations/" + negotiationId + "/counter",
                """
                        {
                          "offeredPrice": 37.00,
                          "requestedQuantity": 0
                        }
                        """,
                bearerToken(farmer)
        );

        assertThat(counter.statusCode()).isEqualTo(400);
        assertThat(counter.body()).contains("Requested quantity must be greater than 0");
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

