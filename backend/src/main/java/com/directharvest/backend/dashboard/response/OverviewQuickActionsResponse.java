package com.directharvest.backend.dashboard.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dashboard overview quick actions response.")
public record OverviewQuickActionsResponse(
        @Schema(description = "User role for the quick actions", example = "FARMER")
        String userRole,
        @Schema(description = "List of quick actions for the user")
        List<OverviewQuickActionResponse> actions
) {}
