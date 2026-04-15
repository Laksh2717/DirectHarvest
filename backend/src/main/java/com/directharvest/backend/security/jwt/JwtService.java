package com.directharvest.backend.security.jwt;
import com.directharvest.backend.common.exception.UnauthorizedException;
import com.directharvest.backend.security.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
@Service
public class JwtService {
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private final String issuer;
    private final String secret;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    public JwtService(
            @Value("${security.jwt.issuer:directharvest}") String issuer,
            @Value("${security.jwt.secret:directharvest-directharvest-directharvest-secret-32+}") String secret,
            @Value("${security.jwt.access-token-expiration-ms:86400000}") long accessTokenExpirationMs,
            @Value("${security.jwt.refresh-token-expiration-ms:2592000000}") long refreshTokenExpirationMs
    ) {
        this.issuer = issuer;
        this.secret = secret;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, TOKEN_TYPE_ACCESS, accessTokenExpirationMs);
    }
    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, TOKEN_TYPE_REFRESH, refreshTokenExpirationMs);
    }
    public String extractUsername(String token) {
        return extractAllClaims(token).getOrDefault("sub", "").toString();
    }
    public Map<String, Object> extractAllClaims(String token) {
        String[] parts = splitToken(token);
        validateSignature(parts[0], parts[1], parts[2]);
        Map<String, Object> claims = parseClaims(decode(parts[1]));
        if (isExpired(claims)) {
            throw new UnauthorizedException("JWT token has expired");
        }
        return claims;
    }
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Map<String, Object> claims = extractAllClaims(token);
            String username = claims.getOrDefault("sub", "").toString();
            return username.equalsIgnoreCase(userDetails.getUsername());
        } catch (UnauthorizedException ex) {
            return false;
        }
    }
    public boolean isRefreshToken(String token) {
        try {
            return TOKEN_TYPE_REFRESH.equals(extractAllClaims(token).get("tokenType"));
        } catch (UnauthorizedException ex) {
            return false;
        }
    }
    private String generateToken(UserDetails userDetails, String tokenType, long ttlMs) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = Instant.now().plusMillis(ttlMs).getEpochSecond();
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", userDetails.getUsername());
        claims.put("iss", issuer);
        claims.put("iat", issuedAt);
        claims.put("exp", expiresAt);
        claims.put("tokenType", tokenType);
        claims.put("role", resolveRole(userDetails));
        String encodedHeader = encode(HEADER_JSON);
        String encodedPayload = encode(toJson(claims));
        String signature = sign(encodedHeader + "." + encodedPayload);
        return encodedHeader + "." + encodedPayload + "." + signature;
    }
    private String resolveRole(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getRole().name();
        }
        return userDetails.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .orElse("");
    }
    private boolean isExpired(Map<String, Object> claims) {
        Object exp = claims.get("exp");
        if (exp == null) {
            return true;
        }
        long expiry;
        if (exp instanceof Number number) {
            expiry = number.longValue();
        } else {
            expiry = Long.parseLong(exp.toString());
        }
        return Instant.now().getEpochSecond() > expiry;
    }
    private String[] splitToken(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("JWT token is missing");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new UnauthorizedException("Invalid JWT token format");
        }
        return parts;
    }
    private void validateSignature(String encodedHeader, String encodedPayload, String signature) {
        String expectedSignature = sign(encodedHeader + "." + encodedPayload);
        if (!expectedSignature.equals(signature)) {
            throw new UnauthorizedException("Invalid JWT signature");
        }
    }
    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
        } catch (Exception ex) {
            throw new UnauthorizedException("Unable to sign JWT token");
        }
    }
    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
    private String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
    private String toJson(Map<String, Object> claims) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(escape(entry.getKey())).append('"').append(':').append(formatJsonValue(entry.getValue()));
        }
        builder.append('}');
        return builder.toString();
    }
    private String formatJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return '"' + escape(value.toString()) + '"';
    }
    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    private Map<String, Object> parseClaims(String json) {
        if (json == null || json.isBlank()) {
            throw new UnauthorizedException("Invalid JWT token");
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new UnauthorizedException("Invalid JWT token");
        }
        Map<String, Object> claims = new LinkedHashMap<>();
        int index = 1;
        while (index < trimmed.length() - 1) {
            index = skipSeparators(trimmed, index);
            if (index >= trimmed.length() - 1) {
                break;
            }
            ParsedToken key = readJsonString(trimmed, index);
            index = skipWhitespace(trimmed, key.nextIndex());
            if (index >= trimmed.length() || trimmed.charAt(index) != ':') {
                throw new UnauthorizedException("Invalid JWT token");
            }
            index++;
            index = skipWhitespace(trimmed, index);
            ParsedValue value = readJsonValue(trimmed, index);
            claims.put(key.value(), value.value());
            index = value.nextIndex();
        }
        return claims;
    }
    private int skipSeparators(String value, int index) {
        while (index < value.length() - 1) {
            char c = value.charAt(index);
            if (!Character.isWhitespace(c) && c != ',') {
                break;
            }
            index++;
        }
        return index;
    }
    private int skipWhitespace(String value, int index) {
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        return index;
    }
    private ParsedToken readJsonString(String value, int index) {
        if (value.charAt(index) != '"') {
            throw new UnauthorizedException("Invalid JWT token");
        }
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;
        index++;
        while (index < value.length()) {
            char c = value.charAt(index);
            if (escaping) {
                builder.append(unescape(c));
                escaping = false;
            } else if (c == '\\') {
                escaping = true;
            } else if (c == '"') {
                return new ParsedToken(builder.toString(), index + 1);
            } else {
                builder.append(c);
            }
            index++;
        }
        throw new UnauthorizedException("Invalid JWT token");
    }
    private ParsedValue readJsonValue(String value, int index) {
        if (index >= value.length()) {
            throw new UnauthorizedException("Invalid JWT token");
        }
        if (value.charAt(index) == '"') {
            ParsedToken token = readJsonString(value, index);
            return new ParsedValue(token.value(), skipValueTerminator(value, token.nextIndex()));
        }
        int start = index;
        while (index < value.length()) {
            char c = value.charAt(index);
            if (c == ',' || c == '}') {
                break;
            }
            index++;
        }
        String raw = value.substring(start, index).trim();
        Object parsed = parsePrimitive(raw);
        return new ParsedValue(parsed, skipValueTerminator(value, index));
    }
    private int skipValueTerminator(String value, int index) {
        index = skipWhitespace(value, index);
        if (index < value.length() && value.charAt(index) == ',') {
            index++;
        }
        return index;
    }
    private Object parsePrimitive(String raw) {
        if (raw.isEmpty() || "null".equals(raw)) {
            return null;
        }
        if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw)) {
            return Boolean.parseBoolean(raw);
        }
        try {
            if (raw.contains(".")) {
                return Double.parseDouble(raw);
            }
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            return raw;
        }
    }
    private char unescape(char escaped) {
        return switch (escaped) {
            case '"' -> '"';
            case '\\' -> '\\';
            case '/' -> '/';
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            default -> escaped;
        };
    }
    private record ParsedToken(String value, int nextIndex) {
    }
    private record ParsedValue(Object value, int nextIndex) {
    }
}
