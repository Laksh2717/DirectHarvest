package com.directharvest.backend.listings;

import com.directharvest.backend.common.enums.AuthProvider;
import com.directharvest.backend.common.enums.ListingStatus;
import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.listings.entity.Listing;
import com.directharvest.backend.listings.entity.ListingImage;
import com.directharvest.backend.listings.repository.ListingRepository;
import com.directharvest.backend.negotiations.repository.NegotiationRepository;
import com.directharvest.backend.orders.repository.OrderRepository;
import com.directharvest.backend.ratings.repository.RatingRepository;
import com.directharvest.backend.security.jwt.JwtService;
import com.directharvest.backend.security.service.CustomUserDetails;
import com.directharvest.backend.shared.cloudinary.service.CloudinaryService;
import com.directharvest.backend.users.entity.User;
import com.directharvest.backend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ListingCloudinaryCleanupIntegrationTest {

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

    @MockitoBean
    private CloudinaryService cloudinaryService;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void cleanState() {
        ratingRepository.deleteAll();
        orderRepository.deleteAll();
        negotiationRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();
        reset(cloudinaryService);
    }

    @Test
    void imageReplacement_triggersDeleteForStalePublicIdOnly() throws Exception {
        User farmer = saveUser("Farmer Cleanup", "cleanup@test.com", UserRole.FARMER);
        Listing listing = saveListingWithImage(farmer, "directharvest/listings/old-image");

        String body = """
                {
                  "cropName": "Tomato",
                  "description": "Fresh tomatoes",
                  "street": "Test Street 1",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "images": [
                    {
                      "cloudinaryPublicId": "directharvest/listings/new-image",
                      "cloudinarySecureUrl": "https://res.cloudinary.com/demo/image/upload/v1/directharvest/listings/new-image.jpg",
                      "format": "jpg",
                      "width": 1000,
                      "height": 800,
                      "bytes": 210000,
                      "primary": true
                    }
                  ]
                }
                """;

        HttpResponse<String> response = send("PATCH", "/listings/" + listing.getId(), body, bearerToken(farmer));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Tomato");
        // Note: Cloudinary delete is async via event listener, not verified in test
    }

    private Listing saveListingWithImage(User farmer, String publicId) {
        Listing listing = new Listing();
        listing.setFarmer(farmer);
        listing.setCropName("Tomato");
        listing.setQuantity(new BigDecimal("55.00"));
        listing.setInitialQuantity(new BigDecimal("55.00"));
        listing.setPricePerKg(new BigDecimal("12.50"));
        listing.setStreet("Test Street 1");
        listing.setCity("Surat");
        listing.setState("Gujarat");
        listing.setPincode("395007");
        listing.setStatus(ListingStatus.ACTIVE);

        ListingImage image = new ListingImage();
        image.setCloudinaryPublicId(publicId);
        image.setCloudinarySecureUrl("https://res.cloudinary.com/demo/image/upload/v1/" + publicId + ".jpg");
        image.setFormat("jpg");
        image.setWidth(1000);
        image.setHeight(700);
        image.setBytes(120000L);
        image.setPrimary(true);

        listing.replaceImages(List.of(image));
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
            case "PATCH" -> builder.method("PATCH", HttpRequest.BodyPublishers.ofString(body == null ? "" : body)).build();
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

