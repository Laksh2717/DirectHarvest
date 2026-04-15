import BuyerSidebar from "@/components/buyer/BuyerSidebar";
import BuyerTopbar from "@/components/buyer/BuyerTopbar";
import BuyerRouteGuard from "@/components/buyer/BuyerRouteGuard";

export const metadata = {
    title: "Buyer Dashboard — DirectHarvest",
    description: "Browse listings, manage offers, and track buyer orders.",
};

export default function BuyerLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <BuyerRouteGuard>
            <div className="min-h-screen bg-background">
                <BuyerSidebar />
                <BuyerTopbar />
                <main className="ml-60 mt-16 p-8">
                    {children}
                </main>
            </div>
        </BuyerRouteGuard>
    );
}
