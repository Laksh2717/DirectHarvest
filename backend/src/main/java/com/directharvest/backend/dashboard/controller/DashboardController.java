package com.directharvest.backend.dashboard.controller;

import com.directharvest.backend.dashboard.request.OverviewChartGranularity;
import com.directharvest.backend.dashboard.response.OverviewChartResponse;
import com.directharvest.backend.dashboard.response.OverviewQuickActionsResponse;
import com.directharvest.backend.dashboard.response.OverviewResponse;
import com.directharvest.backend.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Dashboard overview endpoints")
@SecurityRequirement(name = "cookieAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('FARMER','BUYER')")
    @Operation(summary = "Get dashboard overview", description = "Returns KPIs for authenticated farmer or buyer user with role-specific metrics.")
    public ResponseEntity<OverviewResponse> getOverview() {
        return ResponseEntity.ok(dashboardService.getOverview());
    }

    @GetMapping("/overview/chart")
    @PreAuthorize("hasAnyRole('FARMER','BUYER')")
    @Operation(
            summary = "Get overview chart data",
            description = "Returns role-based revenue/spending chart points only. Supports MONTHLY (default) and YEARLY without re-fetching full overview payload."
    )
    public ResponseEntity<OverviewChartResponse> getOverviewChart(
            @RequestParam(name = "granularity", defaultValue = "MONTHLY") OverviewChartGranularity granularity,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        return ResponseEntity.ok(dashboardService.getOverviewChart(granularity, year));
    }

    @GetMapping("/overview/quick-actions")
    @PreAuthorize("hasAnyRole('FARMER','BUYER')")
    @Operation(
            summary = "Get overview quick actions",
            description = "Returns top 3 role-based quick actions ordered by priority: my-turn negotiations, active orders, then fallback actions."
    )
    public ResponseEntity<OverviewQuickActionsResponse> getOverviewQuickActions() {
        return ResponseEntity.ok(dashboardService.getOverviewQuickActions());
    }
}
