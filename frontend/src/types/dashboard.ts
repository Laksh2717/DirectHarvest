import type { UserRole } from "@/types/common";
import { LucideIcon } from "lucide-react";
import { ReactNode } from "react";

export interface FarmerKpisResponse {
    totalActiveListings: number;
    activeOrders: number;
    totalCompletedOrders: number;
    averageRating: number | null;
    ratingCount: number;
    totalCancelledOrders: number;
}

export interface BuyerKpisResponse {
    totalActiveNegotiations: number;
    activeOrders: number;
    totalCompletedOrders: number;
    totalCancelledOrders: number;
}

export interface OverviewResponse {
    userRole: UserRole;
    farmerKpis?: FarmerKpisResponse;
    buyerKpis?: BuyerKpisResponse;
}

export type OverviewChartGranularity = "MONTHLY" | "YEARLY";

export interface OverviewChartPointResponse {
    year: number;
    month: number | null;
    label: string;
    amount: number;
}

export interface OverviewChartResponse {
    userRole: UserRole;
    metric: "TOTAL_REVENUE" | "TOTAL_SPENDING";
    granularity: OverviewChartGranularity;
    selectedYear: number | null;
    availableYears: number[];
    points: OverviewChartPointResponse[];
}

export interface OverviewQuickActionResponse {
    actionType:
        | "NEGOTIATION_MY_TURN"
        | "PICKUP_PENDING"
        | "LOW_STOCK"
        | "ALL_CAUGHT_UP"
        | "REVIEW_LISTINGS"
        | "BROWSE_PRODUCTS"
        | "NO_URGENT_ACTIONS"
        | "VIEW_COMPLETED";
    title: string;
    description: string;
    ctaLabel: string;
    ctaPath: string;
    referenceId: number | null;
}

export interface OverviewQuickActionsResponse {
    userRole: UserRole;
    actions: OverviewQuickActionResponse[];
}

export type DashboardRole = "BUYER" | "FARMER";

export type DashboardKpisByRole = {
    BUYER: BuyerKpisResponse;
    FARMER: FarmerKpisResponse;
};

export type DashboardOverviewKpisState<T extends DashboardRole> = {
    kpis: DashboardKpisByRole[T] | null;
    loading: boolean;
    error: string | null;
};

export type OverviewChartFormValues = {
    granularity: OverviewChartGranularity;
    selectedYear: number;
};  

export type DashboardRouteGuardProps = {
    expectedRole: UserRole;
    loginPath: string;
    children: React.ReactNode;
};

export type DashboardNavItem = {
    label: string;
    icon: LucideIcon;
    href: string;
};

export type DashboardSidebarProps = {
    basePath: string;
    navItems: DashboardNavItem[];
    profilePath: string;
    logoutDescription: string;
};

export type DashboardTopbarProps = {
    pageTitles: Record<string, string>;
    fallbackName: string;
};

export interface KpiCardProps {
    title: string;
    value: string | number;
    icon: ReactNode;
    subtitle?: string;
    bgColor?: string;
    trend?: "up" | "down" | "neutral";
    compact?: boolean;
}

export type OverviewQuickActionsProps = {
    role: "FARMER" | "BUYER";
};

export type OverviewRevenueSpendingChartProps = {
    role: "FARMER" | "BUYER";
};

export type ChartDatum = {
    label: string;
    year: number;
    month: number | null;
    amount: number;
};