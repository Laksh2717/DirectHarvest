import { useEffect, useState } from "react";
import { dashboardService, type BuyerKpisResponse, type FarmerKpisResponse } from "@/services/dashboardService";
import { toast } from "sonner";
import { resolveErrorMessage } from "@/lib/utils";
import type { DashboardRole, DashboardOverviewKpisState } from "@/types/dashboard";

export function useDashboardOverviewKpis(role: "BUYER"): DashboardOverviewKpisState<"BUYER">;
export function useDashboardOverviewKpis(role: "FARMER"): DashboardOverviewKpisState<"FARMER">;
export function useDashboardOverviewKpis(role: DashboardRole) {
    const [kpis, setKpis] = useState<BuyerKpisResponse | FarmerKpisResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let isMounted = true;

        const fetchKpis = async () => {
            try {
                setLoading(true);
                setError(null);

                const overview = await dashboardService.getOverview();
                const nextKpis = role === "BUYER" ? overview.buyerKpis : overview.farmerKpis;

                if (!isMounted) {
                    return;
                }

                if (nextKpis) {
                    setKpis(nextKpis);
                } else {
                    setError(role === "BUYER" ? "Failed to load buyer KPIs" : "Failed to load farmer KPIs");
                }
            } catch (err) {
                const message = resolveErrorMessage(err, "Failed to load KPIs");
                if (isMounted) {
                    setError(message);
                    toast.error(message);
                }
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        void fetchKpis();

        return () => {
            isMounted = false;
        };
    }, [role]);

    return { kpis, loading, error };
}