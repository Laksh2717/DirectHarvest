package com.directharvest.backend.listings.response;

import java.util.List;

public record BrowseListingsResponse(
        List<ListingResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
