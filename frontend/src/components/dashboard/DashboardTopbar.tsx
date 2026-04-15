"use client";

import { useSyncExternalStore } from "react";
import { usePathname } from "next/navigation";
import { sessionService } from "@/services/sessionService";
import type { DashboardTopbarProps } from "@/types/dashboard";

export default function DashboardTopbar({ pageTitles, fallbackName }: DashboardTopbarProps) {
    const pathname = usePathname();
    const pageTitle = pageTitles[pathname] || "Dashboard";
    const displayName = useSyncExternalStore(
        sessionService.subscribe,
        () => sessionService.getFormattedActiveName() ?? fallbackName,
        () => fallbackName,
    );

    const initials =
        displayName
            .split(" ")
            .filter(Boolean)
            .slice(0, 2)
            .map((part) => part[0]?.toUpperCase())
            .join("") || fallbackName.charAt(0).toUpperCase();

    return (
        <header className="fixed left-60 top-0 right-0 z-30 flex h-16 items-center justify-between border-b border-border bg-card px-8">
            <h1 className="text-xl font-semibold text-foreground">{pageTitle}</h1>
            <div className="flex items-center gap-3">
                <span className="text-sm text-muted-foreground">
                    Welcome, <span className="font-semibold text-foreground">{displayName}</span>
                </span>
                <div className="flex h-9 w-9 items-center justify-center rounded-full bg-primary">
                    <span className="text-sm font-bold text-primary-foreground">{initials}</span>
                </div>
            </div>
        </header>
    );
}
