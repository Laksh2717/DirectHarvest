package com.directharvest.backend.auth;

import com.directharvest.backend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthCookieIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();
    }

    @Test
    void register_setsCookies_andProtectedEndpointUsesCookies() throws Exception {
        HttpResponse<String> registerResponse = postJson(
                "/auth/register",
                """
                        {
                          "name": "Cookie Farmer",
                          "email": "cookie.farmer@test.com",
                          "password": "CookiePass123",
                                                    "street": "Plot 17, Ring Road",
                                                    "city": "Surat",
                                                    "state": "Gujarat",
                                                    "pincode": "395007",
                          "role": "FARMER"
                        }
                        """
        );

        assertThat(registerResponse.statusCode()).isEqualTo(201);
        assertThat(registerResponse.headers().allValues("Set-Cookie")).hasSizeGreaterThanOrEqualTo(2);
        assertThat(registerResponse.body()).contains("\"accessToken\"");

        HttpResponse<String> protectedResponse = send("GET", "/listings/me", null);
        assertThat(protectedResponse.statusCode()).isEqualTo(200);
        assertThat(protectedResponse.body()).contains("[");
    }

    @Test
    void refreshAndLogout_workWithCookiesOnly() throws Exception {
        HttpResponse<String> registerResponse = postJson(
                "/auth/register",
                """
                        {
                          "name": "Cookie Buyer",
                          "email": "cookie.buyer@test.com",
                          "password": "CookiePass123",
                                                    "street": "Plot 18, Ring Road",
                                                    "city": "Surat",
                                                    "state": "Gujarat",
                                                    "pincode": "395007",
                          "role": "BUYER"
                        }
                        """
        );

        assertThat(registerResponse.statusCode()).isEqualTo(201);

        HttpResponse<String> refreshResponse = send("POST", "/auth/refresh", null);
        assertThat(refreshResponse.statusCode()).isEqualTo(200);
        assertThat(refreshResponse.body()).contains("\"accessToken\"");

        HttpResponse<String> logoutResponse = send("POST", "/auth/logout", null);
        assertThat(logoutResponse.statusCode()).isEqualTo(200);
        assertThat(logoutResponse.body()).contains("Logged out successfully");

        HttpResponse<String> protectedResponse = send("GET", "/listings/me", null);
        assertThat(protectedResponse.statusCode()).isEqualTo(401);
    }

    private HttpResponse<String> postJson(String path, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        return httpClient.send(builder.POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path));
        HttpRequest request = switch (method) {
            case "GET" -> builder.GET().build();
            case "POST" -> builder.POST(body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body)).build();
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

