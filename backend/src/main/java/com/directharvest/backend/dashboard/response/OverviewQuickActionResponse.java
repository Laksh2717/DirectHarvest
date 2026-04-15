package com.directharvest.backend.dashboard.response;

public record OverviewQuickActionResponse(
        String actionType,
        String title,
        String description,
        String ctaLabel,
        String ctaPath,
        Long referenceId
) {}
