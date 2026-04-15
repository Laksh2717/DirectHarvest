"use client";

import {
    MessageSquare,
    ShoppingCart,
    CheckCircle2,
    XCircle,
} from "lucide-react";
import { KpiCard } from "./KpiCard";
import LoadingState from "@/components/ui/loading-state";
import EmptyState from "@/components/ui/empty-state";
import { useDashboardOverviewKpis } from "@/hooks/dashboard/useDashboardOverviewKpis";

export function BuyerOverviewKpis() {
    const { kpis, loading, error } = useDashboardOverviewKpis("BUYER");

    if (loading) {
        return (
            <LoadingState
                layout="inline"
                message="Loading buyer KPIs..."
                className="flex min-h-55 items-center justify-center"
            />
        );
    }

    if (error || !kpis) {
        return (
            <EmptyState
                layout="inline"
                message={error || "Failed to load KPIs"}
                cardClassName="max-w-none"
            />
        );
    }

    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {/* Total Active Negotiations */}
            <KpiCard
                title="Active Negotiations"
                value={kpis.totalActiveNegotiations}
                icon={<MessageSquare className="h-6 w-6 text-blue-600" />}
                bgColor="bg-blue-100"
            />

            {/* Active Orders */}
            <KpiCard
                title="Active Orders"
                value={kpis.activeOrders}
                icon={<ShoppingCart className="h-6 w-6 text-orange-600" />}
                bgColor="bg-orange-100"
            />

            {/* Total Completed Orders */}
            <KpiCard
                title="Completed Orders"
                value={kpis.totalCompletedOrders}
                icon={<CheckCircle2 className="h-6 w-6 text-green-600" />}
                bgColor="bg-green-100"
            />

            {/* Total Cancelled Orders */}
            <KpiCard
                title="Cancelled Orders"
                value={kpis.totalCancelledOrders}
                icon={<XCircle className="h-6 w-6 text-red-600" />}
                bgColor="bg-red-100"
            />
        </div>
    );
}
