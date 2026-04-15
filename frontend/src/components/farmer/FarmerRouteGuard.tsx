import DashboardRouteGuard from "@/components/dashboard/DashboardRouteGuard";

export default function FarmerRouteGuard({ children }: { children: React.ReactNode }) {
    return (
        <DashboardRouteGuard expectedRole="FARMER" loginPath="/login/farmer">
            {children}
        </DashboardRouteGuard>
    );
}
