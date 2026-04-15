package com.directharvest.backend.users;

import com.directharvest.backend.common.enums.AuthProvider;
import com.directharvest.backend.common.enums.ListingStatus;
import com.directharvest.backend.common.enums.NegotiationStatus;
import com.directharvest.backend.common.enums.ProposedBy;
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
import java.time.Instant;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserProfileControllerIntegrationTest {

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
    void getMyAddress_returnsOnlyAddressFields() throws Exception {
        User user = new User();
        user.setName("Address User");
        user.setEmail("address.user@test.com");
        user.setPassword("encoded-password");
        user.setRole(UserRole.FARMER);
        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(true);
        user.setStreet("Street 12");
        user.setCity("Surat");
        user.setState("Gujarat");
        user.setPincode("395007");
        user = userRepository.save(user);

        HttpResponse<String> response = sendGet("/users/me/address", bearerToken(user));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"street\":\"Street 12\"");
        assertThat(response.body()).contains("\"city\":\"Surat\"");
        assertThat(response.body()).contains("\"state\":\"Gujarat\"");
        assertThat(response.body()).contains("\"pincode\":\"395007\"");
        assertThat(response.body()).doesNotContain("\"email\"");
        assertThat(response.body()).doesNotContain("\"name\"");
    }

    @Test
    void getMyProfile_forFarmer_includesRatingFields() throws Exception {
        User farmer = new User();
        farmer.setName("Farmer One");
        farmer.setEmail("farmer.profile@test.com");
        farmer.setPassword("encoded-password");
        farmer.setRole(UserRole.FARMER);
        farmer.setProvider(AuthProvider.LOCAL);
        farmer.setEnabled(true);
        farmer.setStreet("Street A");
        farmer.setCity("Surat");
        farmer.setState("Gujarat");
        farmer.setPincode("395007");
        farmer.setAverageRating(new BigDecimal("4.50"));
        farmer.setRatingCount(6);
        farmer = userRepository.save(farmer);

        HttpResponse<String> response = sendGet("/users/me", bearerToken(farmer));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"name\":\"Farmer One\"");
        assertThat(response.body()).contains("\"email\":\"farmer.profile@test.com\"");
        assertThat(response.body()).contains("\"street\":\"Street A\"");
        assertThat(response.body()).contains("\"averageRating\":4.50");
        assertThat(response.body()).contains("\"ratingCount\":6");
    }

    @Test
    void getMyProfile_forBuyer_hasNullRatingFields() throws Exception {
        User buyer = new User();
        buyer.setName("Buyer One");
        buyer.setEmail("buyer.profile@test.com");
        buyer.setPassword("encoded-password");
        buyer.setRole(UserRole.BUYER);
        buyer.setProvider(AuthProvider.LOCAL);
        buyer.setEnabled(true);
        buyer.setStreet("Street B");
        buyer.setCity("Ahmedabad");
        buyer.setState("Gujarat");
        buyer.setPincode("380001");
        buyer = userRepository.save(buyer);

        HttpResponse<String> response = sendGet("/users/me", bearerToken(buyer));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"name\":\"Buyer One\"");
        assertThat(response.body()).contains("\"averageRating\":null");
        assertThat(response.body()).contains("\"ratingCount\":null");
    }

    @Test
    void updateMyProfile_withValidPayload_updatesUserFields() throws Exception {
        User buyer = new User();
        buyer.setName("Buyer Old");
        buyer.setEmail("buyer.old@test.com");
        buyer.setPassword("encoded-password");
        buyer.setRole(UserRole.BUYER);
        buyer.setProvider(AuthProvider.LOCAL);
        buyer.setEnabled(true);
        buyer.setStreet("Old Street");
        buyer.setCity("Old City");
        buyer.setState("Old State");
        buyer.setPincode("395007");
        buyer = userRepository.save(buyer);

        HttpResponse<String> response = sendPatch(
                "/users/me",
                """
                        {
                          "name": "Buyer New",
                          "email": "buyer.new@test.com",
                          "street": "New Street",
                          "city": "New City",
                          "state": "New State",
                          "pincode": "380001"
                        }
                        """,
                bearerToken(buyer)
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"name\":\"Buyer New\"");
        assertThat(response.body()).contains("\"email\":\"buyer.new@test.com\"");
        assertThat(response.body()).contains("\"pincode\":\"380001\"");

        User persisted = userRepository.findById(buyer.getId()).orElseThrow();
        assertThat(persisted.getName()).isEqualTo("Buyer New");
        assertThat(persisted.getEmail()).isEqualTo("buyer.new@test.com");
        assertThat(persisted.getStreet()).isEqualTo("New Street");
        assertThat(persisted.getCity()).isEqualTo("New City");
        assertThat(persisted.getState()).isEqualTo("New State");
        assertThat(persisted.getPincode()).isEqualTo("380001");
    }

    @Test
    void updateMyProfile_withExistingEmail_returnsConflict() throws Exception {
        User current = new User();
        current.setName("Current User");
        current.setEmail("current.user@test.com");
        current.setPassword("encoded-password");
        current.setRole(UserRole.FARMER);
        current.setProvider(AuthProvider.LOCAL);
        current.setEnabled(true);
        current.setStreet("Street C");
        current.setCity("Surat");
        current.setState("Gujarat");
        current.setPincode("395007");
        current = userRepository.save(current);

        User another = new User();
        another.setName("Another User");
        another.setEmail("already.used@test.com");
        another.setPassword("encoded-password");
        another.setRole(UserRole.BUYER);
        another.setProvider(AuthProvider.LOCAL);
        another.setEnabled(true);
        another.setStreet("Street D");
        another.setCity("Rajkot");
        another.setState("Gujarat");
        another.setPincode("360001");
        userRepository.save(another);

        HttpResponse<String> response = sendPatch(
                "/users/me",
                """
                        {
                          "name": "Updated Name",
                          "email": "already.used@test.com",
                          "street": "New Street",
                          "city": "New City",
                          "state": "New State",
                          "pincode": "380001"
                        }
                        """,
                bearerToken(current)
        );

        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(response.body()).contains("Email is already registered");
    }

    @Test
    void updateMyProfile_withInvalidPincode_returnsBadRequest() throws Exception {
        User user = new User();
        user.setName("Pincode User");
        user.setEmail("pincode.user@test.com");
        user.setPassword("encoded-password");
        user.setRole(UserRole.BUYER);
        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(true);
        user.setStreet("Street E");
        user.setCity("Vadodara");
        user.setState("Gujarat");
        user.setPincode("390001");
        user = userRepository.save(user);

        HttpResponse<String> response = sendPatch(
                "/users/me",
                """
                        {
                          "name": "Updated Name",
                          "email": "updated.user@test.com",
                          "street": "New Street",
                          "city": "New City",
                          "state": "New State",
                          "pincode": "12345"
                        }
                        """,
                bearerToken(user)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Pincode must be a valid Indian pincode (6 digits)");
    }

    @Test
    void updateMyProfile_whenBuyerHasActiveNegotiation_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer Negotiation", "farmer.neg@test.com", UserRole.FARMER, "395007");
        User buyer = saveUser("Buyer Negotiation", "buyer.neg@test.com", UserRole.BUYER, "380001");

        Listing listing = new Listing();
        listing.setFarmer(farmer);
        listing.setCropName("Wheat");
        listing.setQuantity(new BigDecimal("40.00"));
        listing.setInitialQuantity(new BigDecimal("40.00"));
        listing.setPricePerKg(new BigDecimal("22.00"));
        listing.setStreet("Farm Street");
        listing.setCity("Surat");
        listing.setState("Gujarat");
        listing.setPincode("395007");
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        Negotiation negotiation = new Negotiation();
        negotiation.setListing(listing);
        negotiation.setFarmer(farmer);
        negotiation.setBuyer(buyer);
        negotiation.setOfferedPrice(new BigDecimal("20.00"));
        negotiation.setRequestedQuantity(new BigDecimal("10.00"));
        negotiation.setStatus(NegotiationStatus.PENDING_FARMER);
        negotiation.setProposedBy(ProposedBy.BUYER);
        negotiation.setExpiresAt(Instant.now().plusSeconds(86_400));
        negotiationRepository.save(negotiation);

        HttpResponse<String> response = sendPatch(
                "/users/me",
                """
                        {
                          "name": "Buyer Updated",
                          "email": "buyer.updated@test.com",
                          "street": "New Street",
                          "city": "New City",
                          "state": "New State",
                          "pincode": "380001"
                        }
                        """,
                bearerToken(buyer)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Cannot update profile while active negotiations exist");
    }

    @Test
    void updateMyProfile_whenFarmerHasActiveNegotiation_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer Active", "farmer.active@test.com", UserRole.FARMER, "395007");
        User buyer = saveUser("Buyer Active", "buyer.active@test.com", UserRole.BUYER, "380001");

        Listing listing = new Listing();
        listing.setFarmer(farmer);
        listing.setCropName("Rice");
        listing.setQuantity(new BigDecimal("60.00"));
        listing.setInitialQuantity(new BigDecimal("60.00"));
        listing.setPricePerKg(new BigDecimal("30.00"));
        listing.setStreet("Farmer Street");
        listing.setCity("Surat");
        listing.setState("Gujarat");
        listing.setPincode("395007");
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        Negotiation negotiation = new Negotiation();
        negotiation.setListing(listing);
        negotiation.setFarmer(farmer);
        negotiation.setBuyer(buyer);
        negotiation.setOfferedPrice(new BigDecimal("28.00"));
        negotiation.setRequestedQuantity(new BigDecimal("15.00"));
        negotiation.setStatus(NegotiationStatus.PENDING_BUYER);
        negotiation.setProposedBy(ProposedBy.FARMER);
        negotiation.setExpiresAt(Instant.now().plusSeconds(86_400));
        negotiationRepository.save(negotiation);

        HttpResponse<String> response = sendPatch(
                "/users/me",
                """
                        {
                          "name": "Farmer Updated",
                          "email": "farmer.updated@test.com",
                          "street": "New Street",
                          "city": "New City",
                          "state": "New State",
                          "pincode": "395007"
                        }
                        """,
                bearerToken(farmer)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Cannot update profile while active negotiations exist");
    }

    private User saveUser(String name, String email, UserRole role, String pincode) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setRole(role);
        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(true);
        user.setStreet("Default Street");
        user.setCity("Default City");
        user.setState("Default State");
        user.setPincode(pincode);
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

    private HttpResponse<String> sendGet(String path, String bearerToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Authorization", bearerToken)
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPatch(String path, String body, String bearerToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Authorization", bearerToken)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
