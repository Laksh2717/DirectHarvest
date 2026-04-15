"use client";

import { useMemo } from "react";
import { BarChart3 } from "lucide-react";
import type { ChartDatum, OverviewRevenueSpendingChartProps } from "@/types/dashboard";
import LoadingState from "@/components/ui/loading-state";
import EmptyState from "@/components/ui/empty-state";
import { formatCurrency } from "@/lib/formatters";
import {
    Area,
    CartesianGrid,
    Line,
    LineChart,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from "recharts";
import { useOverviewRevenueSpendingChart } from "@/hooks/dashboard/useOverviewRevenueSpendingChart";

function ChartTooltip({ active, payload, label }: { active?: boolean; payload?: Array<{ payload: ChartDatum }>; label?: string }) {
    if (!active || !payload || payload.length === 0) return null;

    const point = payload[0].payload;

    return (
        <div className="rounded-lg border border-border bg-background px-3 py-2 shadow-md">
            <p className="text-xs font-medium text-muted-foreground">{label}</p>
            <p className="text-sm font-semibold text-foreground">{formatCurrency(point.amount)}</p>
        </div>
    );
}

export function OverviewRevenueSpendingChart({ role }: OverviewRevenueSpendingChartProps) {
    const { data, loading, error, register, granularity, selectedYear, points } = useOverviewRevenueSpendingChart();

    const chartData = useMemo<ChartDatum[]>(
        () =>
            points.map((point) => ({
                label: point.label,
                year: point.year,
                month: point.month,
                amount: point.amount,
            })),
        [points]
    );

    const totalAmount = useMemo(() => points.reduce((sum, point) => sum + point.amount, 0), [points]);

    const metricLabel = role === "FARMER" ? "Revenue" : "Spending";
    const chartColor = "#15803d";
    const chartFill = "rgba(21, 128, 61, 0.15)";

    const totalValue = !loading && !error ? formatCurrency(totalAmount) : "--";

    return (
        <div className="rounded-lg border border-border bg-card p-5">
            <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                <div className="flex items-center gap-2">
                    <BarChart3 className="h-5 w-5 text-primary" />
                    <h3 className="text-base font-semibold text-foreground">
                        Total {metricLabel} Chart
                    </h3>
                </div>

                <div className="flex flex-wrap items-center gap-2">
                    <div className="rounded-lg border border-primary/30 bg-primary/10 px-3 py-2">
                        <p className="text-sm font-semibold text-foreground">Total: {totalValue}</p>
                    </div>

                    <select
                        {...register("granularity")}
                        className="rounded-md border border-border bg-background px-3 py-1.5 text-sm text-foreground"
                    >
                        <option value="MONTHLY">Month Wise</option>
                        <option value="YEARLY">Year Wise</option>
                    </select>

                    {granularity === "MONTHLY" && (
                        <select
                            {...register("selectedYear", { valueAsNumber: true })}
                            className="rounded-md border border-border bg-background px-3 py-1.5 text-sm text-foreground"
                        >
                            {(data?.availableYears ?? [selectedYear]).map((year) => (
                                <option key={year} value={year}>
                                    {year}
                                </option>
                            ))}
                        </select>
                    )}
                </div>
            </div>

            <div className="mt-5 min-h-75">
                {loading && (
                    <LoadingState
                        layout="inline"
                        message="Loading chart data..."
                        className="flex h-75 items-center justify-center"
                        cardClassName="max-w-none"
                    />
                )}

                {!loading && error && (
                    <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-800">
                        {error}
                    </div>
                )}

                {!loading && !error && points.length === 0 && (
                    <EmptyState
                        layout="inline"
                        message="No chart data available."
                        cardClassName="max-w-none border-dashed p-4"
                        className="flex h-75 items-center justify-center"
                    />
                )}

                {!loading && !error && points.length > 0 && (
                    <div className="h-80 rounded-lg border border-border bg-background p-2">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={chartData} margin={{ top: 20, right: 20, left: 20, bottom: 12 }}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" vertical={false} />
                                <XAxis
                                    dataKey="label"
                                    tick={{ fill: "#6b7280", fontSize: 12 }}
                                    axisLine={{ stroke: "#9ca3af" }}
                                    tickLine={{ stroke: "#9ca3af" }}
                                />
                                <YAxis
                                    tickFormatter={(value) => formatCurrency(Number(value))}
                                    tick={{ fill: "#6b7280", fontSize: 12 }}
                                    axisLine={{ stroke: "#9ca3af" }}
                                    tickLine={{ stroke: "#9ca3af" }}
                                    width={90}
                                />
                                <Tooltip content={<ChartTooltip />} />
                                <Line
                                    type="monotone"
                                    dataKey="amount"
                                    stroke={chartColor}
                                    strokeWidth={3}
                                    dot={{ r: 5, stroke: chartColor, strokeWidth: 2, fill: "#ffffff" }}
                                    activeDot={{ r: 7, stroke: chartColor, strokeWidth: 2, fill: "#ffffff" }}
                                />
                                <Area
                                    type="monotone"
                                    dataKey="amount"
                                    stroke="none"
                                    fill={chartFill}
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                )}
            </div>
        </div>
    );
}
