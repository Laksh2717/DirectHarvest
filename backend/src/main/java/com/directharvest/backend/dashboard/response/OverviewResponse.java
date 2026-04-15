package com.directharvest.backend.dashboard.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dashboard overview response with role-specific KPIs.")
public record OverviewResponse(
        @Schema(description = "User role for the overview", example = "FARMER")
        String userRole,
        @Schema(description = "KPIs for farmer users")
        FarmerKpisResponse farmerKpis,
        @Schema(description = "KPIs for buyer users")
        BuyerKpisResponse buyerKpis
) {}
