package com.directharvest.backend.dashboard.response;

import com.directharvest.backend.dashboard.request.OverviewChartGranularity;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dashboard overview chart response.")
public record OverviewChartResponse(
        @Schema(description = "User role for the chart", example = "BUYER")
        String userRole,
        @Schema(description = "Metric type (e.g., revenue, spending)", example = "revenue")
        String metric,
        @Schema(description = "Chart granularity (MONTHLY/YEARLY)", example = "MONTHLY")
        OverviewChartGranularity granularity,
        @Schema(description = "Selected year for the chart", example = "2026")
        Integer selectedYear,
        @Schema(description = "Available years for chart data")
        List<Integer> availableYears,
        @Schema(description = "Chart data points")
        List<OverviewChartPointResponse> points
) {}
