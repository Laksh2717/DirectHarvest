import DashboardTopbar from "@/components/dashboard/DashboardTopbar";

const pageTitles: Record<string, string> = {
    "/farmer": "Overview",
    "/farmer/create-listing": "Create Listing",
    "/farmer/listings": "My Listings",
    "/farmer/offers": "Offers & Negotiations",
    "/farmer/active-orders": "Active Orders",
    "/farmer/completed-orders": "Completed Orders",
    "/farmer/cancelled-orders": "Cancelled Orders",
    "/farmer/profile": "Profile",
};

const FarmerTopbar = () => {
    return <DashboardTopbar pageTitles={pageTitles} fallbackName="Farmer" />;
};

export default FarmerTopbar;
