"use client";

import { BuyerOverviewKpis } from "@/components/dashboard/BuyerOverviewKpis";
import { OverviewQuickActions } from "@/components/dashboard/OverviewQuickActions";
import { OverviewRevenueSpendingChart } from "@/components/dashboard/OverviewRevenueSpendingChart";

export default function BuyerOverview() {
    return (
        <div className="space-y-8">
            <BuyerOverviewKpis />

            <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
                <div className="lg:col-span-3">
                    <OverviewRevenueSpendingChart role="BUYER" />
                </div>
                <div className="lg:col-span-2">
                    <OverviewQuickActions role="BUYER" />
                </div>
            </div>
        </div>
    );
}
