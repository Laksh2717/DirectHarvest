import DashboardTopbar from "@/components/dashboard/DashboardTopbar";

const pageTitles: Record<string, string> = {
    "/buyer": "Overview",
    "/buyer/browse-products": "Browse Products",
    "/buyer/offers": "Offers & Negotiations",
    "/buyer/active-orders": "Active Orders",
    "/buyer/completed-orders": "Completed Orders",
    "/buyer/cancelled-orders": "Cancelled Orders",
    "/buyer/profile": "Profile",
};

export default function BuyerTopbar() {
    return <DashboardTopbar pageTitles={pageTitles} fallbackName="Buyer" />;
}
