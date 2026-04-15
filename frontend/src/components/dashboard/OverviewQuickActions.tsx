"use client";

import Link from "next/link";
import {
    Bell,
    CircleCheckBig,
    HandCoins,
    Leaf,
    PackageSearch,
    ShoppingBag,
    Truck,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import type { OverviewQuickActionResponse, OverviewQuickActionsProps } from "@/types/dashboard";
import LoadingState from "@/components/ui/loading-state";
import EmptyState from "@/components/ui/empty-state";
import { useOverviewQuickActions } from "@/hooks/dashboard/useOverviewQuickActions";

const getActionIcon = (actionType: OverviewQuickActionResponse["actionType"]) => {
    switch (actionType) {
        case "NEGOTIATION_MY_TURN":
            return <HandCoins className="h-4 w-4 text-amber-700" />;
        case "PICKUP_PENDING":
            return <Truck className="h-4 w-4 text-blue-700" />;
        case "LOW_STOCK":
            return <Leaf className="h-4 w-4 text-emerald-700" />;
        case "BROWSE_PRODUCTS":
            return <ShoppingBag className="h-4 w-4 text-violet-700" />;
        case "NO_URGENT_ACTIONS":
        case "ALL_CAUGHT_UP":
            return <CircleCheckBig className="h-4 w-4 text-green-700" />;
        case "VIEW_COMPLETED":
            return <PackageSearch className="h-4 w-4 text-cyan-700" />;
        default:
            return <Bell className="h-4 w-4 text-muted-foreground" />;
    }
};

export function OverviewQuickActions({ role }: OverviewQuickActionsProps) {
    const { actions, loading, error } = useOverviewQuickActions();

    return (
        <div className="rounded-lg border border-border bg-card p-4">
            <h3 className="mb-2 text-sm font-semibold text-foreground">Quick Actions</h3>

            {loading && (
                <LoadingState
                    layout="inline"
                    message="Loading quick actions..."
                    className="flex min-h-55 items-center justify-center"
                    cardClassName="max-w-none"
                />
            )}

            {!loading && error && (
                <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-800">
                    {error}
                </div>
            )}

            {!loading && !error && actions.length === 0 && (
                <EmptyState
                    layout="inline"
                    message="No quick actions available."
                    cardClassName="max-w-none border-dashed p-4"
                />
            )}

            {!loading && !error && actions.length > 0 && (
                <div className="space-y-2">
                    {actions.map((action, index) => (
                        <div key={`${action.actionType}-${action.referenceId ?? index}`} className="rounded-lg border border-border/70 bg-background p-3">
                            <div className="mb-2 flex items-center gap-2">
                                <span className="rounded-md bg-muted p-1">{getActionIcon(action.actionType)}</span>
                                <p className="text-sm font-semibold text-foreground">{action.title}</p>
                            </div>

                            <p className="text-xs text-muted-foreground">{action.description}</p>

                            <div className="mt-2">
                                <Button asChild size="sm" className="h-7">
                                    <Link href={action.ctaPath}>{action.ctaLabel}</Link>
                                </Button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
