package com.directharvest.backend.shared.cloudinary.service;

import com.directharvest.backend.common.exception.BadRequestException;
import com.directharvest.backend.shared.cloudinary.response.CloudinaryUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final Pattern STRING_FIELD_PATTERN = Pattern.compile("\"%s\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern NUMBER_FIELD_PATTERN = Pattern.compile("\"%s\"\\s*:\\s*(-?\\d+)");
    private static final String UPLOAD_FOLDER = "directharvest/listings";

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final HttpClient httpClient;

    public CloudinaryServiceImpl(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret
    ) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public CloudinaryUploadResponse uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required for Cloudinary upload");
        }

        validateCredentials();

        String body;
        try {
            String contentType = hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            String dataUri = "data:" + contentType + ";base64," + base64;

            long timestamp = Instant.now().getEpochSecond();
            Map<String, String> form = new LinkedHashMap<>();
            form.put("file", dataUri);
            form.put("api_key", apiKey);
            form.put("timestamp", String.valueOf(timestamp));
            form.put("folder", UPLOAD_FOLDER);

            Map<String, String> signatureParams = new TreeMap<>();
            signatureParams.put("folder", UPLOAD_FOLDER);
            signatureParams.put("timestamp", String.valueOf(timestamp));
            form.put("signature", sign(signatureParams));

            body = submitForm(uploadUrl(), form);
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Cloudinary upload failed: " + ex.getMessage());
        }

        String publicIdValue = extractString(body, "public_id");
        String secureUrl = extractString(body, "secure_url");

        return new CloudinaryUploadResponse(
                publicIdValue,
                secureUrl,
                extractString(body, "format"),
                extractInteger(body, "width"),
                extractInteger(body, "height"),
                extractLong(body, "bytes")
        );
    }

    @Override
    public void delete(String publicId) {
        if (!hasText(publicId)) {
            return;
        }
        validateCredentials();

        long timestamp = Instant.now().getEpochSecond();

        Map<String, String> signatureParams = new TreeMap<>();
        signatureParams.put("public_id", publicId.trim());
        signatureParams.put("timestamp", String.valueOf(timestamp));
        signatureParams.put("invalidate", "true");

        Map<String, String> form = new LinkedHashMap<>();
        form.put("public_id", publicId.trim());
        form.put("timestamp", String.valueOf(timestamp));
        form.put("invalidate", "true");
        form.put("api_key", apiKey);
        form.put("signature", sign(signatureParams));

        String response = submitForm(destroyUrl(), form);
        String result = extractString(response, "result");
        if (result == null) {
            throw new BadRequestException("Cloudinary delete failed: unexpected response");
        }
    }

    private String submitForm(String url, Map<String, String> form) {
        String encoded = form.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(encoded))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new BadRequestException("Cloudinary request failed: " + response.body());
            }
            return response.body();
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Cloudinary request failed: " + ex.getMessage());
        }
    }

    private String sign(Map<String, String> params) {
        String payload = params.entrySet().stream()
                .filter(entry -> hasText(entry.getValue()))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + "&" + right)
                .orElse("");

        String source = payload + apiSecret;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new BadRequestException("Unable to sign Cloudinary request");
        }
    }

    private String extractString(String json, String field) {
        Pattern pattern = Pattern.compile(String.format(STRING_FIELD_PATTERN.pattern(), Pattern.quote(field)));
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private Integer extractInteger(String json, String field) {
        Long value = extractLong(json, field);
        return value == null ? null : value.intValue();
    }

    private Long extractLong(String json, String field) {
        Pattern pattern = Pattern.compile(String.format(NUMBER_FIELD_PATTERN.pattern(), Pattern.quote(field)));
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
    }

    private void validateCredentials() {
        if (!hasText(cloudName) || !hasText(apiKey) || !hasText(apiSecret)) {
            throw new BadRequestException("Cloudinary credentials are not configured");
        }
    }


    private String uploadUrl() {
        return "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
    }

    private String destroyUrl() {
        return "https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

