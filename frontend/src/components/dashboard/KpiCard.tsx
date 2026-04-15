"use client";

import type { KpiCardProps } from "@/types/dashboard";

export function KpiCard({
    title,
    value,
    icon,
    bgColor = "bg-blue-50",
    compact = false,
}: KpiCardProps) {
    const cardPaddingClass = compact ? "p-4" : "p-6";
    const titleClass = compact ? "text-xs mb-2" : "text-sm mb-2";
    const valueClass = "text-2xl";
    const iconWrapClass = compact ? "rounded-lg p-2" : "rounded-lg p-3";

    return (
        <div className={`rounded-xl border border-border bg-card shadow-sm hover:shadow-md transition-shadow ${cardPaddingClass}`}>
            <div className="flex items-start justify-between">
                <div className="flex-1">
                    <p className={`font-medium text-muted-foreground ${titleClass}`}>{title}</p>
                    <p className={`font-bold text-foreground ${valueClass}`}>{value}</p>
                </div>
                <div className={`${bgColor} ${iconWrapClass} flex items-center justify-center flex-shrink-0`}>
                    {icon}
                </div>
            </div>
        </div>
    );
}
