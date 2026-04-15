package com.directharvest.backend.listings.response;

public record ListingImageResponse(
        Long id,
        String cloudinaryPublicId,
        String cloudinarySecureUrl,
        String format,
        Integer width,
        Integer height,
        Long bytes,
        boolean primary
) {
}

