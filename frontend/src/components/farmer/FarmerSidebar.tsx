"use client";

import {
    LayoutDashboard,
    PlusCircle,
    Package,
    Handshake,
    Truck,
    CheckCircle,
    XCircle,
    type LucideIcon,
} from "lucide-react";
import DashboardSidebar from "@/components/dashboard/DashboardSidebar";

const navItems = [
    { label: "Overview", icon: LayoutDashboard, href: "/farmer" },
    { label: "Create Listing", icon: PlusCircle, href: "/farmer/create-listing" },
    { label: "My Listings", icon: Package, href: "/farmer/listings" },
    { label: "Offers & Negotiations", icon: Handshake, href: "/farmer/offers" },
    { label: "Active Orders", icon: Truck, href: "/farmer/active-orders" },
    { label: "Completed Orders", icon: CheckCircle, href: "/farmer/completed-orders" },
    { label: "Cancelled Orders", icon: XCircle, href: "/farmer/cancelled-orders" },
] as Array<{ label: string; icon: LucideIcon; href: string }>;

const FarmerSidebar = () => {
    return (
        <DashboardSidebar
            basePath="/farmer"
            navItems={navItems}
            profilePath="/farmer/profile"
            logoutDescription="Are you sure you want to log out of your farmer account?"
        />
    );
};

export default FarmerSidebar;
