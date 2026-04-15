"use client";


import { LayoutDashboard, PackageSearch, Handshake, Truck, CheckCircle, XCircle, type LucideIcon } from "lucide-react";
import DashboardSidebar from "@/components/dashboard/DashboardSidebar";

const navItems = [
    { label: "Overview", icon: LayoutDashboard, href: "/buyer" },
    { label: "Browse Products", icon: PackageSearch, href: "/buyer/browse-products" },
    { label: "Offers & Negotiations", icon: Handshake, href: "/buyer/offers" },
    { label: "Active Orders", icon: Truck, href: "/buyer/active-orders" },
    { label: "Completed Orders", icon: CheckCircle, href: "/buyer/completed-orders" },
    { label: "Cancelled Orders", icon: XCircle, href: "/buyer/cancelled-orders" },
] as Array<{ label: string; icon: LucideIcon; href: string }>;

export default function BuyerSidebar() {
    return (
        <DashboardSidebar
            basePath="/buyer"
            navItems={navItems}
            profilePath="/buyer/profile"
            logoutDescription="Are you sure you want to log out of your buyer account?"
        />
    );
}
