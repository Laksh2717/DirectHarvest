package com.directharvest.backend.auth.service;

import com.directharvest.backend.common.exception.BadRequestException;
import com.directharvest.backend.common.exception.UnauthorizedException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class GoogleIdTokenVerifierImpl implements GoogleIdTokenVerifier {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String googleClientId;

    public GoogleIdTokenVerifierImpl(@Value("${security.oauth.google.client-id:}") String googleClientId) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.googleClientId = googleClientId;
    }

    @Override
    public GoogleUserInfo verify(String idToken) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new BadRequestException("Google auth is not configured");
        }

        String encodedToken = URLEncoder.encode(idToken, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + encodedToken))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new UnauthorizedException("Invalid Google id token");
            }

            Map<String, Object> payload = objectMapper.readValue(response.body(), MAP_TYPE);
            return validatePayload(payload);
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid Google id token");
        }
    }

    private GoogleUserInfo validatePayload(Map<String, Object> payload) {
        if (payload == null) {
            throw new UnauthorizedException("Invalid Google id token");
        }

        String audience = asString(payload.get("aud"));
        if (!googleClientId.equals(audience)) {
            throw new UnauthorizedException("Google token audience mismatch");
        }

        String subject = asString(payload.get("sub"));
        String email = asString(payload.get("email"));
        String name = asString(payload.get("name"));
        boolean emailVerified = asBoolean(payload.get("email_verified"));

        if (subject == null || subject.isBlank() || email == null || email.isBlank()) {
            throw new UnauthorizedException("Invalid Google token claims");
        }
        if (!emailVerified) {
            throw new UnauthorizedException("Google account email is not verified");
        }

        return new GoogleUserInfo(subject, email, name);
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private boolean asBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            return "true".equalsIgnoreCase(text);
        }
        return false;
    }
}

