package com.directharvest.backend.listings;

import com.directharvest.backend.common.enums.AuthProvider;
import com.directharvest.backend.common.enums.ListingStatus;
import com.directharvest.backend.common.enums.NegotiationStatus;
import com.directharvest.backend.common.enums.ProposedBy;
import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.listings.entity.Listing;
import com.directharvest.backend.listings.entity.ListingImage;
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
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ListingControllerIntegrationTest {

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
    void createListing_withFarmerToken_returnsCreatedListing() throws Exception {
        User farmer = saveUser("Farmer One", "farmer1@test.com", UserRole.FARMER);

        String body = """
                {
                  "description": "Organic farm fresh",
                  "cropName": "Tomato",
                  "quantity": 120.5,
                  "pricePerKg": 34.75,
                  "street": "Plot 17, Ring Road",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "images": [
                    {
                      "cloudinaryPublicId": "directharvest/listings/tomato-1",
                      "cloudinarySecureUrl": "https://res.cloudinary.com/demo/image/upload/v1/directharvest/listings/tomato-1.jpg",
                      "format": "jpg",
                      "width": 1200,
                      "height": 900,
                      "bytes": 245000,
                      "primary": true
                    }
                  ]
                }
                """;

        HttpResponse<String> response = send("POST", "/listings", body, bearerToken(farmer));

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("\"farmerId\":" + farmer.getId());
        assertThat(response.body()).contains("\"cropName\":\"Tomato\"");
        assertThat(response.body()).contains("\"cloudinaryPublicId\":\"directharvest/listings/tomato-1\"");
        assertThat(response.body()).contains("\"status\":\"ACTIVE\"");
    }

    @Test
    void createListing_withBuyerToken_returnsForbidden() throws Exception {
        User buyer = saveUser("Buyer One", "buyer1@test.com", UserRole.BUYER);

        String body = """
                {
                  "cropName": "Potato",
                  "quantity": 12,
                  "pricePerKg": 20,
                  "street": "Plot 18, Ring Road",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007"
                }
                """;

        HttpResponse<String> response = send("POST", "/listings", body, bearerToken(buyer));

        assertThat(response.statusCode()).isEqualTo(403);
    }

        @Test
        void getListingById_allowsBuyerAndFarmer() throws Exception {
                User farmer = saveUser("Farmer Detail View", "farmer.view@test.com", UserRole.FARMER);
                User buyer = saveUser("Buyer Detail View", "buyer.view@test.com", UserRole.BUYER);

                Listing listing = saveListing(farmer, "Carrot Batch");

                HttpResponse<String> buyerResponse = send("GET", "/listings/" + listing.getId(), null, bearerToken(buyer));
                HttpResponse<String> farmerResponse = send("GET", "/listings/" + listing.getId(), null, bearerToken(farmer));

                assertThat(buyerResponse.statusCode()).isEqualTo(200);
                assertThat(farmerResponse.statusCode()).isEqualTo(200);
                assertThat(buyerResponse.body()).contains("\"cropName\":\"Carrot Batch\"");
                assertThat(buyerResponse.body()).doesNotContain("negotiation");
        }

        @Test
        void getListingsByStatus_whenNoStatusProvided_returnsAllStatuses() throws Exception {
                User farmer = saveUser("Farmer Status View", "farmer.status@test.com", UserRole.FARMER);

                Listing active = saveListing(farmer, "Active Crop");
                Listing inactive = saveListing(farmer, "Inactive Crop");
                inactive.setStatus(ListingStatus.INACTIVE);
                listingRepository.save(inactive);

                HttpResponse<String> response = send("GET", "/listings/me", null, bearerToken(farmer));

                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.body()).contains("Active Crop");
                assertThat(response.body()).contains("Inactive Crop");
        }

        @Test
        void getListingsByStatus_filtersCorrectly() throws Exception {
                User farmer = saveUser("Farmer Status Filter", "farmer.status.filter@test.com", UserRole.FARMER);

                Listing active = saveListing(farmer, "Active Filter Crop");
                Listing inactive = saveListing(farmer, "Inactive Filter Crop");
                inactive.setStatus(ListingStatus.INACTIVE);
                listingRepository.save(inactive);

                // Query for ACTIVE listings only
                HttpResponse<String> response = send("GET", "/listings/me?status=ACTIVE", null, bearerToken(farmer));

                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.body()).contains("Active Filter Crop");
                assertThat(response.body()).doesNotContain("Inactive Filter Crop");
        }

    @Test
    void updateListing_withNonOwnerToken_returnsForbidden() throws Exception {
        User owner = saveUser("Farmer Owner", "owner@test.com", UserRole.FARMER);
        User anotherFarmer = saveUser("Farmer Two", "farmer2@test.com", UserRole.FARMER);

        Listing listing = new Listing();
        listing.setFarmer(owner);
        listing.setCropName("Onion");
        listing.setQuantity(new BigDecimal("50.00"));
        listing.setInitialQuantity(new BigDecimal("50.00"));
        listing.setPricePerKg(new BigDecimal("18.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        String body = """
                {
                  "cropName": "Updated Onion",
                  "description": "Updated harvest",
                  "street": "New Street",
                  "city": "New City",
                  "state": "New State",
                  "pincode": "380001"
                }
                """;

        HttpResponse<String> response = send("PATCH", "/listings/" + listing.getId(), body, bearerToken(anotherFarmer));

        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    void updateListing_withOwnerToken_updatesOnlyAllowedFields() throws Exception {
        User owner = saveUser("Farmer Detail", "detail@test.com", UserRole.FARMER);

        Listing listing = new Listing();
        listing.setFarmer(owner);
        listing.setCropName("Old Crop");
        listing.setQuantity(new BigDecimal("50.00"));
        listing.setInitialQuantity(new BigDecimal("50.00"));
        listing.setPricePerKg(new BigDecimal("18.00"));
        fillRequiredAddress(listing);
        listing.setDescription("Old description");
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        HttpResponse<String> response = send(
                "PATCH",
                "/listings/" + listing.getId(),
                """
                        {
                          "cropName": "Updated Crop",
                          "description": "",
                          "street": "Updated Street",
                          "city": "Updated City",
                          "state": "Updated State",
                          "pincode": "395007"
                        }
                        """,
                bearerToken(owner)
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"cropName\":\"Updated Crop\"");
        assertThat(response.body()).contains("\"street\":\"Updated Street\"");
        assertThat(response.body()).contains("\"city\":\"Updated City\"");
        assertThat(response.body()).contains("\"state\":\"Updated State\"");
        assertThat(response.body()).contains("\"pincode\":\"395007\"");
        assertThat(response.body()).contains("\"pricePerKg\":18.00");

        Listing persisted = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(persisted.getCropName()).isEqualTo("Updated Crop");
        assertThat(persisted.getDescription()).isEmpty();
        assertThat(persisted.getStreet()).isEqualTo("Updated Street");
        assertThat(persisted.getCity()).isEqualTo("Updated City");
        assertThat(persisted.getState()).isEqualTo("Updated State");
        assertThat(persisted.getPincode()).isEqualTo("395007");
        assertThat(persisted.getPricePerKg()).isEqualByComparingTo("18.00");
    }

    @Test
    void updateListing_withInactiveListing_returnsBadRequest() throws Exception {
        User owner = saveUser("Farmer Inactive Detail", "inactive.detail@test.com", UserRole.FARMER);

        Listing listing = new Listing();
        listing.setFarmer(owner);
        listing.setCropName("Old Crop");
        listing.setQuantity(new BigDecimal("50.00"));
        listing.setInitialQuantity(new BigDecimal("50.00"));
        listing.setPricePerKg(new BigDecimal("18.00"));
        fillRequiredAddress(listing);
        listing.setDescription("Old description");
        listing.setStatus(ListingStatus.INACTIVE);
        listing = listingRepository.save(listing);

        HttpResponse<String> response = send(
                "PATCH",
                "/listings/" + listing.getId(),
                """
                        {
                          "cropName": "Updated Crop",
                          "description": "Updated description",
                          "street": "Updated Street",
                          "city": "Updated City",
                          "state": "Updated State",
                          "pincode": "395007"
                        }
                        """,
                bearerToken(owner)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Listing details can only be updated when listing is ACTIVE");

        Listing persisted = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(persisted.getCropName()).isEqualTo("Old Crop");
        assertThat(persisted.getStatus()).isEqualTo(ListingStatus.INACTIVE);
    }

    @Test
    void updateListing_withInvalidPincode_returnsBadRequest() throws Exception {
        User owner = saveUser("Farmer Invalid Pin", "invalid.pin@test.com", UserRole.FARMER);

        Listing listing = new Listing();
        listing.setFarmer(owner);
        listing.setCropName("Beans");
        listing.setQuantity(new BigDecimal("10.00"));
        listing.setInitialQuantity(new BigDecimal("10.00"));
        listing.setPricePerKg(new BigDecimal("14.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        HttpResponse<String> response = send(
                "PATCH",
                "/listings/" + listing.getId(),
                """
                        {
                          "cropName": "Beans Updated",
                          "description": "Updated",
                          "street": "Updated Street",
                          "city": "Updated City",
                          "state": "Updated State",
                          "pincode": "12345"
                        }
                        """,
                bearerToken(owner)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Pincode must be a valid Indian pincode (6 digits)");
    }

    @Test
    void updateListingPrice_withActiveNegotiation_returnsBadRequest() throws Exception {
        User owner = saveUser("Farmer Price", "farmer.price@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Price", "buyer.price@test.com", UserRole.BUYER);

        Listing listing = new Listing();
        listing.setFarmer(owner);
        listing.setCropName("Groundnut");
        listing.setQuantity(new BigDecimal("40.00"));
        listing.setInitialQuantity(new BigDecimal("40.00"));
        listing.setPricePerKg(new BigDecimal("30.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        Negotiation negotiation = new Negotiation();
        negotiation.setListing(listing);
        negotiation.setFarmer(owner);
        negotiation.setBuyer(buyer);
        negotiation.setOfferedPrice(new BigDecimal("28.00"));
        negotiation.setRequestedQuantity(new BigDecimal("10.00"));
        negotiation.setStatus(NegotiationStatus.PENDING_FARMER);
        negotiation.setProposedBy(ProposedBy.BUYER);
        negotiation.setExpiresAt(Instant.now().plusSeconds(86_400));
        negotiationRepository.save(negotiation);

        HttpResponse<String> response = send(
                "PATCH",
                "/listings/" + listing.getId() + "/price",
                """
                        {
                          "pricePerKg": 33.00
                        }
                        """,
                bearerToken(owner)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Cannot modify listing while active negotiations exist");
        Listing persisted = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(persisted.getPricePerKg()).isEqualByComparingTo("30.00");
    }

    @Test
    void updateListing_withActiveNegotiation_returnsBadRequest() throws Exception {
        User owner = saveUser("Farmer Detail Active", "farmer.detail.active@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Detail Active", "buyer.detail.active@test.com", UserRole.BUYER);

        Listing listing = new Listing();
        listing.setFarmer(owner);
        listing.setCropName("Tomato");
        listing.setQuantity(new BigDecimal("20.00"));
        listing.setInitialQuantity(new BigDecimal("20.00"));
        listing.setPricePerKg(new BigDecimal("15.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        Negotiation negotiation = new Negotiation();
        negotiation.setListing(listing);
        negotiation.setFarmer(owner);
        negotiation.setBuyer(buyer);
        negotiation.setOfferedPrice(new BigDecimal("14.00"));
        negotiation.setRequestedQuantity(new BigDecimal("10.00"));
        negotiation.setStatus(NegotiationStatus.PENDING_BUYER);
        negotiation.setProposedBy(ProposedBy.FARMER);
        negotiation.setExpiresAt(Instant.now().plusSeconds(86_400));
        negotiationRepository.save(negotiation);

        HttpResponse<String> response = send(
                "PATCH",
                "/listings/" + listing.getId(),
                """
                        {
                          "cropName": "Updated Tomato",
                          "description": "Updated",
                          "street": "Updated Street",
                          "city": "Updated City",
                          "state": "Updated State",
                          "pincode": "395007"
                        }
                        """,
                bearerToken(owner)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Cannot modify listing while active negotiations exist");
    }

    @Test
    void updateListingPrice_withClosedNegotiation_updatesPrice() throws Exception {
        User owner = saveUser("Farmer Price Closed", "farmer.price.closed@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Price Closed", "buyer.price.closed@test.com", UserRole.BUYER);

        Listing listing = new Listing();
        listing.setFarmer(owner);
        listing.setCropName("Wheat");
        listing.setQuantity(new BigDecimal("75.00"));
        listing.setInitialQuantity(new BigDecimal("75.00"));
        listing.setPricePerKg(new BigDecimal("20.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        Negotiation closedNegotiation = new Negotiation();
        closedNegotiation.setListing(listing);
        closedNegotiation.setFarmer(owner);
        closedNegotiation.setBuyer(buyer);
        closedNegotiation.setOfferedPrice(new BigDecimal("19.00"));
        closedNegotiation.setRequestedQuantity(new BigDecimal("10.00"));
        closedNegotiation.setStatus(NegotiationStatus.REJECTED);
        closedNegotiation.setProposedBy(ProposedBy.FARMER);
        closedNegotiation.setExpiresAt(Instant.now().minusSeconds(60));
        negotiationRepository.save(closedNegotiation);

        HttpResponse<String> response = send(
                "PATCH",
                "/listings/" + listing.getId() + "/price",
                """
                        {
                          "pricePerKg": 21.00
                        }
                        """,
                bearerToken(owner)
        );

        assertThat(response.statusCode()).isEqualTo(200);
        Listing persisted = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(persisted.getPricePerKg()).isEqualByComparingTo("21.00");
    }

    @Test
    void markInactive_withActiveNegotiation_returnsBadRequest() throws Exception {
        User owner = saveUser("Farmer Inactive Active", "farmer.inactive.active@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Inactive Active", "buyer.inactive.active@test.com", UserRole.BUYER);

        Listing listing = new Listing();
        listing.setFarmer(owner);
        listing.setCropName("Onion");
        listing.setQuantity(new BigDecimal("30.00"));
        listing.setInitialQuantity(new BigDecimal("30.00"));
        listing.setPricePerKg(new BigDecimal("11.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        Negotiation negotiation = new Negotiation();
        negotiation.setListing(listing);
        negotiation.setFarmer(owner);
        negotiation.setBuyer(buyer);
        negotiation.setOfferedPrice(new BigDecimal("10.00"));
        negotiation.setRequestedQuantity(new BigDecimal("10.00"));
        negotiation.setStatus(NegotiationStatus.PENDING_FARMER);
        negotiation.setProposedBy(ProposedBy.BUYER);
        negotiation.setExpiresAt(Instant.now().plusSeconds(86_400));
        negotiationRepository.save(negotiation);

        HttpResponse<String> response = send(
                "PATCH",
                "/listings/" + listing.getId() + "/inactive",
                null,
                bearerToken(owner)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Cannot modify listing while active negotiations exist");
        Listing persisted = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(ListingStatus.ACTIVE);
    }

    @Test
    void deleteListing_withActiveNegotiation_returnsBadRequest() throws Exception {
        User owner = saveUser("Farmer Delete Active", "farmer.delete.active@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Delete Active", "buyer.delete.active@test.com", UserRole.BUYER);

        Listing listing = new Listing();
        listing.setFarmer(owner);
        listing.setCropName("Potato");
        listing.setQuantity(new BigDecimal("40.00"));
        listing.setInitialQuantity(new BigDecimal("40.00"));
        listing.setPricePerKg(new BigDecimal("13.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        Negotiation negotiation = new Negotiation();
        negotiation.setListing(listing);
        negotiation.setFarmer(owner);
        negotiation.setBuyer(buyer);
        negotiation.setOfferedPrice(new BigDecimal("12.00"));
        negotiation.setRequestedQuantity(new BigDecimal("10.00"));
        negotiation.setStatus(NegotiationStatus.PENDING_BUYER);
        negotiation.setProposedBy(ProposedBy.BUYER);
        negotiation.setExpiresAt(Instant.now().plusSeconds(86_400));
        negotiationRepository.save(negotiation);

        HttpResponse<String> response = send(
                "DELETE",
                "/listings/" + listing.getId(),
                null,
                bearerToken(owner)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Listing cannot be deleted because there are negotiations/offers for it");
        assertThat(listingRepository.findById(listing.getId())).isPresent();
    }

    @Test
    void updateListingPriceViaDedicatedEndpoint_withNoActiveNegotiation_updatesPrice() throws Exception {
        User owner = saveUser("Farmer Endpoint", "farmer.endpoint@test.com", UserRole.FARMER);

        Listing listing = new Listing();
        listing.setFarmer(owner);
        listing.setCropName("Rice");
        listing.setQuantity(new BigDecimal("90.00"));
        listing.setInitialQuantity(new BigDecimal("90.00"));
        listing.setPricePerKg(new BigDecimal("25.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        HttpResponse<String> response = send(
                "PATCH",
                "/listings/" + listing.getId() + "/price",
                """
                        {
                          "pricePerKg": 27.00
                        }
                        """,
                bearerToken(owner)
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"pricePerKg\":27.00");
        Listing persisted = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(persisted.getPricePerKg()).isEqualByComparingTo("27.00");
    }

    @Test
    void updateListingPriceViaDedicatedEndpoint_withInactiveListing_returnsBadRequest() throws Exception {
        User owner = saveUser("Farmer Endpoint Inactive", "farmer.endpoint.inactive@test.com", UserRole.FARMER);

        Listing listing = new Listing();
        listing.setFarmer(owner);
        listing.setCropName("Rice");
        listing.setQuantity(new BigDecimal("90.00"));
        listing.setInitialQuantity(new BigDecimal("90.00"));
        listing.setPricePerKg(new BigDecimal("25.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.INACTIVE);
        listing = listingRepository.save(listing);

        HttpResponse<String> response = send(
                "PATCH",
                "/listings/" + listing.getId() + "/price",
                """
                        {
                          "pricePerKg": 27.00
                        }
                        """,
                bearerToken(owner)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Listing price can only be updated when listing is ACTIVE");
        Listing persisted = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(persisted.getPricePerKg()).isEqualByComparingTo("25.00");
    }

    @Test
    void updateListingPriceViaDedicatedEndpoint_withActiveNegotiation_returnsBadRequest() throws Exception {
        User owner = saveUser("Farmer Endpoint Active", "farmer.endpoint.active@test.com", UserRole.FARMER);
        User buyer = saveUser("Buyer Endpoint Active", "buyer.endpoint.active@test.com", UserRole.BUYER);

        Listing listing = new Listing();
        listing.setFarmer(owner);
        listing.setCropName("Corn");
        listing.setQuantity(new BigDecimal("45.00"));
        listing.setInitialQuantity(new BigDecimal("45.00"));
        listing.setPricePerKg(new BigDecimal("16.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        Negotiation negotiation = new Negotiation();
        negotiation.setListing(listing);
        negotiation.setFarmer(owner);
        negotiation.setBuyer(buyer);
        negotiation.setOfferedPrice(new BigDecimal("15.00"));
        negotiation.setRequestedQuantity(new BigDecimal("10.00"));
        negotiation.setStatus(NegotiationStatus.PENDING_BUYER);
        negotiation.setProposedBy(ProposedBy.FARMER);
        negotiation.setExpiresAt(Instant.now().plusSeconds(86_400));
        negotiationRepository.save(negotiation);

        HttpResponse<String> response = send(
                "PATCH",
                "/listings/" + listing.getId() + "/price",
                """
                        {
                          "pricePerKg": 18.00
                        }
                        """,
                bearerToken(owner)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Cannot modify listing while active negotiations exist");
        Listing persisted = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(persisted.getPricePerKg()).isEqualByComparingTo("16.00");
    }

    @Test
    void getMyListings_returnsCurrentFarmerListingsOnly() throws Exception {
        User owner = saveUser("Farmer One", "me@test.com", UserRole.FARMER);
        User another = saveUser("Farmer Two", "other@test.com", UserRole.FARMER);

        listingRepository.save(buildListing(owner, "Apple Batch"));
        listingRepository.save(buildListing(another, "Mango Batch"));

        HttpResponse<String> response = send("GET", "/listings/me", null, bearerToken(owner));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"cropName\":\"Apple Batch\"");
        assertThat(response.body()).contains("\"farmerId\":" + owner.getId());
        assertThat(response.body()).doesNotContain("Mango Batch");
    }

        @Test
        void getMyListings_withBuyerToken_returnsForbidden() throws Exception {
                User buyer = saveUser("Buyer My Listings", "buyer.my@test.com", UserRole.BUYER);

                HttpResponse<String> response = send("GET", "/listings/me", null, bearerToken(buyer));

                assertThat(response.statusCode()).isEqualTo(403);
                assertThat(response.body()).contains("Only FARMER users can create listings");
        }

        @Test
        void browseListings_withoutSearch_returnsPaginatedResults() throws Exception {
                User farmer = saveUser("Farmer Browse", "farmer.browse@test.com", UserRole.FARMER);
                User buyer = saveUser("Buyer Browse", "buyer.browse@test.com", UserRole.BUYER);

                for (int i = 1; i <= 11; i++) {
                        listingRepository.save(buildListing(farmer, "Crop " + i));
                }

                HttpResponse<String> response = send("GET", "/listings/browse", null, bearerToken(buyer));

                assertThat(response.statusCode()).isEqualTo(200);
                // First page returns limited results (size 12 per spec)
                assertThat(response.body()).contains("Crop 11");
                assertThat(response.body()).contains("Crop 2");
        }

        @Test
        void browseListings_withSearch_filtersAcrossTextFields() throws Exception {
                User farmer = saveUser("Farmer Search", "farmer.search@test.com", UserRole.FARMER);
                User buyer = saveUser("Buyer Search", "buyer.search@test.com", UserRole.BUYER);

                Listing tomato = buildListing(farmer, "Tomato Batch");
                tomato.setDescription("Fresh red harvest");
                tomato.setCity("Surat");
                listingRepository.save(tomato);

                Listing wheat = buildListing(farmer, "Wheat Batch");
                wheat.setDescription("Bulk grain stock");
                wheat.setStreet("Village Road");
                wheat.setCity("Ahmedabad");
                listingRepository.save(wheat);

                HttpResponse<String> response = send("GET", "/listings/browse?search=Tomato", null, bearerToken(buyer));

                assertThat(response.statusCode()).isEqualTo(200);
                // Search matches crop name (case-insensitive)
                assertThat(response.body()).contains("Tomato Batch");
                // Wheat Batch should not match when searching for 'Tomato'
                assertThat(response.body()).doesNotContain("Wheat Batch");
        }

        @Test
        void browseListings_isPublicEndpoint() throws Exception {
                User farmer = saveUser("Farmer Browse", "farmer.browse@test.com", UserRole.FARMER);
                User buyer = saveUser("Buyer Browse", "buyer.browse@test.com", UserRole.BUYER);

                saveListing(farmer, "Available Crop");

                // Browse endpoint is public - both farmers and buyers can access it
                HttpResponse<String> response1 = send("GET", "/listings/browse", null, bearerToken(farmer));
                HttpResponse<String> response2 = send("GET", "/listings/browse", null, bearerToken(buyer));

                assertThat(response1.statusCode()).isEqualTo(200);
                assertThat(response2.statusCode()).isEqualTo(200);
        }

    @Test
    void addQuantity_withOutOfStockListing_succeeds() throws Exception {
        User farmer = saveUser("Farmer Qty", "qty@test.com", UserRole.FARMER);

        Listing listing = new Listing();
        listing.setFarmer(farmer);
        listing.setCropName("Carrot");
        listing.setQuantity(new BigDecimal("0.00"));
        listing.setInitialQuantity(new BigDecimal("0.00"));
        listing.setPricePerKg(new BigDecimal("12.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.OUT_OF_STOCK);
        listing = listingRepository.save(listing);

        HttpResponse<String> response = send(
                "POST",
                "/listings/" + listing.getId() + "/quantity/add",
                """
                        {
                          "quantity": 25.50
                        }
                        """,
                bearerToken(farmer)
        );

        assertThat(response.statusCode()).isEqualTo(200);

        Listing persisted = listingRepository.findById(listing.getId()).orElseThrow();
    }

    @Test
    void addQuantity_withActiveListing_incrementsAndKeepsActiveStatus() throws Exception {
        User farmer = saveUser("Farmer Active Qty", "active.qty@test.com", UserRole.FARMER);

        Listing listing = new Listing();
        listing.setFarmer(farmer);
        listing.setCropName("Spinach");
        listing.setQuantity(new BigDecimal("10.00"));
        listing.setInitialQuantity(new BigDecimal("10.00"));
        listing.setPricePerKg(new BigDecimal("11.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        HttpResponse<String> response = send(
                "POST",
                "/listings/" + listing.getId() + "/quantity/add",
                """
                        {
                          "quantity": 7.25
                        }
                        """,
                bearerToken(farmer)
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"quantity\":17.25");
        assertThat(response.body()).contains("\"status\":\"ACTIVE\"");
    }

    @Test
    void addQuantity_withInactiveListing_returnsBadRequest() throws Exception {
        User farmer = saveUser("Farmer Inactive Qty", "inactive.qty@test.com", UserRole.FARMER);

        Listing listing = new Listing();
        listing.setFarmer(farmer);
        listing.setCropName("Peas");
        listing.setQuantity(new BigDecimal("4.00"));
        listing.setInitialQuantity(new BigDecimal("4.00"));
        listing.setPricePerKg(new BigDecimal("13.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.INACTIVE);
        listing = listingRepository.save(listing);

        HttpResponse<String> response = send(
                "POST",
                "/listings/" + listing.getId() + "/quantity/add",
                """
                        {
                          "quantity": 3.00
                        }
                        """,
                bearerToken(farmer)
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Quantity can only be added when listing is ACTIVE");

        Listing persisted = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(persisted.getQuantity()).isEqualByComparingTo("4.00");
        assertThat(persisted.getStatus()).isEqualTo(ListingStatus.INACTIVE);
    }

    @Test
    void addQuantity_withNonOwnerToken_returnsForbidden() throws Exception {
        User owner = saveUser("Farmer Owner Qty", "owner.qty@test.com", UserRole.FARMER);
        User anotherFarmer = saveUser("Farmer Other Qty", "other.qty@test.com", UserRole.FARMER);

        Listing listing = new Listing();
        listing.setFarmer(owner);
        listing.setCropName("Bean");
        listing.setQuantity(new BigDecimal("10.00"));
        listing.setInitialQuantity(new BigDecimal("10.00"));
        listing.setPricePerKg(new BigDecimal("9.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        HttpResponse<String> response = send(
                "POST",
                "/listings/" + listing.getId() + "/quantity/add",
                """
                        {
                          "quantity": 5.00
                        }
                        """,
                bearerToken(anotherFarmer)
        );

        assertThat(response.statusCode()).isEqualTo(403);
    }

                @Test
                void addImages_withOwnerToken_appendsImages() throws Exception {
                                User farmer = saveUser("Farmer Image Add", "image.add@test.com", UserRole.FARMER);

                                Listing listing = new Listing();
                                listing.setFarmer(farmer);
                                listing.setCropName("Tomato");
                                listing.setQuantity(new BigDecimal("25.00"));
                                listing.setInitialQuantity(new BigDecimal("25.00"));
                                listing.setPricePerKg(new BigDecimal("20.00"));
                                fillRequiredAddress(listing);
                                listing.setStatus(ListingStatus.ACTIVE);

                                ListingImage existing = new ListingImage();
                                existing.setCloudinaryPublicId("directharvest/listings/existing-1");
                                existing.setCloudinarySecureUrl("https://res.cloudinary.com/demo/image/upload/v1/directharvest/listings/existing-1.jpg");
                                existing.setFormat("jpg");
                                existing.setPrimary(true);
                                listing.addImage(existing);

                                listing = listingRepository.save(listing);

                                HttpResponse<String> response = send(
                                                                "POST",
                                                                "/listings/" + listing.getId() + "/images",
                                                                """
                                                                                                {
                                                                                                        "images": [
                                                                                                                {
                                                                                                                        "cloudinaryPublicId": "directharvest/listings/new-1",
                                                                                                                        "cloudinarySecureUrl": "https://res.cloudinary.com/demo/image/upload/v1/directharvest/listings/new-1.jpg",
                                                                                                                        "format": "jpg",
                                                                                                                        "width": 1000,
                                                                                                                        "height": 800,
                                                                                                                        "bytes": 150000,
                                                                                                                        "primary": false
                                                                                                                }
                                                                                                        ]
                                                                                                }
                                                                                                """,
                                                                bearerToken(farmer)
                                );

                                assertThat(response.statusCode()).isEqualTo(200);
                                assertThat(response.body()).contains("\"cloudinaryPublicId\":\"directharvest/listings/new-1\"");

                                Listing persisted = listingRepository.findWithImagesById(listing.getId()).orElseThrow();
                                assertThat(persisted.getImages()).hasSize(2);
                                assertThat(persisted.getImages().stream().filter(ListingImage::isPrimary).count()).isEqualTo(1);
                }

                @Test
                void addImages_withIncomingPrimary_switchesPrimaryImage() throws Exception {
                                User farmer = saveUser("Farmer Image Primary", "image.primary@test.com", UserRole.FARMER);

                                Listing listing = new Listing();
                                listing.setFarmer(farmer);
                                listing.setCropName("Potato");
                                listing.setQuantity(new BigDecimal("18.00"));
                                listing.setInitialQuantity(new BigDecimal("18.00"));
                                listing.setPricePerKg(new BigDecimal("14.00"));
                                fillRequiredAddress(listing);
                                listing.setStatus(ListingStatus.ACTIVE);

                                ListingImage existing = new ListingImage();
                                existing.setCloudinaryPublicId("directharvest/listings/primary-old");
                                existing.setCloudinarySecureUrl("https://res.cloudinary.com/demo/image/upload/v1/directharvest/listings/primary-old.jpg");
                                existing.setFormat("jpg");
                                existing.setPrimary(true);
                                listing.addImage(existing);

                                listing = listingRepository.save(listing);

                                HttpResponse<String> response = send(
                                                                "POST",
                                                                "/listings/" + listing.getId() + "/images",
                                                                """
                                                                                                {
                                                                                                        "images": [
                                                                                                                {
                                                                                                                        "cloudinaryPublicId": "directharvest/listings/primary-new",
                                                                                                                        "cloudinarySecureUrl": "https://res.cloudinary.com/demo/image/upload/v1/directharvest/listings/primary-new.jpg",
                                                                                                                        "format": "jpg",
                                                                                                                        "primary": true
                                                                                                                }
                                                                                                        ]
                                                                                                }
                                                                                                """,
                                                                bearerToken(farmer)
                                );

                                assertThat(response.statusCode()).isEqualTo(200);

                                Listing persisted = listingRepository.findWithImagesById(listing.getId()).orElseThrow();
                                assertThat(persisted.getImages()).hasSize(2);
                                assertThat(persisted.getImages().stream()
                                                                .filter(ListingImage::isPrimary)
                                                                .map(ListingImage::getCloudinaryPublicId))
                                                                .containsExactly("directharvest/listings/primary-new");
                }

                @Test
                void removeImage_withOwnerToken_deletesImageAndPromotesAnotherPrimary() throws Exception {
                                User farmer = saveUser("Farmer Image Remove", "image.remove@test.com", UserRole.FARMER);

                                Listing listing = new Listing();
                                listing.setFarmer(farmer);
                                listing.setCropName("Onion");
                                listing.setQuantity(new BigDecimal("35.00"));
                                listing.setInitialQuantity(new BigDecimal("35.00"));
                                listing.setPricePerKg(new BigDecimal("16.00"));
                                fillRequiredAddress(listing);
                                listing.setStatus(ListingStatus.ACTIVE);

                                ListingImage first = new ListingImage();
                                first.setCloudinaryPublicId("directharvest/listings/remove-1");
                                first.setCloudinarySecureUrl("https://res.cloudinary.com/demo/image/upload/v1/directharvest/listings/remove-1.jpg");
                                first.setFormat("jpg");
                                first.setPrimary(true);
                                listing.addImage(first);

                                ListingImage second = new ListingImage();
                                second.setCloudinaryPublicId("directharvest/listings/remove-2");
                                second.setCloudinarySecureUrl("https://res.cloudinary.com/demo/image/upload/v1/directharvest/listings/remove-2.jpg");
                                second.setFormat("jpg");
                                second.setPrimary(false);
                                listing.addImage(second);

                                listing = listingRepository.save(listing);
                                Long imageToDeleteId = listing.getImages().get(0).getId();

                                HttpResponse<String> response = send(
                                                                "DELETE",
                                                                "/listings/" + listing.getId() + "/images/" + imageToDeleteId,
                                                                null,
                                                                bearerToken(farmer)
                                );

                                assertThat(response.statusCode()).isEqualTo(200);
                                assertThat(response.body()).doesNotContain("directharvest/listings/remove-1");

                                Listing persisted = listingRepository.findWithImagesById(listing.getId()).orElseThrow();
                                assertThat(persisted.getImages()).hasSize(1);
                                assertThat(persisted.getImages().get(0).getCloudinaryPublicId()).isEqualTo("directharvest/listings/remove-2");
                                assertThat(persisted.getImages().get(0).isPrimary()).isTrue();
                }

    private Listing buildListing(User farmer, String title) {
        Listing listing = new Listing();
        listing.setFarmer(farmer);
        listing.setCropName(title);
        listing.setQuantity(new BigDecimal("25.00"));
        listing.setInitialQuantity(new BigDecimal("25.00"));
        listing.setPricePerKg(new BigDecimal("10.00"));
        fillRequiredAddress(listing);
        listing.setStatus(ListingStatus.ACTIVE);
        return listing;
    }

        private Listing saveListing(User farmer, String title) {
                return listingRepository.save(buildListing(farmer, title));
        }

    private void fillRequiredAddress(Listing listing) {
        listing.setStreet("Test Street 1");
        listing.setCity("Surat");
        listing.setState("Gujarat");
        listing.setPincode("395007");
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
            case "DELETE" -> builder.DELETE().build();
            case "GET" -> builder.GET().build();
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

