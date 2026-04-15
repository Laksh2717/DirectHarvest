import api from "@/lib/api";
import type {
    OverviewChartGranularity,
    OverviewChartResponse,
    OverviewQuickActionsResponse,
    OverviewResponse,
} from "@/types/dashboard";

export type {
    BuyerKpisResponse,
    FarmerKpisResponse,
    OverviewChartGranularity,
    OverviewChartPointResponse,
    OverviewChartResponse,
    OverviewQuickActionResponse,
    OverviewQuickActionsResponse,
    OverviewResponse,
} from "@/types/dashboard";

export const dashboardService = {
    getOverview: async () => {
        const response = await api.get<OverviewResponse>("/dashboard/overview");
        return response.data;
    },

    getOverviewChart: async (granularity: OverviewChartGranularity, year?: number) => {
        const params = new URLSearchParams();
        params.set("granularity", granularity);
        if (typeof year === "number") {
            params.set("year", String(year));
        }

        const response = await api.get<OverviewChartResponse>(`/dashboard/overview/chart?${params.toString()}`);
        return response.data;
    },

    getOverviewQuickActions: async () => {
        const response = await api.get<OverviewQuickActionsResponse>("/dashboard/overview/quick-actions");
        return response.data;
    },
};
