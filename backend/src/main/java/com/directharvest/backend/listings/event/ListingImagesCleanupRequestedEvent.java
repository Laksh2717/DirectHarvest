package com.directharvest.backend.listings.event;

import java.util.Set;

public record ListingImagesCleanupRequestedEvent(Set<String> publicIds) {
}

