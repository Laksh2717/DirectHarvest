"use client";

import { FarmerOverviewKpis } from "@/components/dashboard/FarmerOverviewKpis";
import { OverviewQuickActions } from "@/components/dashboard/OverviewQuickActions";
import { OverviewRevenueSpendingChart } from "@/components/dashboard/OverviewRevenueSpendingChart";

export default function FarmerOverview() {
    return (
        <div className="space-y-8">
            <FarmerOverviewKpis />

            <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
                <div className="lg:col-span-3">
                    <OverviewRevenueSpendingChart role="FARMER" />
                </div>
                <div className="lg:col-span-2">
                    <OverviewQuickActions role="FARMER" />
                </div>
            </div>
        </div>
    );
}
