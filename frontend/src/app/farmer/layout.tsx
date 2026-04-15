import FarmerSidebar from "@/components/farmer/FarmerSidebar";
import FarmerTopbar from "@/components/farmer/FarmerTopbar";
import FarmerRouteGuard from "@/components/farmer/FarmerRouteGuard";

export const metadata = {
    title: "Farmer Dashboard — DirectHarvest",
    description: "Manage your produce listings, negotiate offers, and track orders.",
};

export default function FarmerLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <FarmerRouteGuard>
            <div className="min-h-screen bg-background">
                <FarmerSidebar />
                <FarmerTopbar />
                <main className="ml-60 mt-16 p-8">
                    {children}
                </main>
            </div>
        </FarmerRouteGuard>
    );
}
