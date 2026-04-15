import { useEffect, useMemo, useState } from "react";
import { useForm, useWatch } from "react-hook-form";
import { dashboardService } from "@/services/dashboardService";;
import { toast } from "sonner";
import { resolveErrorMessage } from "@/lib/utils";
import type { OverviewChartFormValues, OverviewChartResponse } from "@/types/dashboard";

export function useOverviewRevenueSpendingChart() {
    const [data, setData] = useState<OverviewChartResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const { register, control, setValue } = useForm<OverviewChartFormValues>({
        defaultValues: {
            granularity: "MONTHLY",
            selectedYear: new Date().getFullYear(),
        },
    });

    const granularity = useWatch({ control, name: "granularity" }) ?? "MONTHLY";
    const selectedYear = useWatch({ control, name: "selectedYear" }) ?? new Date().getFullYear();

    useEffect(() => {
        let isMounted = true;

        const loadChart = async () => {
            try {
                setLoading(true);
                setError(null);
                const chartData = await dashboardService.getOverviewChart(
                    granularity,
                    granularity === "MONTHLY" ? selectedYear : undefined,
                );

                if (!isMounted) {
                    return;
                }

                setData(chartData);

                if (granularity === "MONTHLY" && chartData.selectedYear && chartData.selectedYear !== selectedYear) {
                    setValue("selectedYear", chartData.selectedYear);
                }
            } catch (err) {
                const message = resolveErrorMessage(err, "Failed to load chart data");
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

        void loadChart();

        return () => {
            isMounted = false;
        };
    }, [granularity, selectedYear, setValue]);

    const points = useMemo(() => data?.points ?? [], [data?.points]);

    return {
        data,
        loading,
        error,
        register,
        granularity,
        selectedYear,
        points,
    };
}