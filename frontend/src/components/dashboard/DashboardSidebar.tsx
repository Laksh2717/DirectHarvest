"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { AlertTriangle, LogOut, Sprout, User } from "lucide-react";
import { toast } from "sonner";
import ConfirmationModal from "@/components/ui/confirmation-modal";
import { authService } from "@/services/authService";
import type { DashboardSidebarProps } from "@/types/dashboard";

export default function DashboardSidebar({ basePath, navItems, profilePath, logoutDescription }: DashboardSidebarProps) {
    const pathname = usePathname();
    const router = useRouter();
    const [logoutOpen, setLogoutOpen] = useState(false);
    const [loggingOut, setLoggingOut] = useState(false);

    const handleLogout = async () => {
        setLoggingOut(true);
        try {
            await authService.logout();
            toast.success("Logged out successfully");
            setLogoutOpen(false);
            router.push("/");
        } catch {
            toast.error("Logout failed. Please try again.");
        } finally {
            setLoggingOut(false);
        }
    };

    const isNavItemActive = (href: string) => {
        if (pathname === href) {
            return true;
        }

        if (href === basePath) {
            return false;
        }

        return pathname.startsWith(`${href}/`);
    };

    return (
        <>
            <aside className="fixed left-0 top-0 bottom-0 z-40 flex w-60 flex-col border-r border-border bg-card">
                <div className="flex h-16 shrink-0 items-center gap-2 border-b border-border px-5">
                    <Sprout className="h-6 w-6 text-primary" />
                    <span className="text-lg font-semibold text-foreground">DirectHarvest</span>
                </div>

                <nav className="flex-1 space-y-0.5 overflow-y-auto px-3 py-4">
                    {navItems.map((item) => {
                        const isActive = isNavItemActive(item.href);
                        return (
                            <Link
                                key={item.href}
                                href={item.href}
                                className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-[13px] font-medium transition-all duration-200 ${isActive ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:bg-muted hover:text-foreground"}`}
                            >
                                <item.icon className="h-4.5 w-4.5" />
                                {item.label}
                            </Link>
                        );
                    })}
                </nav>

                <div className="space-y-0.5 border-t border-border px-3 pb-4 pt-3">
                    <Link
                        href={profilePath}
                        className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-[13px] font-medium transition-all duration-200 ${pathname === profilePath ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:bg-muted hover:text-foreground"}`}
                    >
                        <User className="h-4.5 w-4.5" />
                        Profile
                    </Link>
                    <button
                        type="button"
                        onClick={() => setLogoutOpen(true)}
                        className="flex w-full cursor-pointer items-center gap-3 rounded-lg px-3 py-2.5 text-[13px] font-medium text-muted-foreground transition-all duration-200 hover:bg-destructive/10 hover:text-destructive"
                    >
                        <LogOut className="h-4.5 w-4.5" />
                        Logout
                    </button>
                </div>
            </aside>

            <ConfirmationModal
                open={logoutOpen}
                title="Logout confirmation"
                description={logoutDescription}
                confirmText="Yes, log out"
                cancelText="Stay logged in"
                loading={loggingOut}
                icon={<AlertTriangle className="h-5 w-5" />}
                onConfirm={handleLogout}
                onCancel={() => setLogoutOpen(false)}
            />
        </>
    );
}
