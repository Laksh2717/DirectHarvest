import DashboardRouteGuard from "@/components/dashboard/DashboardRouteGuard";

export default function BuyerRouteGuard({ children }: { children: React.ReactNode }) {
    return (
        <DashboardRouteGuard expectedRole="BUYER" loginPath="/login/buyer">
            {children}
        </DashboardRouteGuard>
    );
}
