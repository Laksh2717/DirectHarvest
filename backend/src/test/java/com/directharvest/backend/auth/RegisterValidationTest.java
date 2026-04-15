package com.directharvest.backend.auth;

import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.users.entity.User;
import com.directharvest.backend.users.repository.UserRepository;
import com.directharvest.backend.security.jwt.JwtService;
import com.directharvest.backend.security.service.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RegisterValidationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Autowired
    private com.directharvest.backend.listings.repository.ListingRepository listingRepository;

    @BeforeEach
    void cleanState() {
      listingRepository.deleteAll();
      userRepository.deleteAll();
    }

    // Test 1: Name should not be empty
    @Test
    void register_withEmptyName_returnsBadRequest() throws Exception {
        String body = """
                {
                  "name": "",
                  "email": "test@example.com",
                  "password": "ValidPassword123",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "FARMER"
                }
                """;

        HttpResponse<String> response = send("POST", "/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Name is required");
    }

    @Test
    void register_withBlankName_returnsBadRequest() throws Exception {
        String body = """
                {
                  "name": "   ",
                  "email": "test@example.com",
                  "password": "ValidPassword123",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "FARMER"
                }
                """;

        HttpResponse<String> response = send("POST", "/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Name is required");
    }

    @Test
    void register_withMissingName_returnsBadRequest() throws Exception {
        String body = """
                {
                  "email": "test@example.com",
                  "password": "ValidPassword123",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "FARMER"
                }
                """;

        HttpResponse<String> response = send("POST", "/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(400);
    }

    // Test 2: Email should be valid
    @Test
    void register_withInvalidEmail_returnsBadRequest() throws Exception {
        String body = """
                {
                  "name": "John Doe",
                  "email": "invalid-email",
                  "password": "ValidPassword123",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "FARMER"
                }
                """;

        HttpResponse<String> response = send("POST", "/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Email is invalid");
    }

    @Test
    void register_withValidEmail_succeeds() throws Exception {
        String body = """
                {
                  "name": "John Doe",
                  "email": "valid.user@example.com",
                  "password": "ValidPassword123",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "FARMER"
                }
                """;

        HttpResponse<String> response = send("POST", "/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("\"accessToken\"");
    }

    // Test 3: Password should be >= 8 characters (note: spec says 6, but code enforces 8)
    @Test
    void register_withShortPassword_returnsBadRequest() throws Exception {
        String body = """
                {
                  "name": "John Doe",
                  "email": "test@example.com",
                  "password": "Pass12",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "FARMER"
                }
                """;

        HttpResponse<String> response = send("POST", "/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Password must be between 8 and 100 characters");
    }

    @Test
    void register_withValidPassword_succeeds() throws Exception {
        String body = """
                {
                  "name": "John Doe",
                  "email": "valid@example.com",
                  "password": "ValidPassword123",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "FARMER"
                }
                """;

        HttpResponse<String> response = send("POST", "/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(201);
    }

    // Test 4: Role should be FARMER or BUYER (not ADMIN or invalid)
    @Test
    void register_withADMINRole_returnsBadRequest() throws Exception {
        String body = """
                {
                  "name": "John Doe",
                  "email": "admin@example.com",
                  "password": "ValidPassword123",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "ADMIN"
                }
                """;

        HttpResponse<String> response = send("POST", "/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("ADMIN registration is not allowed");
    }

    @Test
    void register_withFARMERRole_succeeds() throws Exception {
        String body = """
                {
                  "name": "Farmer John",
                  "email": "farmer@example.com",
                  "password": "ValidPassword123",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "FARMER"
                }
                """;

        HttpResponse<String> response = send("POST", "/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(201);
        User savedUser = userRepository.findByEmailIgnoreCase("farmer@example.com").orElseThrow();
        assertThat(savedUser.getRole()).isEqualTo(UserRole.FARMER);
    }

    @Test
    void register_withBUYERRole_succeeds() throws Exception {
        String body = """
                {
                  "name": "Buyer Jane",
                  "email": "buyer@example.com",
                  "password": "ValidPassword123",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "BUYER"
                }
                """;

        HttpResponse<String> response = send("POST", "/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(201);
        User savedUser = userRepository.findByEmailIgnoreCase("buyer@example.com").orElseThrow();
        assertThat(savedUser.getRole()).isEqualTo(UserRole.BUYER);
    }

    @Test
    void register_withMissingRole_returnsBadRequest() throws Exception {
        String body = """
                {
                  "name": "John Doe",
                  "email": "test@example.com",
                  "password": "ValidPassword123"
                }
                """;

        HttpResponse<String> response = send("POST", "/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(400);
    }

    // Test 5: Password is stored in hashed form (not plaintext)
    @Test
    void register_passwordStoredAsHash() throws Exception {
        String plainPassword = "MySecurePassword123";
        String body = """
                {
                  "name": "Hash Test",
                  "email": "hash@example.com",
                  "password": "%s",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "FARMER"
                }
                """.formatted(plainPassword);

        HttpResponse<String> response = send("POST", "/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(201);

        User savedUser = userRepository.findByEmailIgnoreCase("hash@example.com").orElseThrow();
        String storedPassword = savedUser.getPassword();

        // Verify password is NOT plaintext
        assertThat(storedPassword).isNotEqualTo(plainPassword);

        // Verify password IS hashed (BCrypt format)
        assertThat(storedPassword).startsWith("$2");

        // Verify BCrypt can verify the plaintext against the hash
        assertThat(passwordEncoder.matches(plainPassword, storedPassword)).isTrue();
    }

    // Test 6: Email should be unique
    @Test
    void register_withDuplicateEmail_returnsConflict() throws Exception {
        String firstBody = """
                {
                  "name": "First User",
                  "email": "duplicate@example.com",
                  "password": "ValidPassword123",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "FARMER"
                }
                """;

        HttpResponse<String> firstResponse = send("POST", "/auth/register", firstBody);
        assertThat(firstResponse.statusCode()).isEqualTo(201);

        String secondBody = """
                {
                  "name": "Second User",
                  "email": "duplicate@example.com",
                  "password": "ValidPassword123",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "BUYER"
                }
                """;

        HttpResponse<String> secondResponse = send("POST", "/auth/register", secondBody);

        assertThat(secondResponse.statusCode()).isEqualTo(409);
        assertThat(secondResponse.body()).contains("Email is already registered");
    }

    @Test
    void register_withCaseInsensitiveDuplicateEmail_returnsConflict() throws Exception {
        String firstBody = """
                {
                  "name": "First User",
                  "email": "CaseTest@EXAMPLE.COM",
                  "password": "ValidPassword123",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "FARMER"
                }
                """;

        HttpResponse<String> firstResponse = send("POST", "/auth/register", firstBody);
        assertThat(firstResponse.statusCode()).isEqualTo(201);

        String secondBody = """
                {
                  "name": "Second User",
                  "email": "casetest@example.com",
                  "password": "ValidPassword123",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "BUYER"
                }
                """;

        HttpResponse<String> secondResponse = send("POST", "/auth/register", secondBody);

        assertThat(secondResponse.statusCode()).isEqualTo(409);
        assertThat(secondResponse.body()).contains("Email is already registered");
    }

    // Test 7: Valid successful registration
    @Test
    void register_withValidData_succeeds() throws Exception {
        String body = """
                {
                  "name": "Valid User",
                  "email": "valid.user@test.com",
                  "password": "SecurePassword123",
                  "street": "Test Street",
                  "city": "Surat",
                  "state": "Gujarat",
                  "pincode": "395007",
                  "role": "FARMER"
                }
                """;

        HttpResponse<String> response = send("POST", "/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("\"accessToken\"");
        assertThat(response.body()).contains("\"refreshToken\"");

        User savedUser = userRepository.findByEmailIgnoreCase("valid.user@test.com").orElseThrow();
        assertThat(savedUser.getName()).isEqualTo("Valid User");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.FARMER);
        assertThat(savedUser.isEnabled()).isTrue();
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path));

        if (body != null) {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        HttpRequest request = builder.build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

