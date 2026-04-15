package com.directharvest.backend.dashboard.response;

import java.math.BigDecimal;

public record OverviewChartPointResponse(
        Integer year,
        Integer month,
        String label,
        BigDecimal amount
) {}
