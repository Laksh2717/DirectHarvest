"use client";

import {
    Package,
    ShoppingCart,
    CheckCircle2,
    XCircle,
    Star,
} from "lucide-react";
import { KpiCard } from "./KpiCard";
import LoadingState from "@/components/ui/loading-state";
import EmptyState from "@/components/ui/empty-state";
import { formatRating } from "@/lib/formatters";
import { useDashboardOverviewKpis } from "@/hooks/dashboard/useDashboardOverviewKpis";

export function FarmerOverviewKpis() {
    const { kpis, loading, error } = useDashboardOverviewKpis("FARMER");

    if (loading) {
        return (
            <LoadingState
                layout="inline"
                message="Loading farmer KPIs..."
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
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
            {/* Total Active Listings */}
            <KpiCard
                title="Total Active Listings"
                value={kpis.totalActiveListings}
                icon={<Package className="h-5 w-5 text-blue-600" />}
                bgColor="bg-blue-100"
                compact
            />

            {/* Active Orders */}
            <KpiCard
                title="Active Orders"
                value={kpis.activeOrders}
                icon={<ShoppingCart className="h-5 w-5 text-orange-600" />}
                bgColor="bg-orange-100"
                compact
            />

            {/* Total Completed Orders */}
            <KpiCard
                title="Completed Orders"
                value={kpis.totalCompletedOrders}
                icon={<CheckCircle2 className="h-5 w-5 text-green-600" />}
                bgColor="bg-green-100"
                compact
            />

            {/* Average Rating */}
            <KpiCard
                title="Average Rating"
                value={`${formatRating(kpis.averageRating)} (${kpis.ratingCount})`}
                icon={<Star className="h-5 w-5 text-yellow-600" />}
                bgColor="bg-yellow-100"
                compact
            />

            {/* Total Cancelled Orders */}
            <KpiCard
                title="Cancelled Orders"
                value={kpis.totalCancelledOrders}
                icon={<XCircle className="h-5 w-5 text-red-600" />}
                bgColor="bg-red-100"
                compact
            />
        </div>
    );
}
